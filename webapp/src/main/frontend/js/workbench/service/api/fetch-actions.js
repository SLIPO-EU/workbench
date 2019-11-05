import fetchJSON from './fetch-json';

const headers = {
  'accept': 'application/json',
  'Content-Type': 'application/json',
  'x-requested-with': 'XMLHttpRequest',
};

const formHeaders = {
  'accept': 'application/json',
  'x-requested-with': 'XMLHttpRequest',
};

export default {
  submit: (url, token, form, method = 'POST') => fetchJSON(url, method, token, form, formHeaders),

  get: (url, token) => fetchJSON(url, 'GET', token, null, headers),

  post: (url, token, data) => fetchJSON(url, 'POST', token, typeof data === 'string' ? data : JSON.stringify(data), headers),

  put: (url, token, data) => fetchJSON(url, 'PUT', token, JSON.stringify(data), headers),

  patch: (url, token, data) => fetchJSON(url, 'PATCH', token, JSON.stringify(data), headers),

  delete: (url, token) => fetchJSON(url, 'DELETE', token, null, headers),

};
