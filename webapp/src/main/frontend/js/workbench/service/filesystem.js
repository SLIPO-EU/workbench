const actions = require('./api/fetch-actions');

function relativePath(path) {
  if (path.startsWith('/')) {
    return path.slice(1);
  }
  return path;
}

module.exports = {

  fetch: (token) => {
    return actions.get(`/action/file-system`, token);
  },

  createFolder: (path, token) => {
    return actions.post(`/action/file-system`, token, { path: relativePath(path), });
  },

  deletePath: (path, token) => {
    return actions.delete(`/action/file-system?path=${relativePath(path)}`, token);
  },

  upload: (data, file, token) => {
    const form = new FormData();
    form.append('file', file);
    form.append('data', new Blob([JSON.stringify(data)], {
      type: 'application/json'
    }));

    return actions.submit('/action/file-system/upload', token, form);
  },

};
