import actions from './api/fetch-actions';

module.exports = {
  fetch: (token) => {
    return actions.get(`/action/dashboard`, token);
  },
};
  