const { createBrowserHistory } = require('history');

const history = createBrowserHistory();

// A properly formatted basename should have a leading slash, but no trailing slash
const basename = '/workbench';

module.exports = {
  history,
  basename
};
