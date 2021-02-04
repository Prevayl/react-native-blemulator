'use strict';

const config = require('conventional-changelog-conventionalcommits');

module.exports = config({
  types: [
    { type: 'chore', section: 'Other', hidden: false },
    { type: 'revert', section: 'Reverted', hidden: false },
    { type: 'feat', section: 'Features', hidden: false },
    { type: 'fix', section: 'Bug Fixes', hidden: false },
    { type: 'docs', section: 'Documentation', hidden: false },
    { type: 'style', section: 'Styling', hidden: false },
    { type: 'refactor', section: 'Refactoring', hidden: false },
    { type: 'perf', section: 'Performance Improvements', hidden: false },
    { type: 'test', section: 'Tests', hidden: false },
    { type: 'build', section: 'Build System', hidden: false },
    { type: 'ci', section: 'CI', hidden: false },
  ],
});
