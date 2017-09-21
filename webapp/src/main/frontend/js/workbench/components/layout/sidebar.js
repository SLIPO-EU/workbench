import * as React from 'react';
import * as  PropTypes from 'prop-types';
import { NavLink } from 'react-router-dom';
import * as Immutable from 'immutable';

import * as Roles from '../../model/role';
import { StaticRoutes, DynamicRoutes, buildPath } from '../../model/routes';

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
              <NavLink to={StaticRoutes.Dashboard} className="nav-link" activeClassName="active">
                {'Dashboard'}
              </NavLink>
            </li>

            <li className={'nav-item nav-dropdown ' + (expanded(Sections.Resource) ? 'open' : '')}>
              <a className="nav-link nav-dropdown-toggle" onClick={() => (toggle(Sections.Resource), false)}>
                {'Resources'}
              </a>
              <ul className="nav-dropdown-items">
                <li className="nav-item">
                  <NavLink to={StaticRoutes.ResourceExplorer} className="nav-link" activeClassName="active">
                    <i className="fa fa-database"></i>{'Explorer'}
                  </NavLink>
                </li>
                <li className="nav-item">
                  <NavLink to={StaticRoutes.ResourceRegistration} className="nav-link" activeClassName="active">
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
                  <NavLink to={StaticRoutes.ProcessExplorer} className="nav-link" activeClassName="active">
                    <i className="fa fa-clock-o"></i>{'ProcessExplorer'}
                  </NavLink>
                </li>
                <li className="nav-item">
                  <NavLink to={DynamicRoutes.ProcessDesignerCreate} className="nav-link" activeClassName="active">
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
                  <NavLink to={StaticRoutes.RecipeExplorer} className="nav-link" activeClassName="active">
                    <i className="fa fa-book"></i>{'Overview'}
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
                  <NavLink to={StaticRoutes.SchemaExplorer} className="nav-link" activeClassName="active">
                    <i className="fa fa-file-code-o"></i>{'Schema Mapper'}
                  </NavLink>
                </li>
              </ul>
            </li>

            <SecureContent role={Roles.ADMIN}>
              <li className={'nav-item nav-dropdown ' + (expanded(Sections.Admin) ? 'open' : '')}>
                <a className="nav-link nav-dropdown-toggle" onClick={() => (toggle(Sections.Admin), false)}>
                  {'Admin'}
                </a>
                <ul className="nav-dropdown-items">
                  <li className="nav-item">
                    <NavLink to={StaticRoutes.UserManager} className="nav-link" activeClassName="active">
                      <i className="fa fa-users"></i>{'Users'}
                    </NavLink>
                  </li>
                  <li className="nav-item">
                    <NavLink to={StaticRoutes.EventViewer} className="nav-link" activeClassName="active">
                      <i className="fa fa-heartbeat"></i>{'Event Log'}
                    </NavLink>
                  </li>
                </ul>
              </li>
            </SecureContent>

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

export default Sidebar;
