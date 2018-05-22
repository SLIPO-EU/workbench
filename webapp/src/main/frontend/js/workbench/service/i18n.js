import _ from 'lodash';

import {
  flatten,
} from 'flat';

import * as api from './api/i18n';

export const getMessages = (locale) => {
  return api.getMessages(locale).then(r => {
    if (_.isEmpty(r) || !_.isObject(r)) {
      throw new Error('Expected a non-empty object with keyed messages!');
    } else {
      // Convert to a flat object of keyed messages
      return flatten(r);
    }
  });
};
