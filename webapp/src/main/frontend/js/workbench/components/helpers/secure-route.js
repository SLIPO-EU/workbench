import * as React from 'react';
import * as ReactRedux from 'react-redux';
import { Route, Redirect } from 'react-router-dom';

class SecureRoute extends React.Component {

  constructor(props) {
    super(props);
  }

  hasRole(role) {
    if (!role) {
      return false;
    }

    let user = this.props.user;
    return (user && user.profile.roles.indexOf(role) !== -1);
  }

  render() {
    let { role, ...rest } = this.props;
    let authenticated = (this.props.user != null);

    if (!authenticated) {
      return (
        <Redirect to="/login" />
      );
    }
    if (this.hasRole(role)) {
      return (
        <Route {...rest} />
      );
    }
    return (
      <Redirect to="/error/403" />
    );
  }
}

//
// Wrap into a connected component
//

const mapStateToProps = (state) => {
  return {
    user: state.user
  };
};

const mapDispatchToProps = null;

SecureRoute = ReactRedux.connect(mapStateToProps, mapDispatchToProps)(SecureRoute);

export default SecureRoute;
