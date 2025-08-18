#!/usr/bin/env python3

import unittest
from unittest.mock import Mock, patch, MagicMock
import os
import sys
import json
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
        self.assertEqual(self.api.base_url, "https://api.github.com")
        self.assertEqual(self.api.headers["Authorization"], "token test_token")
        self.assertEqual(self.api.headers["Accept"], "application/vnd.github.v3+json")
        self.assertEqual(self.api.headers["User-Agent"], "copilot-retrigger-script")
    
    @patch('copilot_retrigger.requests.get')
    def test_get_pull_requests_success(self, mock_get):
        """Test successful pull request retrieval."""
        mock_response = Mock()
        mock_response.raise_for_status.return_value = None
        mock_response.json.return_value = [{"number": 1, "title": "Test PR"}]
        mock_get.return_value = mock_response
        
        result = self.api.get_pull_requests()
        
        self.assertEqual(result, [{"number": 1, "title": "Test PR"}])
        mock_get.assert_called_once_with(
            "https://api.github.com/repos/test_owner/test_repo/pulls",
            headers=self.api.headers,
            params={"state": "open", "per_page": 100}
        )
    
    @patch('copilot_retrigger.requests.get')
    def test_get_pull_requests_with_state(self, mock_get):
        """Test pull request retrieval with specific state."""
        mock_response = Mock()
        mock_response.raise_for_status.return_value = None
        mock_response.json.return_value = []
        mock_get.return_value = mock_response
        
        self.api.get_pull_requests(state="closed")
        
        mock_get.assert_called_once_with(
            "https://api.github.com/repos/test_owner/test_repo/pulls",
            headers=self.api.headers,
            params={"state": "closed", "per_page": 100}
        )
    
    @patch('copilot_retrigger.requests.get')
    def test_get_pull_request_comments_success(self, mock_get):
        """Test successful comment retrieval."""
        mock_response = Mock()
        mock_response.raise_for_status.return_value = None
        mock_response.json.return_value = [{"id": 1, "body": "Test comment"}]
        mock_get.return_value = mock_response
        
        result = self.api.get_pull_request_comments(123)
        
        self.assertEqual(result, [{"id": 1, "body": "Test comment"}])
        mock_get.assert_called_once_with(
            "https://api.github.com/repos/test_owner/test_repo/issues/123/comments",
            headers=self.api.headers
        )
    
    @patch('copilot_retrigger.requests.post')
    def test_add_comment_success(self, mock_post):
        """Test successful comment addition."""
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
    
    @patch('copilot_retrigger.requests.get')
    def test_get_pr_status_success(self, mock_get):
        """Test successful PR status retrieval."""
        mock_response = Mock()
        mock_response.raise_for_status.return_value = None
        mock_response.json.return_value = {"number": 123, "mergeable": True}
        mock_get.return_value = mock_response
        
        result = self.api.get_pr_status(123)
        
        self.assertEqual(result, {"number": 123, "mergeable": True})
        mock_get.assert_called_once_with(
            "https://api.github.com/repos/test_owner/test_repo/pulls/123",
            headers=self.api.headers
        )
    
    @patch('copilot_retrigger.requests.get')
    def test_get_check_runs_success(self, mock_get):
        """Test successful check runs retrieval."""
        mock_response = Mock()
        mock_response.raise_for_status.return_value = None
        mock_response.json.return_value = {
            "check_runs": [{"id": 1, "conclusion": "success"}]
        }
        mock_get.return_value = mock_response
        
        result = self.api.get_check_runs("abc123")
        
        self.assertEqual(result, [{"id": 1, "conclusion": "success"}])
        mock_get.assert_called_once_with(
            "https://api.github.com/repos/test_owner/test_repo/commits/abc123/check-runs",
            headers=self.api.headers
        )
    
    @patch('copilot_retrigger.requests.get')
    def test_get_check_runs_no_check_runs_key(self, mock_get):
        """Test check runs retrieval when response has no check_runs key."""
        mock_response = Mock()
        mock_response.raise_for_status.return_value = None
        mock_response.json.return_value = {}
        mock_get.return_value = mock_response
        
        result = self.api.get_check_runs("abc123")
        
        self.assertEqual(result, [])


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
            "user": {"login": "copilot"},
            "assignees": []
        }
        
        result = self.retrigger.is_copilot_pr(pr)
        
        self.assertTrue(result)
    
    def test_is_copilot_pr_author_github_copilot_bot(self):
        """Test PR identification when author is github-copilot[bot]."""
        pr = {
            "user": {"login": "github-copilot[bot]"},
            "assignees": []
        }
        
        result = self.retrigger.is_copilot_pr(pr)
        
        self.assertTrue(result)
    
    def test_is_copilot_pr_assignee_copilot(self):
        """Test PR identification when assignee is copilot."""
        pr = {
            "user": {"login": "regular_user"},
            "assignees": [{"login": "copilot"}]
        }
        
        result = self.retrigger.is_copilot_pr(pr)
        
        self.assertTrue(result)
    
    def test_is_copilot_pr_partial_match_in_author(self):
        """Test PR identification with partial copilot match in author."""
        pr = {
            "user": {"login": "test-copilot-user"},
            "assignees": []
        }
        
        result = self.retrigger.is_copilot_pr(pr)
        
        self.assertTrue(result)
    
    def test_is_copilot_pr_not_copilot(self):
        """Test PR identification when neither author nor assignee is copilot."""
        pr = {
            "user": {"login": "regular_user"},
            "assignees": [{"login": "another_user"}]
        }
        
        result = self.retrigger.is_copilot_pr(pr)
        
        self.assertFalse(result)
    
    def test_is_copilot_pr_missing_fields(self):
        """Test PR identification with missing fields."""
        pr = {}
        
        result = self.retrigger.is_copilot_pr(pr)
        
        self.assertFalse(result)
    
    def test_has_merge_conflicts_true(self):
        """Test merge conflict detection when conflicts exist."""
        pr = {"number": 123}
        self.mock_api.get_pr_status.return_value = {"mergeable": False}
        
        result = self.retrigger.has_merge_conflicts(pr)
        
        self.assertTrue(result)
        self.mock_api.get_pr_status.assert_called_once_with(123)
    
    def test_has_merge_conflicts_false(self):
        """Test merge conflict detection when no conflicts exist."""
        pr = {"number": 123}
        self.mock_api.get_pr_status.return_value = {"mergeable": True}
        
        result = self.retrigger.has_merge_conflicts(pr)
        
        self.assertFalse(result)
    
    def test_has_merge_conflicts_none(self):
        """Test merge conflict detection when mergeable is None."""
        pr = {"number": 123}
        self.mock_api.get_pr_status.return_value = {"mergeable": None}
        
        result = self.retrigger.has_merge_conflicts(pr)
        
        self.assertFalse(result)
    
    def test_has_failing_checks_true(self):
        """Test failing check detection when checks are failing."""
        pr = {"head": {"sha": "abc123"}}
        self.mock_api.get_check_runs.return_value = [
            {"id": 1, "conclusion": "success"},
            {"id": 2, "conclusion": "failure"}
        ]
        
        result = self.retrigger.has_failing_checks(pr)
        
        self.assertTrue(result)
        self.mock_api.get_check_runs.assert_called_once_with("abc123")
    
    def test_has_failing_checks_false(self):
        """Test failing check detection when all checks pass."""
        pr = {"head": {"sha": "abc123"}}
        self.mock_api.get_check_runs.return_value = [
            {"id": 1, "conclusion": "success"},
            {"id": 2, "conclusion": "success"}
        ]
        
        result = self.retrigger.has_failing_checks(pr)
        
        self.assertFalse(result)
    
    def test_has_failing_checks_no_checks(self):
        """Test failing check detection when no checks exist."""
        pr = {"head": {"sha": "abc123"}}
        self.mock_api.get_check_runs.return_value = []
        
        result = self.retrigger.has_failing_checks(pr)
        
        self.assertFalse(result)
    
    def test_count_retrigger_comments_merge_conflict(self):
        """Test counting retrigger comments with merge conflict comments."""
        self.mock_api.get_pull_request_comments.return_value = [
            {"body": "Regular comment"},
            {"body": "@copilot can you update the branch and fix the conflicts?"},
            {"body": "Another regular comment"},
            {"body": "@copilot can you update the branch and fix the conflicts?"}
        ]
        
        result = self.retrigger.count_retrigger_comments(123)
        
        self.assertEqual(result, 2)
        self.mock_api.get_pull_request_comments.assert_called_once_with(123)
    
    def test_count_retrigger_comments_failing_checks(self):
        """Test counting retrigger comments with failing check comments."""
        self.mock_api.get_pull_request_comments.return_value = [
            {"body": "Regular comment"},
            {"body": "@copilot can you fix the failing github actions?"},
            {"body": "Another regular comment"}
        ]
        
        result = self.retrigger.count_retrigger_comments(123)
        
        self.assertEqual(result, 1)
    
    def test_count_retrigger_comments_mixed(self):
        """Test counting retrigger comments with mixed comment types."""
        self.mock_api.get_pull_request_comments.return_value = [
            {"body": "@copilot can you update the branch and fix the conflicts?"},
            {"body": "@copilot can you fix the failing github actions?"},
            {"body": "Regular comment"}
        ]
        
        result = self.retrigger.count_retrigger_comments(123)
        
        self.assertEqual(result, 2)
    
    def test_count_retrigger_comments_none(self):
        """Test counting retrigger comments when none exist."""
        self.mock_api.get_pull_request_comments.return_value = [
            {"body": "Regular comment"},
            {"body": "Another regular comment"}
        ]
        
        result = self.retrigger.count_retrigger_comments(123)
        
        self.assertEqual(result, 0)
    
    @patch('builtins.print')
    def test_process_pr_not_copilot(self, mock_print):
        """Test processing a PR that is not a copilot PR."""
        pr = {
            "number": 123,
            "title": "Test PR",
            "user": {"login": "regular_user"},
            "assignees": []
        }
        
        self.retrigger.process_pr(pr)
        
        mock_print.assert_any_call("Processing PR #123: Test PR")
        mock_print.assert_any_call("  Skipping - not a copilot PR")
        self.mock_api.get_pull_request_comments.assert_not_called()
    
    @patch('builtins.print')
    def test_process_pr_max_comments_reached(self, mock_print):
        """Test processing a PR that has reached the maximum comment limit."""
        pr = {
            "number": 123,
            "title": "Test PR",
            "user": {"login": "copilot"},
            "assignees": []
        }
        
        self.mock_api.get_pull_request_comments.return_value = [
            {"body": "@copilot can you update the branch and fix the conflicts?"},
            {"body": "@copilot can you fix the failing github actions?"},
            {"body": "@copilot can you update the branch and fix the conflicts?"}
        ]
        
        self.retrigger.process_pr(pr)
        
        mock_print.assert_any_call("Processing PR #123: Test PR")
        mock_print.assert_any_call("  Skipping - already added 3 retrigger comments")
        self.mock_api.get_pr_status.assert_not_called()
    
    @patch('builtins.print')
    def test_process_pr_merge_conflicts(self, mock_print):
        """Test processing a PR with merge conflicts."""
        pr = {
            "number": 123,
            "title": "Test PR",
            "user": {"login": "copilot"},
            "assignees": []
        }
        
        self.mock_api.get_pull_request_comments.return_value = []
        self.mock_api.get_pr_status.return_value = {"mergeable": False}
        
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
            "user": {"login": "copilot"},
            "assignees": [],
            "head": {"sha": "abc123"}
        }
        
        self.mock_api.get_pull_request_comments.return_value = []
        self.mock_api.get_pr_status.return_value = {"mergeable": True}
        self.mock_api.get_check_runs.return_value = [
            {"id": 1, "conclusion": "failure"}
        ]
        
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
            "user": {"login": "copilot"},
            "assignees": [],
            "head": {"sha": "abc123"}
        }
        
        self.mock_api.get_pull_request_comments.return_value = []
        self.mock_api.get_pr_status.return_value = {"mergeable": True}
        self.mock_api.get_check_runs.return_value = [
            {"id": 1, "conclusion": "success"}
        ]
        
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
            "user": {"login": "copilot"},
            "assignees": [],
            "head": {"sha": "abc123"}
        }
        
        self.mock_api.get_pull_request_comments.return_value = []
        self.mock_api.get_pr_status.return_value = {"mergeable": False}
        
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
            "user": {"login": "copilot"},
            "assignees": [],
            "head": {"sha": "abc123"}
        }
        
        self.mock_api.get_pull_request_comments.return_value = []
        self.mock_api.get_pr_status.return_value = {"mergeable": True}
        self.mock_api.get_check_runs.return_value = [{"conclusion": "failure"}]
        
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
                "user": {"login": "copilot"},
                "assignees": [],
                "head": {"sha": "abc123"}
            }
        ]
        
        self.mock_api.get_pull_requests.return_value = prs
        self.mock_api.get_pull_request_comments.return_value = []
        self.mock_api.get_pr_status.return_value = {"mergeable": True}
        self.mock_api.get_check_runs.return_value = []
        
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
                "user": {"login": "copilot"},
                "assignees": [],
                "head": {"sha": "abc123"}
            },
            {
                "number": 124,
                "title": "Test PR 2",
                "user": {"login": "regular_user"},
                "assignees": []
            }
        ]
        
        self.mock_api.get_pull_requests.return_value = prs
        self.mock_api.get_pull_request_comments.return_value = []
        self.mock_api.get_pr_status.return_value = {"mergeable": True}
        self.mock_api.get_check_runs.return_value = []
        
        self.retrigger.run()
        
        # Check that start and completion messages are printed
        mock_print.assert_any_call("Found 2 open pull requests")
        mock_print.assert_any_call("Copilot retrigger check completed")
        self.mock_api.get_pull_requests.assert_called_once()
    
    @patch('builtins.print')
    def test_run_with_exception(self, mock_print):
        """Test run with exception during PR processing."""
        prs = [
            {
                "number": 123,
                "title": "Test PR 1",
                "user": {"login": "copilot"},
                "assignees": []
            }
        ]
        
        self.mock_api.get_pull_requests.return_value = prs
        self.mock_api.get_pull_request_comments.side_effect = Exception("API Error")
        
        self.retrigger.run()
        
        mock_print.assert_any_call("Error processing PR #123: API Error")
        mock_print.assert_any_call("Copilot retrigger check completed")


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
