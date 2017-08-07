var _ = require('lodash');

function diffShallow(o1, o2) {
  return _.fromPairs(
    _.differenceWith(
      _.toPairs(o1),
      _.toPairs(o2),
      function (e1, e2) {
        return (e1[0] == e2[0] && e1[1] == e2[1]);
      }
    )
  );
}

module.exports = diffShallow;
