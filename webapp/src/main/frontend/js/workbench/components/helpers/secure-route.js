import * as React from 'react';
import * as ReactRedux from 'react-redux';
import { Route, Redirect } from 'react-router-dom';

import { Pages, ErrorPages } from '../../model/routes';

class SecureRoute extends React.Component {

  constructor(props) {
    super(props);
  }

  hasRole(role) {
    if (!role) {
      return false;
    }

    let user = this.props.user;
    return (user && user.roles.indexOf(role) !== -1);
  }

  render() {
    let { role, ...rest } = this.props;
    let authenticated = (this.props.user != null);

    if (!authenticated) {
      return (
        <Redirect to={Pages.Login} />
      );
    }
    if (this.hasRole(role)) {
      return (
        <Route {...rest} />
      );
    }
    return (
      <Redirect to={ErrorPages.Forbidden} />
    );
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

SecureRoute = ReactRedux.connect(mapStateToProps, mapDispatchToProps)(SecureRoute);

export default SecureRoute;
