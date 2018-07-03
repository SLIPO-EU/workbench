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

  static propTypes = {
    roles: PropTypes.arrayOf(PropTypes.string).isRequired,
    children: PropTypes.element.isRequired,
  }

  static defaultProps = {
    roles: [],
  }

  hasAnyRole(roles) {
    if ((!roles) || (roles.length === 0)) {
      return false;
    }

    const user = this.props.user;
    if (!user) {
      return false;
    }

    for (let role of roles) {
      if (user.roles.indexOf(role) !== -1) {
        return true;
      }
    }
    return false;
  }

  render() {
    let { roles } = this.props;

    if (!this.hasAnyRole(roles)) {
      return null;
    }

    return this.props.children;
  }
}

//
// Wrap into a connected component
//

const mapStateToProps = (state) => {
  return {
    user: state.user.profile
  };
};

const mapDispatchToProps = null;

SecureContent = ReactRedux.connect(mapStateToProps, mapDispatchToProps)(SecureContent);

export default SecureContent;
