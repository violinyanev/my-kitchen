#!/usr/bin/env python3

import unittest
from unittest.mock import Mock, patch, MagicMock
import os
import sys
import json
import requests
from datetime import datetime

# Add the scripts directory to the path so we can import the module
sys.path.insert(0, os.path.dirname(__file__))

from copilot_retrigger import GitHubAPI, CopilotRetrigger


class TestGitHubAPI(unittest.TestCase):
    """Test cases for the GitHubAPI class."""
    
    def setUp(self):
        self.api = GitHubAPI("test_token", "test_owner", "test_repo")
    
    def test_init(self):
        """Test GitHubAPI initialization."""
        self.assertEqual(self.api.token, "test_token")
        self.assertEqual(self.api.owner, "test_owner")
        self.assertEqual(self.api.repo, "test_repo")
        self.assertEqual(self.api.graphql_url, "https://api.github.com/graphql")
        self.assertEqual(self.api.rest_url, "https://api.github.com")
        self.assertEqual(self.api.headers["Authorization"], "token test_token")
        self.assertEqual(self.api.headers["Accept"], "application/vnd.github.v3+json")
        self.assertEqual(self.api.headers["User-Agent"], "copilot-retrigger-script")
    
    @patch('copilot_retrigger.requests.post')
    def test_fetch_all_pr_data_success(self, mock_post):
        """Test successful GraphQL PR data retrieval."""
        mock_response = Mock()
        mock_response.raise_for_status.return_value = None
        mock_response.json.return_value = {
            "data": {
                "repository": {
                    "pullRequests": {
                        "nodes": [
                            {
                                "number": 1,
                                "title": "Test PR",
                                "mergeable": "MERGEABLE",
                                "author": {"login": "test_user"},
                                "assignees": {"nodes": []},
                                "headRef": {
                                    "target": {
                                        "oid": "abc123",
                                        "checkSuites": {"nodes": []},
                                        "statusCheckRollup": {"state": "SUCCESS"}
                                    }
                                },
                                "comments": {"nodes": []}
                            }
                        ]
                    }
                }
            }
        }
        mock_post.return_value = mock_response
        
        result = self.api.fetch_all_pr_data()
        
        self.assertEqual(len(result), 1)
        self.assertEqual(result[0]["number"], 1)
        self.assertEqual(result[0]["title"], "Test PR")
        
        # Verify GraphQL query was called correctly
        mock_post.assert_called_once_with(
            "https://api.github.com/graphql",
            headers=self.api.headers,
            json={
                "query": unittest.mock.ANY,  # GraphQL query is complex, just check it's present
                "variables": {"owner": "test_owner", "repo": "test_repo"}
            }
        )
    
    @patch('copilot_retrigger.requests.post')
    def test_fetch_all_pr_data_with_errors(self, mock_post):
        """Test GraphQL PR data retrieval with errors."""
        mock_response = Mock()
        mock_response.raise_for_status.return_value = None
        mock_response.json.return_value = {
            "errors": [{"message": "GraphQL error"}]
        }
        mock_post.return_value = mock_response
        
        with self.assertRaises(Exception) as context:
            self.api.fetch_all_pr_data()
        
        self.assertIn("GraphQL errors", str(context.exception))
    
    @patch('copilot_retrigger.requests.post')
    def test_add_comment_success(self, mock_post):
        """Test successful comment addition using REST API."""
        mock_response = Mock()
        mock_response.raise_for_status.return_value = None
        mock_response.json.return_value = {"id": 1, "body": "Test comment"}
        mock_post.return_value = mock_response
        
        result = self.api.add_comment(123, "Test comment")
        
        self.assertEqual(result, {"id": 1, "body": "Test comment"})
        mock_post.assert_called_once_with(
            "https://api.github.com/repos/test_owner/test_repo/issues/123/comments",
            headers=self.api.headers,
            json={"body": "Test comment"}
        )
    
    @patch('copilot_retrigger.requests.post')
    def test_fetch_all_pr_data_http_error(self, mock_post):
        """Test GraphQL PR data retrieval with HTTP error."""
        mock_response = Mock()
        mock_response.raise_for_status.side_effect = requests.exceptions.HTTPError("HTTP Error")
        mock_post.return_value = mock_response
        
        with self.assertRaises(requests.exceptions.HTTPError):
            self.api.fetch_all_pr_data()
    
    @patch('copilot_retrigger.requests.post')
    def test_add_comment_http_error(self, mock_post):
        """Test comment addition with HTTP error."""
        mock_response = Mock()
        mock_response.raise_for_status.side_effect = requests.exceptions.HTTPError("HTTP Error")
        mock_post.return_value = mock_response
        
        with self.assertRaises(requests.exceptions.HTTPError):
            self.api.add_comment(123, "Test comment")
    
    @patch('copilot_retrigger.requests.post')
    def test_approve_workflow_run_success(self, mock_post):
        """Test successful workflow run approval."""
        mock_response = Mock()
        mock_response.raise_for_status.return_value = None
        mock_response.content = b''
        mock_post.return_value = mock_response
        
        result = self.api.approve_workflow_run(12345)
        
        self.assertEqual(result, {})
        mock_post.assert_called_once_with(
            "https://api.github.com/repos/test_owner/test_repo/actions/runs/12345/approve",
            headers=self.api.headers
        )
    
    @patch('copilot_retrigger.requests.post')
    def test_approve_workflow_run_with_content(self, mock_post):
        """Test workflow run approval with response content."""
        mock_response = Mock()
        mock_response.raise_for_status.return_value = None
        mock_response.content = b'{"status": "approved"}'
        mock_response.json.return_value = {"status": "approved"}
        mock_post.return_value = mock_response
        
        result = self.api.approve_workflow_run(12345)
        
        self.assertEqual(result, {"status": "approved"})
    
    @patch('copilot_retrigger.requests.post')
    def test_approve_workflow_run_http_error(self, mock_post):
        """Test workflow run approval with HTTP error."""
        mock_response = Mock()
        mock_response.raise_for_status.side_effect = requests.exceptions.HTTPError("HTTP Error")
        mock_post.return_value = mock_response
        
        with self.assertRaises(requests.exceptions.HTTPError):
            self.api.approve_workflow_run(12345)
    
    @patch('copilot_retrigger.requests.get')
    def test_get_workflow_runs_for_pr_success(self, mock_get):
        """Test successful workflow runs retrieval for PR."""
        mock_response = Mock()
        mock_response.raise_for_status.return_value = None
        mock_response.json.return_value = {
            "workflow_runs": [
                {
                    "id": 1,
                    "status": "waiting",
                    "pull_requests": [{"number": 123}]
                },
                {
                    "id": 2,
                    "status": "waiting",
                    "pull_requests": [{"number": 456}]
                },
                {
                    "id": 3,
                    "status": "waiting",
                    "pull_requests": [{"number": 123}]
                }
            ]
        }
        mock_get.return_value = mock_response
        
        result = self.api.get_workflow_runs_for_pr(123)
        
        self.assertEqual(len(result), 2)
        self.assertEqual(result[0]["id"], 1)
        self.assertEqual(result[1]["id"], 3)
        mock_get.assert_called_once_with(
            "https://api.github.com/repos/test_owner/test_repo/actions/runs",
            headers=self.api.headers,
            params={
                "event": "pull_request",
                "status": "waiting",
                "per_page": 100
            }
        )
    
    @patch('copilot_retrigger.requests.get')
    def test_get_workflow_runs_for_pr_empty_result(self, mock_get):
        """Test workflow runs retrieval when no runs match the PR."""
        mock_response = Mock()
        mock_response.raise_for_status.return_value = None
        mock_response.json.return_value = {
            "workflow_runs": [
                {
                    "id": 1,
                    "status": "waiting",
                    "pull_requests": [{"number": 456}]
                }
            ]
        }
        mock_get.return_value = mock_response
        
        result = self.api.get_workflow_runs_for_pr(123)
        
        self.assertEqual(len(result), 0)
    
    @patch('copilot_retrigger.requests.get')
    def test_get_workflow_runs_for_pr_http_error(self, mock_get):
        """Test workflow runs retrieval with HTTP error."""
        mock_response = Mock()
        mock_response.raise_for_status.side_effect = requests.exceptions.HTTPError("HTTP Error")
        mock_get.return_value = mock_response
        
        with self.assertRaises(requests.exceptions.HTTPError):
            self.api.get_workflow_runs_for_pr(123)


