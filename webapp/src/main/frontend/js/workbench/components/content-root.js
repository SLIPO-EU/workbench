import _ from 'lodash';
import * as React from 'react';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';
import { Switch, Route, Redirect } from 'react-router-dom';

import { ToastContainer } from 'react-toastify';

import { Pages, StaticRoutes, DynamicRoutes, ErrorPages } from '../model/routes';
import { userPropType } from '../model/prop-types/user';
import { resize } from '../ducks/ui/viewport';
import { getFilesystem } from '../ducks/config';

import Home from './home';
import LoginForm from './pages/login-form';
import RegisterForm from './pages/register-form';

import Page403 from './pages/page-403.js';
import Page404 from './pages/page-404.js';

import Placeholder from './helpers/placeholder';

//
// Presentational component
//
class ContentRoot extends React.Component {

  constructor(props) {
    super(props);
  }
  
  componentWillMount() {
    if (this.props.user != null) {
      this._getFileSystem();
    }
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.user == null && nextProps.user != null) {
      this._getFileSystem();
    }
  }

  componentDidMount() {
    this._listener = _.debounce(this._setViewport.bind(this), 150);
    window.addEventListener('resize', this._listener);
  }

  componentWillUnmount() {
    window.removeEventListener('resize', this._listener);
  }

  _getFileSystem() {
    this.props.getFilesystem('');
  }

  _setViewport() {
    this.props.resize(
      document.documentElement.clientWidth, 
      document.documentElement.clientHeight
    );
  }

  render() {
    let authenticated = (this.props.user != null);
    let routes;

    if (!authenticated) {
      routes = (
        <Switch>
          <Route path={Pages.Login} name="login" component={LoginForm} />
          <Route path={Pages.Register} name="register" component={RegisterForm} />
          <Route path={Pages.ResetPassword} name="reset-password" component={Placeholder} />
          <Redirect push={true} to={Pages.Login} />
        </Switch>
      );
    } else {
      routes = (
        <Switch>
          {/* Handle errors first */}
          <Route path={ErrorPages.Forbidden} component={Page403} exact />
          <Route path={ErrorPages.NotFound} component={Page404} exact />
          {/* Redirect for authenticated users. Navigation after a successful login operation
              occurs after the component hierarchy is rendered due to state change and causes
              /error/404 to render */}
          <Redirect from={Pages.Login} to={StaticRoutes.Dashboard} exact />
          <Redirect from={Pages.Register} to={StaticRoutes.Dashboard} exact />
          <Redirect from={Pages.ResetPassword} to={StaticRoutes.Dashboard} exact />
          {/* Default component */}
          <Route path="/" name="home" component={() => (<Home user={this.props.user} />)} />
        </Switch>
      );
    }

    return (
      <div>
        <ToastContainer
          position="top-right"
          type="default"
          autoClose={5000}
          hideProgressBar={false}
          newestOnTop={false}
          closeOnClick
          pauseOnHover
        />
        {routes}
      </div>
    );
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

const mapDispatchToProps = (dispatch) => bindActionCreators({ resize, getFilesystem }, dispatch);

ContentRoot = ReactRedux.connect(mapStateToProps, mapDispatchToProps)(ContentRoot);

module.exports = ContentRoot;
