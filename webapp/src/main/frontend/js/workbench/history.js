const { createBrowserHistory } = require('history');

// A properly formatted basename should have a leading slash, but no trailing slash
const basename = '/workbench';

const history = createBrowserHistory();

module.exports = {
  basename,
  history,
};
