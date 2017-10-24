const actions = require('./api/fetch-actions');

module.exports = {
  fetch: (data, token) => {
    return actions.post('/action/resource/query', token, data);
  },
  upload: (data, file, token) => {
    const form = new FormData();
    form.append('file', file);
    form.append('data', new Blob([JSON.stringify(data)], { 
      type: 'application/json'
    })); 

    return actions.submit('/action/resource/upload', token, form, 'PUT');
  },
  register: (data, token) => {
    return actions.put('/action/resource/register', token, data);
  },
};
