import re
import subprocess
import sys

# Conventional Commits regex
pattern = re.compile(
    r'^(feat|fix|chore|docs|style|refactor|perf|test|build|ci|revert)'
    r'(?:\([\w\-]+\))?:\s.+'
)

# Get commit range from env vars or default
commit_range = sys.argv[1] if len(sys.argv) > 1 else "HEAD~1..HEAD"

# Get commit messages
result = subprocess.run(
    ["git", "log", "--format=%s", commit_range],
    stdout=subprocess.PIPE,
    text=True
)

messages = result.stdout.strip().split("\n")

failed = False
for msg in messages:
    if not pattern.match(msg):
        print(f"❌ Commit message does not follow Conventional Commits: {msg}")
        failed = True

if failed:
    sys.exit(1)

print("✅ All commit messages follow Conventional Commits.")
