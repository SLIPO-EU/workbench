import actions from './api/fetch-actions';

export default {

  getEvents: (query, token) => {
    return actions.post('/action/admin/events', token, query);
  },

  getAccounts: (query, token) => {
    return actions.post('/action/admin/accounts', token, query);
  },

  createAccount: (account, token)=> {
    return actions.put('/action/admin/account', token, account);
  },

  updateAccount: (account, token) => {
    return actions.post('/action/admin/account', token, account);
  },

};
