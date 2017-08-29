const React = require('react');
const PropTypes = require('prop-types');
const ReactRedux = require('react-redux');
const { NavLink } = require('react-router-dom');
const Immutable = require('immutable');

import * as Roles from '../../model/role';

class Sidebar extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      expanded: new Immutable.Set(),
    };
  }

  hasRole(role) {
    if (!role) {
      return false;
    }

    let user = this.props.user;
    return (user && user.profile.roles.indexOf(role) !== -1);
  }

  render() {
    var { location } = this.props;

    var expanded = (p) => (
      location.pathname.indexOf(p) >= 0 || this.state.expanded.has(p)
    );

    var toggle = (p) => {
      var s = this.state.expanded;
      this.setState({ expanded: s.has(p) ? s.remove(p) : s.add(p) });
    };

    return (
      <div className="sidebar">
        <nav className="sidebar-nav">
          <ul className="nav">

            <li className="nav-item">
              <NavLink to={'/dashboard'} className="nav-link" activeClassName="active">
                {'Dashboard'}
              </NavLink>
            </li>

            <li className={'nav-item nav-dropdown ' + (expanded('/resource') ? 'open' : '')}>
              <a className="nav-link nav-dropdown-toggle" onClick={() => (toggle('/resource'), false)}>
                {'Resources'}
              </a>
              <ul className="nav-dropdown-items">
                <li className="nav-item">
                  <NavLink to={'/resource/explorer'} className="nav-link" activeClassName="active">
                    <i className="fa fa-database"></i>{'Explorer'}
                  </NavLink>
                </li>
                <li className="nav-item">
                  <NavLink to={'/resource/register'} className="nav-link" activeClassName="active">
                    <i className="fa fa-pencil"></i>{'Register'}
                  </NavLink>
                </li>
              </ul>
            </li>

            <li className={'nav-item nav-dropdown ' + (expanded('/process') ? 'open' : '')}>
              <a className="nav-link nav-dropdown-toggle" onClick={() => (toggle('/process'), false)}>
                {'Data Processing'}
              </a>
              <ul className="nav-dropdown-items">
                <li className="nav-item">
                  <NavLink to={'/process/scheduler'} className="nav-link" activeClassName="active">
                    <i className="fa fa-clock-o"></i>{'Scheduler'}
                  </NavLink>
                </li>
                <li className="nav-item">
                  <NavLink to={'/process/design'} className="nav-link" activeClassName="active">
                    <i className="fa fa-magic"></i>{'Design'}
                  </NavLink>
                </li>
              </ul>
            </li>

            <li className={'nav-item nav-dropdown ' + (expanded('/recipe') ? 'open' : '')}>
              <a className="nav-link nav-dropdown-toggle" onClick={() => (toggle('/recipe'), false)}>
                {'Recipes'}
              </a>
              <ul className="nav-dropdown-items">
                <li className="nav-item">
                  <NavLink to={'/recipe/overview'} className="nav-link" activeClassName="active">
                    <i className="fa fa-book"></i>{'Overview'}
                  </NavLink>
                </li>
                <li className="nav-item">
                  <NavLink to={'/recipe/design'} className="nav-link" activeClassName="active">
                    <i className="fa fa-magic"></i>{'Design'}
                  </NavLink>
                </li>
              </ul>
            </li>

            <li className={'nav-item nav-dropdown ' + (expanded('/tools') ? 'open' : '')}>
              <a className="nav-link nav-dropdown-toggle" onClick={() => (toggle('/tools'), false)}>
                {'Tools'}
              </a>
              <ul className="nav-dropdown-items">
                <li className="nav-item">
                  <NavLink to={'/tools/schema-mapper'} className="nav-link" activeClassName="active">
                    <i className="fa fa-file-code-o"></i>{'Schema Mapper'}
                  </NavLink>
                </li>
              </ul>
            </li>
            {this.hasRole(Roles.ADMIN) &&
              <li className={'nav-item nav-dropdown ' + (expanded('/admin') ? 'open' : '')}>
                <a className="nav-link nav-dropdown-toggle" onClick={() => (toggle('/admin'), false)}>
                  {'Admin'}
                </a>
                <ul className="nav-dropdown-items">
                  <li className="nav-item">
                    <NavLink to={'/admin/user-manager'} className="nav-link" activeClassName="active">
                      <i className="fa fa-users"></i>{'Users'}
                    </NavLink>
                  </li>
                  <li className="nav-item">
                    <NavLink to={'/admin/event-viewer'} className="nav-link" activeClassName="active">
                      <i className="fa fa-heartbeat"></i>{'Event Log'}
                    </NavLink>
                  </li>
                </ul>
              </li>
            }
          </ul>
        </nav>
      </div>
    );
  }
}

Sidebar.propTypes = {
  location: PropTypes.shape({
    pathname: PropTypes.string.isRequired,
  }),
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

Sidebar = ReactRedux.connect(mapStateToProps, mapDispatchToProps)(Sidebar);

module.exports = Sidebar;