class TestCopilotRetrigger(unittest.TestCase):
    """Test cases for the CopilotRetrigger class."""
    
    def setUp(self):
        self.mock_api = Mock(spec=GitHubAPI)
        self.retrigger = CopilotRetrigger(self.mock_api)
        self.retrigger_dry_run = CopilotRetrigger(self.mock_api, dry_run=True)
    
    def test_init(self):
        """Test CopilotRetrigger initialization."""
        self.assertEqual(self.retrigger.github_api, self.mock_api)
        self.assertFalse(self.retrigger.dry_run)
        self.assertEqual(self.retrigger.MERGE_CONFLICT_COMMENT, 
                         "@copilot can you update the branch and fix the conflicts?")
        self.assertEqual(self.retrigger.FAILING_CHECKS_COMMENT,
                         "@copilot can you fix the failing github actions?")
        self.assertEqual(self.retrigger.MAX_COMMENTS, 3)
        self.assertIn("copilot", self.retrigger.COPILOT_USERS)
        self.assertIn("github-copilot[bot]", self.retrigger.COPILOT_USERS)
    
    def test_is_copilot_pr_author_copilot(self):
        """Test PR identification when author is copilot."""
        pr = {
            "author": {"login": "copilot"},
            "assignees": {"nodes": []}
        }
        
        result = self.retrigger.is_copilot_pr(pr)
        
        self.assertTrue(result)
    
    def test_is_copilot_pr_author_github_copilot_bot(self):
        """Test PR identification when author is github-copilot[bot]."""
        pr = {
            "author": {"login": "github-copilot[bot]"},
            "assignees": {"nodes": []}
        }
        
        result = self.retrigger.is_copilot_pr(pr)
        
        self.assertTrue(result)
    
    def test_is_copilot_pr_assignee_copilot(self):
        """Test PR identification when assignee is copilot."""
        pr = {
            "author": {"login": "regular_user"},
            "assignees": {"nodes": [{"login": "copilot"}]}
        }
        
        result = self.retrigger.is_copilot_pr(pr)
        
        self.assertTrue(result)
    
    def test_is_copilot_pr_partial_match_in_author(self):
        """Test PR identification with partial copilot match in author."""
        pr = {
            "author": {"login": "test-copilot-user"},
            "assignees": {"nodes": []}
        }
        
        result = self.retrigger.is_copilot_pr(pr)
        
        self.assertTrue(result)
    
    def test_is_copilot_pr_not_copilot(self):
        """Test PR identification when neither author nor assignee is copilot."""
        pr = {
            "author": {"login": "regular_user"},
            "assignees": {"nodes": [{"login": "another_user"}]}
        }
        
        result = self.retrigger.is_copilot_pr(pr)
        
        self.assertFalse(result)
    
    def test_is_copilot_pr_missing_fields(self):
        """Test PR identification with missing fields."""
        pr = {}
        
        result = self.retrigger.is_copilot_pr(pr)
        
        self.assertFalse(result)
    
    def test_is_copilot_pr_null_author(self):
        """Test PR identification with null author."""
        pr = {
            "author": None,
            "assignees": {"nodes": []}
        }
        
        result = self.retrigger.is_copilot_pr(pr)
        
        self.assertFalse(result)
    
    def test_has_merge_conflicts_true(self):
        """Test merge conflict detection when conflicts exist."""
        pr = {"mergeable": "CONFLICTING"}
        
        result = self.retrigger.has_merge_conflicts(pr)
        
        self.assertTrue(result)
    
    def test_has_merge_conflicts_false(self):
        """Test merge conflict detection when no conflicts exist."""
        pr = {"mergeable": "MERGEABLE"}
        
        result = self.retrigger.has_merge_conflicts(pr)
        
        self.assertFalse(result)
    
    def test_has_merge_conflicts_unknown(self):
        """Test merge conflict detection when mergeable status is unknown."""
        pr = {"mergeable": "UNKNOWN"}
        
        result = self.retrigger.has_merge_conflicts(pr)
        
        self.assertFalse(result)
    
    def test_has_failing_checks_true_via_rollup(self):
        """Test failing check detection via status rollup when checks are failing."""
        pr = {
            "headRef": {
                "target": {
                    "statusCheckRollup": {"state": "FAILURE"},
                    "checkSuites": {"nodes": []}
                }
            }
        }
        
        result = self.retrigger.has_failing_checks(pr)
        
        self.assertTrue(result)
    
    def test_has_failing_checks_true_via_individual_checks(self):
        """Test failing check detection via individual check runs."""
        pr = {
            "headRef": {
                "target": {
                    "statusCheckRollup": {"state": "SUCCESS"},
                    "checkSuites": {
                        "nodes": [
                            {
                                "checkRuns": {
                                    "nodes": [
                                        {"conclusion": "SUCCESS"},
                                        {"conclusion": "FAILURE"}
                                    ]
                                }
                            }
                        ]
                    }
                }
            }
        }
        
        result = self.retrigger.has_failing_checks(pr)
        
        self.assertTrue(result)
    
    def test_has_failing_checks_false(self):
        """Test failing check detection when all checks pass."""
        pr = {
            "headRef": {
                "target": {
                    "statusCheckRollup": {"state": "SUCCESS"},
                    "checkSuites": {
                        "nodes": [
                            {
                                "checkRuns": {
                                    "nodes": [
                                        {"conclusion": "SUCCESS"},
                                        {"conclusion": "SUCCESS"}
                                    ]
                                }
                            }
                        ]
                    }
                }
            }
        }
        
        result = self.retrigger.has_failing_checks(pr)
        
        self.assertFalse(result)
    
    def test_has_failing_checks_no_head_ref(self):
        """Test failing check detection when headRef is missing."""
        pr = {}
        
        result = self.retrigger.has_failing_checks(pr)
        
        self.assertFalse(result)
    
    def test_count_retrigger_comments_merge_conflict(self):
        """Test counting retrigger comments with merge conflict comments."""
        pr = {
            "comments": {
                "nodes": [
                    {"body": "Regular comment"},
                    {"body": "@copilot can you update the branch and fix the conflicts?"},
                    {"body": "Another regular comment"},
                    {"body": "@copilot can you update the branch and fix the conflicts?"}
                ]
            }
        }
        
        result = self.retrigger.count_retrigger_comments(pr)
        
        self.assertEqual(result, 2)
    
    def test_count_retrigger_comments_failing_checks(self):
        """Test counting retrigger comments with failing check comments."""
        pr = {
            "comments": {
                "nodes": [
                    {"body": "Regular comment"},
                    {"body": "@copilot can you fix the failing github actions?"},
                    {"body": "Another regular comment"}
                ]
            }
        }
        
        result = self.retrigger.count_retrigger_comments(pr)
        
        self.assertEqual(result, 1)
    
    def test_count_retrigger_comments_mixed(self):
        """Test counting retrigger comments with mixed comment types."""
        pr = {
            "comments": {
                "nodes": [
                    {"body": "@copilot can you update the branch and fix the conflicts?"},
                    {"body": "@copilot can you fix the failing github actions?"},
                    {"body": "Regular comment"}
                ]
            }
        }
        
        result = self.retrigger.count_retrigger_comments(pr)
        
        self.assertEqual(result, 2)
    
    def test_count_retrigger_comments_none(self):
        """Test counting retrigger comments when none exist."""
        pr = {
            "comments": {
                "nodes": [
                    {"body": "Regular comment"},
                    {"body": "Another regular comment"}
                ]
            }
        }
        
        result = self.retrigger.count_retrigger_comments(pr)
        
        self.assertEqual(result, 0)
    
    @patch('builtins.print')
    def test_process_pr_not_copilot(self, mock_print):
        """Test processing a PR that is not a copilot PR."""
        pr = {
            "number": 123,
            "title": "Test PR",
            "author": {"login": "regular_user"},
            "assignees": {"nodes": []}
        }
        
        self.retrigger.process_pr(pr)
        
        mock_print.assert_any_call("Processing PR #123: Test PR")
        mock_print.assert_any_call("  Skipping - not a copilot PR")
    
    @patch('builtins.print')
    def test_process_pr_max_comments_reached(self, mock_print):
        """Test processing a PR that has reached the maximum comment limit."""
        pr = {
            "number": 123,
            "title": "Test PR",
            "author": {"login": "copilot"},
            "assignees": {"nodes": []},
            "comments": {
                "nodes": [
                    {"body": "@copilot can you update the branch and fix the conflicts?"},
                    {"body": "@copilot can you fix the failing github actions?"},
                    {"body": "@copilot can you update the branch and fix the conflicts?"}
                ]
            }
        }
        
        self.retrigger.process_pr(pr)
        
        mock_print.assert_any_call("Processing PR #123: Test PR")
        mock_print.assert_any_call("  Skipping - already added 3 retrigger comments")
    
    @patch('builtins.print')
    def test_process_pr_merge_conflicts(self, mock_print):
        """Test processing a PR with merge conflicts."""
        pr = {
            "number": 123,
            "title": "Test PR",
            "author": {"login": "copilot"},
            "assignees": {"nodes": []},
            "comments": {"nodes": []},
            "mergeable": "CONFLICTING"
        }
        
        self.retrigger.process_pr(pr)
        
        mock_print.assert_any_call("Processing PR #123: Test PR")
        mock_print.assert_any_call("  Found merge conflicts, adding comment")
        self.mock_api.add_comment.assert_called_once_with(
            123, "@copilot can you update the branch and fix the conflicts?"
        )
    
    @patch('builtins.print')
    def test_process_pr_failing_checks(self, mock_print):
        """Test processing a PR with failing checks."""
        pr = {
            "number": 123,
            "title": "Test PR",
            "author": {"login": "copilot"},
            "assignees": {"nodes": []},
            "comments": {"nodes": []},
            "mergeable": "MERGEABLE",
            "headRef": {
                "target": {
                    "statusCheckRollup": {"state": "FAILURE"},
                    "checkSuites": {"nodes": []}
                }
            }
        }
        
        self.retrigger.process_pr(pr)
        
        mock_print.assert_any_call("Processing PR #123: Test PR")
        mock_print.assert_any_call("  Found failing checks, adding comment")
        self.mock_api.add_comment.assert_called_once_with(
            123, "@copilot can you fix the failing github actions?"
        )
    
    @patch('builtins.print')
    def test_process_pr_no_issues(self, mock_print):
        """Test processing a PR with no issues."""
        pr = {
            "number": 123,
            "title": "Test PR",
            "author": {"login": "copilot"},
            "assignees": {"nodes": []},
            "comments": {"nodes": []},
            "mergeable": "MERGEABLE",
            "headRef": {
                "target": {
                    "statusCheckRollup": {"state": "SUCCESS"},
                    "checkSuites": {"nodes": []}
                }
            }
        }
        
        self.retrigger.process_pr(pr)
        
        mock_print.assert_any_call("Processing PR #123: Test PR")
        mock_print.assert_any_call("  No issues found")
        self.mock_api.add_comment.assert_not_called()
    
    @patch('builtins.print')
    def test_process_pr_dry_run_merge_conflicts(self, mock_print):
        """Test processing PR with merge conflicts in dry run mode."""
        pr = {
            "number": 123,
            "title": "Test PR",
            "author": {"login": "copilot"},
            "assignees": {"nodes": []},
            "comments": {"nodes": []},
            "mergeable": "CONFLICTING"
        }
        
        self.retrigger_dry_run.process_pr(pr)
        
        mock_print.assert_any_call("Processing PR #123: Test PR")
        mock_print.assert_any_call("  [DRY RUN] Would add merge conflict comment: @copilot can you update the branch and fix the conflicts?")
        self.mock_api.add_comment.assert_not_called()
    
    @patch('builtins.print')
    def test_process_pr_dry_run_failing_checks(self, mock_print):
        """Test processing PR with failing checks in dry run mode."""
        pr = {
            "number": 123,
            "title": "Test PR",
            "author": {"login": "copilot"},
            "assignees": {"nodes": []},
            "comments": {"nodes": []},
            "mergeable": "MERGEABLE",
            "headRef": {
                "target": {
                    "statusCheckRollup": {"state": "FAILURE"},
                    "checkSuites": {"nodes": []}
                }
            }
        }
        
        self.retrigger_dry_run.process_pr(pr)
        
        mock_print.assert_any_call("Processing PR #123: Test PR")
        mock_print.assert_any_call("  [DRY RUN] Would add failing checks comment: @copilot can you fix the failing github actions?")
        self.mock_api.add_comment.assert_not_called()
    
    @patch('builtins.print')
    def test_run_dry_run(self, mock_print):
        """Test dry run mode shows appropriate messages."""
        prs = [
            {
                "number": 123,
                "title": "Test PR",
                "author": {"login": "copilot"},
                "assignees": {"nodes": []},
                "comments": {"nodes": []},
                "mergeable": "MERGEABLE",
                "headRef": {
                    "target": {
                        "statusCheckRollup": {"state": "SUCCESS"},
                        "checkSuites": {"nodes": []}
                    }
                }
            }
        ]
        
        self.mock_api.fetch_all_pr_data.return_value = prs
        
        self.retrigger_dry_run.run()
        
        # Check that dry run messages are printed
        printed_calls = [call.args[0] for call in mock_print.call_args_list]
        self.assertTrue(any("[DRY RUN]" in call for call in printed_calls))
        self.mock_api.add_comment.assert_not_called()
    
    def test_init_with_dry_run(self):
        """Test CopilotRetrigger initialization with dry run enabled."""
        retrigger = CopilotRetrigger(self.mock_api, dry_run=True)
        self.assertTrue(retrigger.dry_run)
        
        retrigger_no_dry_run = CopilotRetrigger(self.mock_api, dry_run=False)
        self.assertFalse(retrigger_no_dry_run.dry_run)
        
        retrigger_default = CopilotRetrigger(self.mock_api)
        self.assertFalse(retrigger_default.dry_run)
    
    @patch('builtins.print')
    def test_run_success(self, mock_print):
        """Test successful run of the retrigger process."""
        prs = [
            {
                "number": 123,
                "title": "Test PR 1",
                "author": {"login": "copilot"},
                "assignees": {"nodes": []},
                "comments": {"nodes": []},
                "mergeable": "MERGEABLE",
                "headRef": {
                    "target": {
                        "statusCheckRollup": {"state": "SUCCESS"},
                        "checkSuites": {"nodes": []}
                    }
                }
            },
            {
                "number": 124,
                "title": "Test PR 2",
                "author": {"login": "regular_user"},
                "assignees": {"nodes": []}
            }
        ]
        
        self.mock_api.fetch_all_pr_data.return_value = prs
        
        self.retrigger.run()
        
        # Check that start and completion messages are printed
        mock_print.assert_any_call("Found 2 open pull requests")
        mock_print.assert_any_call("Copilot retrigger check completed")
        self.mock_api.fetch_all_pr_data.assert_called_once()
    
    @patch('builtins.print')
    def test_run_with_exception(self, mock_print):
        """Test run with exception during PR processing."""
        prs = [
            {
                "number": 123,
                "title": "Test PR 1",
                "author": {"login": "copilot"},
                "assignees": {"nodes": []}
            }
        ]
        
        self.mock_api.fetch_all_pr_data.return_value = prs
        
        # Make the is_copilot_pr method throw an exception to simulate error
        with patch.object(self.retrigger, 'is_copilot_pr', side_effect=Exception("API Error")):
            self.retrigger.run()
        
        mock_print.assert_any_call("Error processing PR #123: API Error")
        mock_print.assert_any_call("Copilot retrigger check completed")
    
    def test_has_workflow_runs_waiting_approval_with_waiting_runs(self):
        """Test detection of workflow runs waiting for approval."""
        pr = {"number": 123}
        
        # Mock the API to return waiting workflow runs
        self.mock_api.get_workflow_runs_for_pr.return_value = [
            {"id": 1, "status": "waiting"},
            {"id": 2, "status": "completed"},
            {"id": 3, "status": "waiting"}
        ]
        
        result = self.retrigger.has_workflow_runs_waiting_approval(pr)
        
        self.assertEqual(result, [1, 3])
        self.mock_api.get_workflow_runs_for_pr.assert_called_once_with(123)
    
    def test_has_workflow_runs_waiting_approval_no_waiting_runs(self):
        """Test detection when no workflow runs are waiting."""
        pr = {"number": 123}
        
        # Mock the API to return no waiting workflow runs
        self.mock_api.get_workflow_runs_for_pr.return_value = [
            {"id": 1, "status": "completed"},
            {"id": 2, "status": "completed"}
        ]
        
        result = self.retrigger.has_workflow_runs_waiting_approval(pr)
        
        self.assertEqual(result, [])
    
    def test_has_workflow_runs_waiting_approval_empty_result(self):
        """Test detection when no workflow runs exist."""
        pr = {"number": 123}
        
        # Mock the API to return empty list
        self.mock_api.get_workflow_runs_for_pr.return_value = []
        
        result = self.retrigger.has_workflow_runs_waiting_approval(pr)
        
        self.assertEqual(result, [])
    
    @patch('builtins.print')
    def test_has_workflow_runs_waiting_approval_with_exception(self, mock_print):
        """Test detection with API exception."""
        pr = {"number": 123}
        
        # Mock the API to raise an exception
        self.mock_api.get_workflow_runs_for_pr.side_effect = Exception("API Error")
        
        result = self.retrigger.has_workflow_runs_waiting_approval(pr)
        
        self.assertEqual(result, [])
        mock_print.assert_called_with("    Error checking workflow runs: API Error")
    
    @patch('builtins.print')
    def test_approve_waiting_workflows_with_waiting_runs(self, mock_print):
        """Test approving workflow runs that are waiting."""
        pr = {"number": 123}
        
        # Mock workflow runs waiting for approval
        with patch.object(self.retrigger, 'has_workflow_runs_waiting_approval', return_value=[1, 2]):
            self.retrigger.approve_waiting_workflows(pr)
        
        # Verify approve_workflow_run was called for both runs
        self.assertEqual(self.mock_api.approve_workflow_run.call_count, 2)
        self.mock_api.approve_workflow_run.assert_any_call(1)
        self.mock_api.approve_workflow_run.assert_any_call(2)
        
        mock_print.assert_any_call("  Approved workflow run 1")
        mock_print.assert_any_call("  Approved workflow run 2")
        mock_print.assert_any_call("  Successfully approved 2 workflow runs")
    
    @patch('builtins.print')
    def test_approve_waiting_workflows_no_waiting_runs(self, mock_print):
        """Test approving when no runs are waiting."""
        pr = {"number": 123}
        
        # Mock no workflow runs waiting for approval
        with patch.object(self.retrigger, 'has_workflow_runs_waiting_approval', return_value=[]):
            self.retrigger.approve_waiting_workflows(pr)
        
        # Verify approve_workflow_run was not called
        self.mock_api.approve_workflow_run.assert_not_called()
        
        # Should not print any approval messages
        approval_calls = [call for call in mock_print.call_args_list if "Approved" in str(call)]
        self.assertEqual(len(approval_calls), 0)
    
    @patch('builtins.print')
    def test_approve_waiting_workflows_dry_run(self, mock_print):
        """Test approving workflow runs in dry run mode."""
        pr = {"number": 123}
        
        # Mock workflow runs waiting for approval
        with patch.object(self.retrigger_dry_run, 'has_workflow_runs_waiting_approval', return_value=[1, 2]):
            self.retrigger_dry_run.approve_waiting_workflows(pr)
        
        # Verify approve_workflow_run was not called in dry run
        self.mock_api.approve_workflow_run.assert_not_called()
        
        mock_print.assert_any_call("  [DRY RUN] Would approve 2 waiting workflow runs: [1, 2]")
    
    @patch('builtins.print')
    def test_approve_waiting_workflows_with_exception(self, mock_print):
        """Test approving workflow runs with API exception."""
        pr = {"number": 123}
        
        # Mock workflow runs waiting for approval
        with patch.object(self.retrigger, 'has_workflow_runs_waiting_approval', return_value=[1, 2]):
            # Mock approve_workflow_run to fail for the first run but succeed for second
            self.mock_api.approve_workflow_run.side_effect = [
                Exception("API Error"),
                None  # Success for second call
            ]
            
            self.retrigger.approve_waiting_workflows(pr)
        
        mock_print.assert_any_call("  Error approving workflow run 1: API Error")
        mock_print.assert_any_call("  Approved workflow run 2")
        mock_print.assert_any_call("  Successfully approved 1 workflow runs")
    
    @patch('builtins.print')
    def test_process_pr_with_workflow_approval(self, mock_print):
        """Test processing a PR that includes workflow approval."""
        pr = {
            "number": 123,
            "title": "Test PR",
            "author": {"login": "copilot"},
            "assignees": {"nodes": []},
            "comments": {"nodes": []},
            "mergeable": "MERGEABLE",
            "headRef": {
                "target": {
                    "statusCheckRollup": {"state": "SUCCESS"},
                    "checkSuites": {"nodes": []}
                }
            }
        }
        
        # Mock that there are workflow runs to approve
        with patch.object(self.retrigger, 'approve_waiting_workflows') as mock_approve:
            self.retrigger.process_pr(pr)
            mock_approve.assert_called_once_with(pr)
        
        mock_print.assert_any_call("Processing PR #123: Test PR")
        mock_print.assert_any_call("  No issues found")


