const PropTypes = require('prop-types');

var userPropType = PropTypes.shape({
  username: PropTypes.string.isRequired,
  email: PropTypes.string.isRequired,
  givenName: PropTypes.string,
  familyName: PropTypes.string,
});


module.exports = {
  userPropType,
};
