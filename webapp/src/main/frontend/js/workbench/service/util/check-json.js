const _ = require('lodash');

function checkError(r) {
  if (_.isEmpty(r.errors)) {
    return r;
  } else {
    var e = _.first(r.errors);
    throw new Error(e.description);
  }
}

module.exports = { checkError };
