import actions from './api/fetch-actions';

export default {

  fetch: (token) => {
    return actions.get(`/action/dashboard`, token);
  },

};
