import * as actions from './api/fetch-actions';

const getConfiguration = () => {
  return actions.get('/action/configuration');
};

export default {
  getConfiguration,
};
