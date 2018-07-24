import _ from 'lodash';

import {
  flatten,
} from 'flat';

import * as i18n from './api/i18n';

export default {

  getMessages: (locale) => {
    return i18n.getMessages(locale).then(r => {
      if (_.isEmpty(r) || !_.isObject(r)) {
        throw new Error('Expected a non-empty object with keyed messages!');
      } else {
        // Convert to a flat object of keyed messages
        return flatten(r);
      }
    });
  },

};
