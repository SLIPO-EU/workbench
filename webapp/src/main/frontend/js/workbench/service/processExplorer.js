const actions = require('./api/fetch-actions');

module.exports = {
  fetch: (data, token) => {
    return actions.post('/action/process/query', token, data);
  }
};