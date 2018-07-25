import actions from './api/fetch-actions';

export default {

  getEvents: (query, token) => {
    return actions.post('/action/admin/events', token, query);
  },

  getAccounts: (query, token) => {
    return actions.post('/action/admin/accounts', token, query);
  },

  updateAccount: (account, token) => {
    return actions.post('/action/admin/account', token, account);
  },

};