class TestMainFunction(unittest.TestCase):
    """Test cases for the main function and script entry point."""
    
    @patch('sys.argv', ['copilot_retrigger.py'])
    @patch('os.getenv')
    @patch('copilot_retrigger.CopilotRetrigger')
    @patch('copilot_retrigger.GitHubAPI')
    def test_main_success(self, mock_github_api, mock_retrigger, mock_getenv):
        """Test successful main function execution."""
        # Mock os.getenv to return test values
        def getenv_side_effect(key, default=None):
            if key == "GITHUB_TOKEN":
                return "test_token"
            elif key == "GITHUB_REPOSITORY":
                return "owner/repo"
            return default
        
        mock_getenv.side_effect = getenv_side_effect
        
        mock_api_instance = Mock()
        mock_github_api.return_value = mock_api_instance
        mock_retrigger_instance = Mock()
        mock_retrigger.return_value = mock_retrigger_instance
        
        # Import and call main
        import copilot_retrigger
        copilot_retrigger.main()
        
        mock_github_api.assert_called_once_with("test_token", "owner", "repo")
        mock_retrigger.assert_called_once_with(mock_api_instance, dry_run=False)
        mock_retrigger_instance.run.assert_called_once()
    
    @patch('sys.argv', ['copilot_retrigger.py'])
    @patch('os.getenv')
    @patch('copilot_retrigger.CopilotRetrigger')
    @patch('copilot_retrigger.GitHubAPI')
    def test_main_default_repository(self, mock_github_api, mock_retrigger, mock_getenv):
        """Test main function with default repository."""
        # Mock os.getenv to return test values
        def getenv_side_effect(key, default=None):
            if key == "GITHUB_TOKEN":
                return "test_token"
            elif key == "GITHUB_REPOSITORY":
                return default  # Return the default value to test default behavior
            return default
        
        mock_getenv.side_effect = getenv_side_effect
        
        mock_api_instance = Mock()
        mock_github_api.return_value = mock_api_instance
        mock_retrigger_instance = Mock()
        mock_retrigger.return_value = mock_retrigger_instance
        
        import copilot_retrigger
        copilot_retrigger.main()
        
        mock_github_api.assert_called_once_with("test_token", "violinyanev", "my-kitchen")
    
    @patch('sys.argv', ['copilot_retrigger.py', '--dry-run'])
    @patch('os.getenv')
    @patch('copilot_retrigger.CopilotRetrigger')
    @patch('copilot_retrigger.GitHubAPI')
    def test_main_dry_run(self, mock_github_api, mock_retrigger, mock_getenv):
        """Test main function with dry run flag."""
        # Mock os.getenv to return test values
        def getenv_side_effect(key, default=None):
            if key == "GITHUB_TOKEN":
                return "test_token"
            elif key == "GITHUB_REPOSITORY":
                return "owner/repo"
            return default
        
        mock_getenv.side_effect = getenv_side_effect
        
        mock_api_instance = Mock()
        mock_github_api.return_value = mock_api_instance
        mock_retrigger_instance = Mock()
        mock_retrigger.return_value = mock_retrigger_instance
        
        # Import and call main
        import copilot_retrigger
        copilot_retrigger.main()
        
        mock_github_api.assert_called_once_with("test_token", "owner", "repo")
        mock_retrigger.assert_called_once_with(mock_api_instance, dry_run=True)
        mock_retrigger_instance.run.assert_called_once()
    
    @patch('sys.argv', ['copilot_retrigger.py'])
    @patch('os.getenv')
    @patch('copilot_retrigger.CopilotRetrigger')
    @patch('copilot_retrigger.GitHubAPI')
    def test_main_no_dry_run(self, mock_github_api, mock_retrigger, mock_getenv):
        """Test main function without dry run flag."""
        # Mock os.getenv to return test values
        def getenv_side_effect(key, default=None):
            if key == "GITHUB_TOKEN":
                return "test_token"
            elif key == "GITHUB_REPOSITORY":
                return "owner/repo"
            return default
        
        mock_getenv.side_effect = getenv_side_effect
        
        mock_api_instance = Mock()
        mock_github_api.return_value = mock_api_instance
        mock_retrigger_instance = Mock()
        mock_retrigger.return_value = mock_retrigger_instance
        
        # Import and call main
        import copilot_retrigger
        copilot_retrigger.main()
        
        mock_github_api.assert_called_once_with("test_token", "owner", "repo")
        mock_retrigger.assert_called_once_with(mock_api_instance, dry_run=False)
        mock_retrigger_instance.run.assert_called_once()
    
    @patch('sys.argv', ['copilot_retrigger.py'])
    @patch('os.getenv')
    @patch('copilot_retrigger.CopilotRetrigger')
    @patch('copilot_retrigger.GitHubAPI')
    @patch('builtins.print')
    @patch('sys.exit')
    def test_main_exception(self, mock_exit, mock_print, mock_github_api, mock_retrigger, mock_getenv):
        """Test main function with exception during execution."""
        # Mock os.getenv to return test values
        def getenv_side_effect(key, default=None):
            if key == "GITHUB_TOKEN":
                return "test_token"
            elif key == "GITHUB_REPOSITORY":
                return "owner/repo"
            return default
        
        mock_getenv.side_effect = getenv_side_effect
        
        mock_api_instance = Mock()
        mock_github_api.return_value = mock_api_instance
        mock_retrigger_instance = Mock()
        mock_retrigger_instance.run.side_effect = Exception("Test error")
        mock_retrigger.return_value = mock_retrigger_instance
        
        import copilot_retrigger
        copilot_retrigger.main()
        
        mock_print.assert_called_with("Error running copilot retrigger: Test error")
        mock_exit.assert_called_with(1)


if __name__ == '__main__':
    unittest.main()
