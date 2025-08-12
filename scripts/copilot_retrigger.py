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
"""

import os
import sys
import json
import requests
from typing import List, Dict, Optional
from datetime import datetime


class GitHubAPI:
    """Helper class for GitHub API interactions."""
    
    def __init__(self, token: str, owner: str, repo: str):
        self.token = token
        self.owner = owner
        self.repo = repo
        self.base_url = "https://api.github.com"
        self.headers = {
            "Authorization": f"token {token}",
            "Accept": "application/vnd.github.v3+json",
            "User-Agent": "copilot-retrigger-script"
        }
    
    def get_pull_requests(self, state: str = "open") -> List[Dict]:
        """Get all pull requests for the repository."""
        url = f"{self.base_url}/repos/{self.owner}/{self.repo}/pulls"
        params = {"state": state, "per_page": 100}
        
        response = requests.get(url, headers=self.headers, params=params)
        response.raise_for_status()
        return response.json()
    
    def get_pull_request_comments(self, pr_number: int) -> List[Dict]:
        """Get all comments for a specific pull request."""
        url = f"{self.base_url}/repos/{self.owner}/{self.repo}/issues/{pr_number}/comments"
        
        response = requests.get(url, headers=self.headers)
        response.raise_for_status()
        return response.json()
    
    def add_comment(self, pr_number: int, comment: str) -> Dict:
        """Add a comment to a pull request."""
        url = f"{self.base_url}/repos/{self.owner}/{self.repo}/issues/{pr_number}/comments"
        data = {"body": comment}
        
        response = requests.post(url, headers=self.headers, json=data)
        response.raise_for_status()
        return response.json()
    
    def get_pr_status(self, pr_number: int) -> Dict:
        """Get the detailed status of a pull request."""
        url = f"{self.base_url}/repos/{self.owner}/{self.repo}/pulls/{pr_number}"
        
        response = requests.get(url, headers=self.headers)
        response.raise_for_status()
        return response.json()
    
    def get_check_runs(self, sha: str) -> List[Dict]:
        """Get check runs for a specific commit."""
        url = f"{self.base_url}/repos/{self.owner}/{self.repo}/commits/{sha}/check-runs"
        
        response = requests.get(url, headers=self.headers)
        response.raise_for_status()
        return response.json().get("check_runs", [])


class CopilotRetrigger:
    """Main class for handling copilot retrigger logic."""
    
    MERGE_CONFLICT_COMMENT = "@copilot can you update the branch and fix the conflicts?"
    FAILING_CHECKS_COMMENT = "@copilot can you fix the failing github actions?"
    MAX_COMMENTS = 3
    COPILOT_USERS = ["copilot", "github-copilot[bot]"]  # Add known copilot usernames
    
    def __init__(self, github_api: GitHubAPI):
        self.github_api = github_api
    
    def is_copilot_pr(self, pr: Dict) -> bool:
        """Check if a PR is assigned to or created by copilot."""
        # Check if author is copilot
        author = pr.get("user", {}).get("login", "")
        if any(copilot_user in author.lower() for copilot_user in self.COPILOT_USERS):
            return True
        
        # Check if assigned to copilot
        assignees = pr.get("assignees", [])
        for assignee in assignees:
            if any(copilot_user in assignee.get("login", "").lower() for copilot_user in self.COPILOT_USERS):
                return True
        
        return False
    
    def has_merge_conflicts(self, pr: Dict) -> bool:
        """Check if a PR has merge conflicts."""
        # Get detailed PR status
        pr_status = self.github_api.get_pr_status(pr["number"])
        return pr_status.get("mergeable") is False
    
    def has_failing_checks(self, pr: Dict) -> bool:
        """Check if a PR has failing CI checks."""
        sha = pr["head"]["sha"]
        check_runs = self.github_api.get_check_runs(sha)
        
        for check in check_runs:
            if check.get("conclusion") == "failure":
                return True
        
        return False
    
    def count_retrigger_comments(self, pr_number: int) -> int:
        """Count how many retrigger comments have been added by this script."""
        comments = self.github_api.get_pull_request_comments(pr_number)
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
        comment_count = self.count_retrigger_comments(pr_number)
        if comment_count >= self.MAX_COMMENTS:
            print(f"  Skipping - already added {comment_count} retrigger comments")
            return
        
        # Check for merge conflicts
        if self.has_merge_conflicts(pr):
            print(f"  Found merge conflicts, adding comment")
            self.github_api.add_comment(pr_number, self.MERGE_CONFLICT_COMMENT)
            return
        
        # Check for failing checks
        if self.has_failing_checks(pr):
            print(f"  Found failing checks, adding comment")
            self.github_api.add_comment(pr_number, self.FAILING_CHECKS_COMMENT)
            return
        
        print(f"  No issues found")
    
    def run(self) -> None:
        """Main entry point to process all copilot PRs."""
        print(f"Starting copilot retrigger check at {datetime.now()}")
        
        # Get all open pull requests
        prs = self.github_api.get_pull_requests()
        print(f"Found {len(prs)} open pull requests")
        
        # Process each PR
        for pr in prs:
            try:
                self.process_pr(pr)
            except Exception as e:
                print(f"Error processing PR #{pr['number']}: {e}")
        
        print("Copilot retrigger check completed")


def main():
    """Main function."""
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
    retrigger = CopilotRetrigger(github_api)
    
    # Run the retrigger check
    try:
        retrigger.run()
    except Exception as e:
        print(f"Error running copilot retrigger: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()