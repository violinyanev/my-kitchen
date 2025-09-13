#!/usr/bin/env python3

#  -------------------------------------------------------------------------
#  Copyright (C) 2023 Violin Yanev
#  -------------------------------------------------------------------------
#  This Source Code Form is subject to the terms of the Mozilla Public
#  License, v. 2.0. If a copy of the MPL was not distributed with this
#  file, You can obtain one at https://mozilla.org/MPL/2.0/.
#  -------------------------------------------------------------------------

"""
Automatic copilot retrigger script.

This script monitors pull requests assigned to or created by copilot and
automatically adds retrigger comments when PRs fail to merge or have failing checks.

The script uses GraphQL to efficiently fetch all PR data in a single query,
including author, assignees, mergeable status, check runs, and comments.
This reduces API calls from potentially dozens to just one fetch plus
individual comment additions.
"""

import os
import sys
import json
import argparse
import requests
from typing import List, Dict, Optional
from datetime import datetime


class GitHubAPI:
    """Helper class for GitHub API interactions using GraphQL."""
    
    def __init__(self, token: str, owner: str, repo: str):
        self.token = token
        self.owner = owner
        self.repo = repo
        self.graphql_url = "https://api.github.com/graphql"
        self.rest_url = "https://api.github.com"
        self.headers = {
            "Authorization": f"token {token}",
            "Accept": "application/vnd.github.v3+json",
            "User-Agent": "copilot-retrigger-script"
        }
    
    def fetch_all_pr_data(self) -> List[Dict]:
        """Fetch all PR data in a single GraphQL query."""
        query = """
        query($owner: String!, $repo: String!) {
          repository(owner: $owner, name: $repo) {
            pullRequests(states: OPEN, first: 100, orderBy: {field: UPDATED_AT, direction: DESC}) {
              nodes {
                number
                title
                mergeable
                author {
                  login
                }
                assignees(first: 10) {
                  nodes {
                    login
                  }
                }
                headRef {
                  target {
                    ... on Commit {
                      oid
                      checkSuites(first: 50) {
                        nodes {
                          checkRuns(first: 50) {
                            nodes {
                              conclusion
                              name
                              status
                            }
                          }
                        }
                      }
                      statusCheckRollup {
                        state
                      }
                    }
                  }
                }
                comments(first: 100) {
                  nodes {
                    body
                    author {
                      login
                    }
                  }
                }
              }
            }
          }
        }
        """
        
        variables = {
            "owner": self.owner,
            "repo": self.repo
        }
        
        response = requests.post(
            self.graphql_url,
            headers=self.headers,
            json={"query": query, "variables": variables}
        )
        response.raise_for_status()
        
        data = response.json()
        if "errors" in data:
            raise Exception(f"GraphQL errors: {data['errors']}")
        
        return data["data"]["repository"]["pullRequests"]["nodes"]
    
    def add_comment(self, pr_number: int, comment: str) -> Dict:
        """Add a comment to a pull request using REST API."""
        url = f"{self.rest_url}/repos/{self.owner}/{self.repo}/issues/{pr_number}/comments"
        data = {"body": comment}
        
        response = requests.post(url, headers=self.headers, json=data)
        response.raise_for_status()
        return response.json()


