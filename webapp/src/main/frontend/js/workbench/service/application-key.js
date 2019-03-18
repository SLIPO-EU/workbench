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

  checkApplicationName: (name, token) => {
    const data = {
      pagingOptions: {
        pageIndex: 0,
        pageSize: 1000,
      },
      query: {
        applicationName: name,
        revoked: null,
        userName: null,
      },
    };

    return actions.post('/action/admin/application-key/query', token, data)
      .then((result) => {
        return !!result.items.find(key => key.name.toUpperCase() === name.toUpperCase());
      });
  },

};
