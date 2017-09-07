import * as React from 'react';
import * as PropTypes from 'prop-types';
import * as ReactRedux from 'react-redux';

/**
 * Renders children only if a role name is provided as
 * a property and the user has the specific role
 * 
 * @class SecureContent
 * @extends {React.Component}
 */
class SecureContent extends React.Component {

  constructor(props) {
    super(props);
  }

  hasRole(role) {
    let user = this.props.user;

    if ((!user) || (!role)) {
      return false;
    }

    return (user.profile.roles.indexOf(role) !== -1);
  }

  render() {
    let { role } = this.props;

    if (!this.hasRole(role)) {
      return null;
    }

    return this.props.children;
  }
}

SecureContent.propTypes = {
  role: PropTypes.string,
  children: PropTypes.element.isRequired,
};

//
// Wrap into a connected component
//

const mapStateToProps = (state) => {
  return {
    user: state.user
  };
};

const mapDispatchToProps = null;

SecureContent = ReactRedux.connect(mapStateToProps, mapDispatchToProps)(SecureContent);

export default SecureContent;