class CopilotRetrigger:
    """Main class for handling copilot retrigger logic."""
    
    MERGE_CONFLICT_COMMENT = "@copilot can you update the branch and fix the conflicts?"
    FAILING_CHECKS_COMMENT = "@copilot can you fix the failing github actions?"
    MAX_COMMENTS = 3
    COPILOT_USERS = ["copilot", "github-copilot[bot]"]  # Add known copilot usernames
    
    def __init__(self, github_api: GitHubAPI, dry_run: bool = False):
        self.github_api = github_api
        self.dry_run = dry_run
    
    def is_copilot_pr(self, pr: Dict) -> bool:
        """Check if a PR is assigned to or created by copilot."""
        # Check if author is copilot
        author = pr.get("author", {})
        if author and author.get("login"):
            author_login = author["login"]
            if any(copilot_user in author_login.lower() for copilot_user in self.COPILOT_USERS):
                return True
        
        # Check if assigned to copilot
        assignees = pr.get("assignees", {}).get("nodes", [])
        for assignee in assignees:
            assignee_login = assignee.get("login", "")
            if any(copilot_user in assignee_login.lower() for copilot_user in self.COPILOT_USERS):
                return True
        
        return False
    
    def has_merge_conflicts(self, pr: Dict) -> bool:
        """Check if a PR has merge conflicts."""
        # In GraphQL response, mergeable is directly available
        return pr.get("mergeable") == "CONFLICTING"
    
    def has_failing_checks(self, pr: Dict) -> bool:
        """Check if a PR has failing CI checks."""
        head_ref = pr.get("headRef")
        if not head_ref or not head_ref.get("target"):
            return False
        
        commit = head_ref["target"]
        
        # Check status check rollup first (simpler)
        status_rollup = commit.get("statusCheckRollup")
        if status_rollup and status_rollup.get("state") == "FAILURE":
            return True
        
        # Check individual check runs
        check_suites = commit.get("checkSuites", {}).get("nodes", [])
        for suite in check_suites:
            check_runs = suite.get("checkRuns", {}).get("nodes", [])
            for check in check_runs:
                if check.get("conclusion") == "FAILURE":
                    return True
        
        return False
    
    def count_retrigger_comments(self, pr: Dict) -> int:
        """Count how many retrigger comments have been added by this script."""
        comments = pr.get("comments", {}).get("nodes", [])
        count = 0
        
        for comment in comments:
            body = comment.get("body", "")
            if (self.MERGE_CONFLICT_COMMENT in body or 
                self.FAILING_CHECKS_COMMENT in body):
                count += 1
        
        return count
    
    def process_pr(self, pr: Dict) -> None:
        """Process a single PR and add retrigger comments if needed."""
        pr_number = pr["number"]
        pr_title = pr["title"]
        
        print(f"Processing PR #{pr_number}: {pr_title}")
        
        # Check if this is a copilot PR
        if not self.is_copilot_pr(pr):
            print(f"  Skipping - not a copilot PR")
            return
        
        # Check if we've already added the maximum number of comments
        comment_count = self.count_retrigger_comments(pr)
        if comment_count >= self.MAX_COMMENTS:
            print(f"  Skipping - already added {comment_count} retrigger comments")
            return
        
        # Check for merge conflicts
        if self.has_merge_conflicts(pr):
            if self.dry_run:
                print(f"  [DRY RUN] Would add merge conflict comment: {self.MERGE_CONFLICT_COMMENT}")
            else:
                print(f"  Found merge conflicts, adding comment")
                self.github_api.add_comment(pr_number, self.MERGE_CONFLICT_COMMENT)
            return
        
        # Check for failing checks
        if self.has_failing_checks(pr):
            if self.dry_run:
                print(f"  [DRY RUN] Would add failing checks comment: {self.FAILING_CHECKS_COMMENT}")
            else:
                print(f"  Found failing checks, adding comment")
                self.github_api.add_comment(pr_number, self.FAILING_CHECKS_COMMENT)
            return
        
        print(f"  No issues found")
    
    def run(self) -> None:
        """Main entry point to process all copilot PRs."""
        mode_str = "[DRY RUN] " if self.dry_run else ""
        print(f"{mode_str}Starting copilot retrigger check at {datetime.now()}")
        
        # Get all PR data in a single GraphQL query
        prs = self.github_api.fetch_all_pr_data()
        print(f"{mode_str}Found {len(prs)} open pull requests")
        
        # Process each PR
        for pr in prs:
            try:
                self.process_pr(pr)
            except Exception as e:
                print(f"Error processing PR #{pr['number']}: {e}")
        
        print(f"{mode_str}Copilot retrigger check completed")


def main():
    """Main function."""
    # Parse command line arguments
    parser = argparse.ArgumentParser(description="Automatic copilot retrigger script")
    parser.add_argument("--dry-run", action="store_true", 
                       help="Show what actions would be taken without making any changes")
    args = parser.parse_args()
    
    # Get required environment variables
    github_token = os.getenv("GITHUB_TOKEN")
    if not github_token:
        print("Error: GITHUB_TOKEN environment variable is required")
        sys.exit(1)
    
    # Extract repository info from GitHub Actions environment or use defaults
    github_repository = os.getenv("GITHUB_REPOSITORY", "violinyanev/my-kitchen")
    owner, repo = github_repository.split("/")
    
    # Initialize GitHub API and retrigger handler
    github_api = GitHubAPI(github_token, owner, repo)
    retrigger = CopilotRetrigger(github_api, dry_run=args.dry_run)
    
    # Run the retrigger check
    try:
        retrigger.run()
    except Exception as e:
        print(f"Error running copilot retrigger: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
