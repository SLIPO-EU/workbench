const React = require('react');
const PropTypes = require('prop-types');
const ReactRedux = require('react-redux');
const { NavLink } = require('react-router-dom');
const Immutable = require('immutable');

import * as Roles from '../../model/role';
import SecureContent from '../helpers/secure-content';

const Sections = {
  Resource: 'Resource',
  Process: 'Process',
  Recipe: 'Recipe',
  Tool: 'Tool',
  Admin: 'Admin',
};

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

            <li className={'nav-item nav-dropdown ' + (expanded(Sections.Resource) ? 'open' : '')}>
              <a className="nav-link nav-dropdown-toggle" onClick={() => (toggle(Sections.Resource), false)}>
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

            <li className={'nav-item nav-dropdown ' + (expanded(Sections.Process) ? 'open' : '')}>
              <a className="nav-link nav-dropdown-toggle" onClick={() => (toggle(Sections.Process), false)}>
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

            <li className={'nav-item nav-dropdown ' + (expanded(Sections.Recipe) ? 'open' : '')}>
              <a className="nav-link nav-dropdown-toggle" onClick={() => (toggle(Sections.Recipe), false)}>
                {'Recipes'}
              </a>
              <ul className="nav-dropdown-items">
                <li className="nav-item">
                  <NavLink to={'/recipe/explorer'} className="nav-link" activeClassName="active">
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

            <li className={'nav-item nav-dropdown ' + (expanded(Sections.Tool) ? 'open' : '')}>
              <a className="nav-link nav-dropdown-toggle" onClick={() => (toggle(Sections.Tool), false)}>
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
              <li className={'nav-item nav-dropdown ' + (expanded(Sections.Admin) ? 'open' : '')}>
                <a className="nav-link nav-dropdown-toggle" onClick={() => (toggle(Sections.Admin), false)}>
                  {'Admin'}
                </a>
                <ul className="nav-dropdown-items">
                  <li className="nav-item">
                    <NavLink to={'/admin/user-manager'} className="nav-link" activeClassName="active">
                      <i className="fa fa-users"></i>{'Users'}
                    </NavLink>
                  </li>
                  <SecureContent role={Roles.MAINTAINER}>
                    <li className="nav-item">
                      <NavLink to={'/admin/event-viewer'} className="nav-link" activeClassName="active">
                        <i className="fa fa-heartbeat"></i>{'Event Log'}
                      </NavLink>
                    </li>
                  </SecureContent>
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
