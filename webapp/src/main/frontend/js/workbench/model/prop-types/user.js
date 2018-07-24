import * as PropTypes from 'prop-types';

const userPropType = PropTypes.shape({
  username: PropTypes.string.isRequired,
  email: PropTypes.string.isRequired,
  givenName: PropTypes.string,
  familyName: PropTypes.string,
});

export default userPropType;
