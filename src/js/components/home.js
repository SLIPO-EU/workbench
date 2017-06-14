const React = require('react');
const {Link, Switch, Route, Redirect} = require('react-router-dom');

const Header = require('./layout/header');
const Sidebar = require('./layout/sidebar');
const Breadcrumb = require('./layout/breadcrumb');
const Aside = require('./layout/aside');
const Footer = require('./layout/footer');

const routeInfo = require('../route-info');
const {userPropType} = require('../common-prop-structs');

const Dashboard = require('./views/dashboard');
const Greeter = require('./views/greeter');


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

class Home extends React.Component 
{
  constructor(props)
  {
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

  _toggleSidebar() 
  {
    this.setState({sidebarOpen: !this.state.sidebarOpen});
  }

  _styleSidebar(style)
  {
    if (['fixed', 'compact', 'minimized', 'off-canvas', 'mobile-show'].indexOf(style) < 0) {
      console.warn('Ignoring unknown sidebar style: ' + style);
      return;
    }

    this.setState({sidebarStyle: style});
  }
  
  _toggleAsideMenu() 
  {
    this.setState({asideOpen: !this.state.asideOpen});
  }

  _styleAsideMenu(style) 
  {
    if (['fixed', 'off-canvas'].indexOf(style) < 0) {
      console.warn('Ignoring unknown aside-menu style: ' + style);
      return;
    }
    
    this.setState({asideStyle: style});
  }

  render() 
  {
    var cssClasses = [
      'app',
      /* header-* */
      'header-fixed',
      /* sidebar-* */
      this.state.sidebarOpen? null : 'sidebar-hidden',
      'sidebar-' + (this.state.sidebarStyle || 'fixed'),
      /* aside-menu-* */
      this.state.asideOpen? null : 'aside-menu-hidden',
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
            <div className="container-fluid">
              <Switch>
                <Route path="/greet" name={routeInfo.get('/greet').title} component={Greeter}/>
                <Route path="/dashboard" name={routeInfo.get('/dashboard').title} component={Dashboard}/>
              </Switch>
            </div>
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
