const React = require('react');
const ReactRedux = require('react-redux');
const { Switch, Route, Redirect } = require('react-router-dom');

const { userPropType } = require('../common-prop-structs');

const Home = require('./home');
const LoginForm = require('./login-form');
const RegisterForm = require('./register-form');
const ResetPasswordForm = () => (<p>Todo: Reset password</p>);

import Page403 from './pages/page-403.js';
import Page404 from './pages/page-404.js';

//
// Presentational component
//

class ContentRoot extends React.Component {
  constructor(props) {
    super(props);
  }

  render() {
    var authenticated = (this.props.user != null);

    if (!authenticated) {
      return (
        <Switch>
          <Route path="/login" name="login" component={LoginForm} />
          <Route path="/register" name="register" component={RegisterForm} />
          <Route path="/reset-password" name="reset-password" component={ResetPasswordForm} />
          <Redirect push={true} to="/login" />
        </Switch>
      );
    } else {
      return (
        <Switch>
          {/* Handle errors first */}
          <Route path="/error/403" component={Page403} exact />
          <Route path="/error/404" component={Page404} exact />
          {/* Redirect for authenticated users. Navigation after a successful login operation
              occurs after the component hierarchy is rendered due to state change and casues 
              /error/403 to render */}
          <Redirect from="/login" to="/dashboard" exact />
          <Redirect from="/register" to="/dashboard" exact />
          {/* Default component */}
          <Route path="/" name="home" component={() => (<Home user={this.props.user} />)} />
        </Switch>
      );
    }
  }
}

ContentRoot.propTypes = {
  user: userPropType
};

//
// Container component
//

const mapStateToProps = (state) => ({
  user: state.user.profile,
});

const mapDispatchToProps = null;

ContentRoot = ReactRedux.connect(mapStateToProps, mapDispatchToProps)(ContentRoot);

module.exports = ContentRoot;
