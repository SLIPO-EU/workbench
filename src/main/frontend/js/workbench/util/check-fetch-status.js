// A handler that can be chained to a fetch promise to reject non
// successfull (ie non 2xx) HTTP requests

var sprintf = require('sprintf');

module.exports = function (res) 
{
  if (res.status >= 200 && res.status < 300) {
    return res;
  } else {
    var err = new Error(sprintf(
      "Received: %d %s", res.status, res.statusText));
    throw err;
  }
};
