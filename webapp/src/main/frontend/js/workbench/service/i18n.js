const _ = require('lodash');
const {flatten} = require('flat');

const api = require('./api/i18n');

module.exports = {
  
  getMessages: (locale) => (
    api.getMessages(locale).then(r => {
      if (_.isEmpty(r) || !_.isObject(r)) {
        throw new Error('Expected a non-empty object with keyed messages!');
      } else {
        // Convert to a flat object of keyed messages
        return flatten(r);
      }
    })
  ),
};
