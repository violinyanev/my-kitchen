// commitlint.config.js
module.exports = {
  extends: ['@commitlint/config-conventional'],
  ignores: [(commit) => {
    // Ignore validation for "initial commit" messages (case insensitive)
    // This allows Copilot's initial commits to bypass conventional commit validation
    const message = commit.toLowerCase().trim();
    return message === 'initial commit' || message.startsWith('initial commit');
  }],
};
