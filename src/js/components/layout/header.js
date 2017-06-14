const React = require('react');
const PropTypes = require('prop-types');
const ReactRedux = require('react-redux');
const {Dropdown, DropdownMenu, DropdownItem} = require('reactstrap');
const {Link, NavLink} = require('react-router-dom');

const {userPropType} = require('../../common-prop-structs');

//
// Presentational component
//

class Header extends React.Component 
{
  constructor(props) 
  {
    super(props);

    this._toggleDropdown = this._toggleDropdown.bind(this);

    this.state = {
      dropdownOpen: false,
    };
  }
   
  _toggleDropdown() 
  {
    this.setState({dropdownOpen: !this.state.dropdownOpen});
  }

  render() 
  {
    return (
      <header className="app-header navbar">
        
        <button className="navbar-toggler mobile-sidebar-toggler d-lg-none" type="button" 
            onClick={() => this.props.styleSidebar('mobile-show')}>
          <i className="fa fa-navicon"></i>
        </button>
        <a className="navbar-brand" href="#"></a>
        
        {/* left-aligned menu items */}
        <ul className="nav navbar-nav d-md-down-none mr-auto">
          {/* toggle sidebar */}
          <li className="nav-item">
            <button className="nav-link navbar-toggler sidebar-toggler" type="button" 
                onClick={this.props.toggleSidebar}>
              <i className="fa fa-navicon"></i>
            </button>
          </li>       
          {/* left-aligned top navbar items */} 
          <li className="nav-item px-3">
            <a className="nav-link" href="#/dashboard">Dashboard</a>
          </li>
          <li className="nav-item px-3">
            <a className="nav-link" href="#/users">Users</a>
          </li>
          <li className="nav-item px-3">
            <a className="nav-link" href="#/settings">Settings</a>
          </li>
        </ul>

        {/* right-aligned menu items */}
        <ul className="nav navbar-nav ml-auto">
          <li className="nav-item d-md-down-none">
            <a className="nav-link" href="#"><i className="icon-bell"></i><span className="badge badge-pill badge-danger">5</span></a>
          </li>
          <li className="nav-item d-md-down-none">
            <a className="nav-link" href="#"><i className="icon-list"></i></a>
          </li>
          <li className="nav-item d-md-down-none">
            <a className="nav-link" href="#"><i className="icon-location-pin"></i></a>
          </li>
          <li className="nav-item">
            <Dropdown isOpen={this.state.dropdownOpen} toggle={this._toggleDropdown}>
              <button onClick={this._toggleDropdown} className="nav-link dropdown-toggle" data-toggle="dropdown" type="button" 
                  aria-haspopup="true" aria-expanded={this.state.dropdownOpen}>
                <img src={'https://github.com/identicons/drmalex07.png'} className="img-avatar" 
                  alt={this.props.user.username}
                 />
                <span className="d-md-down-none">{this.props.user.username}</span>
              </button>
              <DropdownMenu className="dropdown-menu-right">
                <DropdownItem header className="text-center">
                  <strong>{'Notifications'}</strong>
                </DropdownItem>
                <DropdownItem className="btn">
                  <i className="fa fa-envelope-o"></i>&nbsp;{'Messages'}<span className="badge badge-success">42</span>
                </DropdownItem>
                <DropdownItem className="btn">
                  <i className="fa fa-tasks"></i>&nbsp;{'Tasks'}<span className="badge badge-danger">42</span>
                </DropdownItem>
                <DropdownItem className="btn">
                  <i className="fa fa-comments"></i>&nbsp;{'Comments'}<span className="badge badge-warning">42</span>
                </DropdownItem>
                {/*<DropdownItem divider />*/}
                <DropdownItem header className="text-center">
                  <strong>{'Account'}</strong>
                </DropdownItem>
                <DropdownItem className="btn">
                  <i className="fa fa-user"></i>&nbsp;{'Profile'}
                </DropdownItem>
                <DropdownItem className="btn">
                  <i className="fa fa-wrench"></i>&nbsp;{'Settings'}
                </DropdownItem>
                <DropdownItem className="btn" onClick={this.props.logout}>
                  <i className="fa fa-sign-out"></i>&nbsp;{'Logout'}
                </DropdownItem>
              </DropdownMenu>
            </Dropdown>
          </li>
          {/* toggle aside menu */}
          <li className="nav-item d-md-down-none">
            <button className="nav-link navbar-toggler aside-menu-toggler" type="button" 
                onClick={this.props.toggleAsideMenu}>
              <i className="fa fa-navicon"></i>
            </button>
          </li>
        </ul>

      </header>
    );
  }
}

Header.propTypes = {
  user: userPropType,
  logout: PropTypes.func.isRequired,
  toggleSidebar: PropTypes.func.isRequired,
  styleSidebar: PropTypes.func.isRequired,
  toggleAsideMenu: PropTypes.func.isRequired,
  styleAsideMenu: PropTypes.func.isRequired,
};


//
// Container component
//

const {logout} = require('../../actions/user');

const mapStateToProps = (state, ownProps) => ({});

const mapDispatchToProps = (dispatch, ownProps) => ({
  logout: () => (
    dispatch(logout())
  ),
}); 

Header = ReactRedux.connect(mapStateToProps, mapDispatchToProps)(Header);

module.exports = Header;
