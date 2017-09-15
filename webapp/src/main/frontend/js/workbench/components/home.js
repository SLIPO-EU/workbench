import * as React from 'react';
import { Switch, Route, Redirect } from 'react-router-dom';
import { Container } from 'reactstrap';

import Header from './layout/header';
import Sidebar from './layout/sidebar';
import Breadcrumb from './layout/breadcrumb';
import Aside from './layout/aside';
import Footer from './layout/footer';

import * as Roles from '../model/role';
import { userPropType } from '../common-prop-structs';

import SecureRoute from './helpers/secure-route';

import Dashboard from './views/dashboard';

import ResourceExplorer from './views/resource-explorer';
import ResourceRegisterForm from './views/resource-register-form';

import Scheduler from './views/process-scheduler';
import ProcessDesigner from './views/process-designer';
import TripleGEO from './views/triplegeo';

import RecipeExplorer from './views/recipe-explorer';
import RecipeDesigner from './views/recipe-designer';

import UserManager from './views/user-manager';
import EventViewer from './views/event-viewer';

/////////////////////////////////////////////////////////////////
//
//    // Header options
//    1. '.header-fixed'					- Fixed Header
//
//    // Sidebar options
//    1. '.sidebar-fixed'					- Fixed Sidebar
//    2. '.sidebar-hidden'				- Hidden Sidebar
//    3. '.sidebar-off-canvas'		- Off Canvas Sidebar
//    4. '.sidebar-minimized'			- Minimized Sidebar (Only icons)
//    5. '.sidebar-compact'			  - Compact Sidebar
//    6. '.sidebar-mobile-show'   - ??
//
//    // Aside options
//    1. '.aside-menu-fixed'			- Fixed Aside Menu
//    2. '.aside-menu-hidden'			- Hidden Aside Menu
//    3. '.aside-menu-off-canvas'	- Off Canvas Aside Menu
//
//    // Footer options
//    1. 'footer-fixed'						- Fixed footer
//
/////////////////////////////////////////////////////////////////

class Home extends React.Component {
  constructor(props) {
    super(props);

    this._toggleSidebar = this._toggleSidebar.bind(this);
    this._styleSidebar = this._styleSidebar.bind(this);
    this._toggleAsideMenu = this._toggleAsideMenu.bind(this);
    this._styleAsideMenu = this._styleAsideMenu.bind(this);

    this.state = {
      sidebarOpen: true,
      sidebarStyle: 'fixed', // fixed, compact, minimized, off-canvas
      asideOpen: false,
      asideStyle: 'fixed', // fixed, off-canvas
    };
  }

  _toggleSidebar() {
    this.setState({ sidebarOpen: !this.state.sidebarOpen });
  }

  _styleSidebar(style) {
    if (['fixed', 'compact', 'minimized', 'off-canvas', 'mobile-show'].indexOf(style) < 0) {
      console.warn('Ignoring unknown sidebar style: ' + style);
      return;
    }

    this.setState({ sidebarStyle: style });
  }

  _toggleAsideMenu() {
    this.setState({ asideOpen: !this.state.asideOpen });
  }

  _styleAsideMenu(style) {
    if (['fixed', 'off-canvas'].indexOf(style) < 0) {
      console.warn('Ignoring unknown aside-menu style: ' + style);
      return;
    }

    this.setState({ asideStyle: style });
  }

  render() {
    var cssClasses = [
      'app',
      /* header-* */
      'header-fixed',
      /* sidebar-* */
      this.state.sidebarOpen ? null : 'sidebar-hidden',
      'sidebar-' + (this.state.sidebarStyle || 'fixed'),
      /* aside-menu-* */
      this.state.asideOpen ? null : 'aside-menu-hidden',
      'aside-menu-' + (this.state.asideStyle || 'fixed'),
    ];

    return (
      <div className={cssClasses.join(' ')}>
        <Header
          user={this.props.user}
          toggleSidebar={this._toggleSidebar}
          styleSidebar={this._styleSidebar}
          toggleAsideMenu={this._toggleAsideMenu}
          styleAsideMenu={this._styleAsideMenu}
        />
        <div className="app-body">
          <Route path="/" component={Sidebar} />
          <div className="main">
            <Route path="/" component={Breadcrumb} />
            <Container fluid>
              <Switch>
                <Redirect from="/" to="/dashboard" exact />
                <Route path="/dashboard" component={Dashboard} />
                <Route path="/resource/explorer" component={ResourceExplorer} />
                <Route path="/resource/register" component={ResourceRegisterForm} />
                <Route path="/process/scheduler" component={Scheduler} />
                <Route path="/process/design/triplegeo" component={TripleGEO}/>
                <Route path="/process/design" component={ProcessDesigner} />
                <Route path="/recipe/explorer" component={RecipeExplorer} />
                <Route path="/recipe/design" component={RecipeDesigner} />
                <SecureRoute path="/admin/user-manager" component={UserManager} role={Roles.ADMIN} />
                <SecureRoute path="/admin/event-viewer" component={EventViewer} role={Roles.MAINTAINER} />
                <Redirect push={true} to="/error/404" />
              </Switch>
            </Container>
          </div>
          <Aside />
        </div>
        <Footer />
      </div>
    );
  }
}

Home.propTypes = {
  user: userPropType,
};

module.exports = Home;
