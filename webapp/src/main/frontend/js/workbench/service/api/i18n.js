import fetch from 'fetch';

import {
  checkStatus,
} from '../util/check-fetch-status';

const credentials = 'same-origin';

export const getMessages = (locale) => {
  return fetch(`/i18n/${locale}/messages.json`, { credentials })
    .then(checkStatus)
    .then(res => res.json());
};

export default {
  getMessages,
};
