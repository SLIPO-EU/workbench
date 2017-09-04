const fetchJSON = require('./fetch-json');

module.exports = {
  submit: (url, token, form) => fetchJSON(url, 'POST', token, form),
  
  get: (url, token) => fetchJSON(url, 'GET', token, null),

  post: (url, token, data) => fetchJSON(url, 'POST', token, JSON.stringify(data)),
    
  put: (url, token, data) => fetchJSON(url, 'PUT', token, JSON.stringify(data)),

  patch: (url, token, data) => fetchJSON(url, 'PATCH', token, JSON.stringify(data)),
    
  delete: (url, token) => fetchJSON(url, 'DELETE', token, null),

};
