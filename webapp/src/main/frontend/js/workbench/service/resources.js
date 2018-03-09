import actions from './api/fetch-actions';

export function fetch(data, token) {
  return actions.post('/action/resource/query', token, data);
}

export function upload(data, file, token) {
  const form = new FormData();
  form.append('file', file);
  form.append('data', new Blob([JSON.stringify(data)], {
    type: 'application/json'
  }));

  return actions.submit('/action/resource/upload', token, form);
}

export function register(data, token) {
  return actions.post('/action/resource/register', token, data);
}
