const _ = require('lodash');

var mirrorToPath = function (o, delimiter='.') 
{   
  var mapper = function (prefix) {
    return function (val, key) {
      var prefixedKey = prefix? (prefix + delimiter + key) : key;
      if (_.isObject(val)) {
        return _.mapValues(val, mapper(prefixedKey));
      } else {
        return prefixedKey;
      }
    };
  };
  
  return _.mapValues(o, mapper(''));
};

module.exports = mirrorToPath;
