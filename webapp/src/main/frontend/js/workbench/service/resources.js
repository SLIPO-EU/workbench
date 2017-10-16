const actions = require('./api/fetch-actions');

let resources = [
  {
    id: '1',
    name: 'Resource 1',
    description: 'This is a test resource',
    format: 'SHAPEFILE',
  },
  {
    id: '2',
    name: 'Resource 2',
    description: 'This is another test resource',
    format: 'GEOJSON',
  },
];

const mockActions = {
 
  get: (url, token, index, offset) => {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        resolve(resources);
      }, 500);
    });
  },

  post: (url, token, data) => {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        resources.push(data);
        resolve(data);
      }, 500);
    });
  },

};

module.exports = {
  fetch: (index, offset, token) => {
    return mockActions.get('/action/resources', token, index, offset);
  },
  create: (data, token) => {
    if (!data.id) {
      throw new Error('No resource id provided');
    }
    return mockActions.post('/action/resources', token, data);
  },
};
