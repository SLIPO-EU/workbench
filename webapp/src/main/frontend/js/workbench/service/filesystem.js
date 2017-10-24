const actions = require('./api/fetch-actions');

module.exports = {
  fetch: (path, token) => {
    return actions.get(`/action/file-system?path=${path}`, token);
  },
};
