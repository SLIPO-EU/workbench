const actions = require('./api/fetch-actions');

module.exports = {
  fetch: (data, token) => {
    return actions.post('/action/process/query', token, data);
  },

  fetchExecutions: (data, token) => {
    return actions.get('/action/process/'+data+'/1/execution', token);
  },

  fetchExecutionDetails: (data, token) => {
    return actions.get('/action/process/'+data.process+'/1/execution/'+data.execution, token);
  },
};