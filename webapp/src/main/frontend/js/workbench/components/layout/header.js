import * as React from 'react';
import * as PropTypes from 'prop-types';
import * as ReactRedux from 'react-redux';
import { Link, withRouter } from 'react-router-dom';

import {
  Dropdown,
  DropdownMenu,
  DropdownItem,
  DropdownToggle,
} from 'reactstrap';

import userPropType from '../../model/prop-types/user';
import { Roles, StaticRoutes } from '../../model';

import Toolbar from './toolbar';
import AsideToggle from './aside-toggle';

import { SecureContent, SelectLanguage, Zoom } from '../helpers';

import { logout } from '../../ducks/user';

//
// Presentational component
//

class Header extends React.Component {
  constructor(props) {
    super(props);

    this._toggleDropdown = this._toggleDropdown.bind(this);
    this._showProfile = this._showProfile.bind(this);
    this._showSettings = this._showSettings.bind(this);

    this.state = {
      dropdownOpen: false,
    };
  }

  static propTypes = {
    user: userPropType,
    logout: PropTypes.func.isRequired,
    toggleSidebar: PropTypes.func.isRequired,
    styleSidebar: PropTypes.func.isRequired,
    setAsideMenuVisibility: PropTypes.func.isRequired,
    toggleAsideMenu: PropTypes.func.isRequired,
    styleAsideMenu: PropTypes.func.isRequired,
  }

  _toggleDropdown() {
    this.setState({ dropdownOpen: !this.state.dropdownOpen });
  }

  _showProfile() {
    this.props.history.push(StaticRoutes.Profile);
  }

  _showSettings() {
    this.props.history.push(StaticRoutes.Settings);
  }

  render() {
    return (
      <header className="app-header navbar">

        <button className="navbar-toggler mobile-sidebar-toggler d-lg-none" type="button"
          onClick={() => this.props.styleSidebar('mobile-show')}>
          <i className="fa fa-navicon"></i>
        </button>
        <a className="navbar-brand" target="_blank" href="http://www.slipo.eu/"></a>

        {/* left-aligned menu items */}
        <Toolbar user={this.props.user} toggleSidebar={this.props.toggleSidebar} />

        {/* right-aligned menu items */}
        <ul className="nav navbar-nav ml-auto">
          <SecureContent roles={[Roles.DEVELOPER]}>
            <li className="nav-item">
              <Zoom />
            </li>
          </SecureContent>
          <li className="nav-item d-md-down-none alert d-none">
            <Link to={StaticRoutes.EventViewer} className="nav-link">
              <i className="icon-bell"></i><span className="badge badge-pill badge-info">5</span>
            </Link>
          </li>
          <li className="nav-item d-md-down-none lang-select">
            <SelectLanguage />
          </li>
          <li className="nav-item">
            <Dropdown isOpen={this.state.dropdownOpen} toggle={this._toggleDropdown}>
              <DropdownToggle caret size="sm" className="no-outline">
                <span className="d-md-down-none">{this.props.user.username}</span>
              </DropdownToggle>
              <DropdownMenu>
                <DropdownItem onClick={this._showProfile}>
                  <i className="fa fa-user"></i>&nbsp;{'Profile'}
                </DropdownItem>
                <DropdownItem onClick={this._showSettings}>
                  <i className="fa fa-wrench"></i>&nbsp;{'Settings'}
                </DropdownItem>
                <DropdownItem onClick={this.props.logout}>
                  <i className="fa fa-sign-out"></i>&nbsp;{'Logout'}
                </DropdownItem>
              </DropdownMenu>
            </Dropdown>
          </li>
          {/* toggle aside menu */}
          <AsideToggle toggleAsideMenu={this.props.toggleAsideMenu} setAsideMenuVisibility={this.props.setAsideMenuVisibility} />
        </ul>

      </header>
    );
  }
}

//
// Container component
//

const mapStateToProps = () => ({});

const mapDispatchToProps = (dispatch) => ({
  logout: () => (
    dispatch(logout())
  ),
});

export default withRouter(ReactRedux.connect(mapStateToProps, mapDispatchToProps)(Header));
