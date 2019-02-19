import actions from './api/fetch-actions';

export default {

  query: (query, token) => {
    return actions.post('/action/admin/application-key/query', token, query);
  },

  create: (data, token) => {
    return actions.post('/action/admin/application-key', token, data);
  },

  revoke: (id, token) => {
    return actions.delete(`/action/admin/application-key/${id}/revoke`, token);
  },

};
