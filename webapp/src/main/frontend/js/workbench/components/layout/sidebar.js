import * as React from 'react';
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';

import * as  PropTypes from 'prop-types';
import { NavLink } from 'react-router-dom';
import { toggleMenu } from '../../ducks/ui/menu';
import * as Roles from '../../model/role';
import { StaticRoutes, DynamicRoutes } from '../../model/routes';

import SecureContent from '../helpers/secure-content';

const Sections = {
  Resource: 'Resource',
  Process: 'Workflow',
  Recipe: 'Recipe',
  Utilities: 'Utilities',
  Admin: 'Admin',
  Documentation: 'Documentation',
};

class Sidebar extends React.Component {

  render() {
    var { location } = this.props;

    var expanded = (p) => (
      location.pathname.indexOf(p) >= 0 || this.props.expanded.has(p)
    );

    var toggle = (p) => {
      this.props.toggleMenu(p);
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
                    <i className="fa fa-search"></i>{'Search'}
                  </NavLink>
                </li>
                <SecureContent roles={[Roles.ADMIN, Roles.AUTHOR]}>
                  <li className="nav-item">
                    <NavLink to={StaticRoutes.ResourceRegistration} className="nav-link" activeClassName="active">
                      <i className="fa fa-pencil"></i>{'Register'}
                    </NavLink>
                  </li>
                </SecureContent>
                <SecureContent roles={[Roles.ADMIN, Roles.AUTHOR]}>
                  <li className="nav-item">
                    <NavLink to={StaticRoutes.ResourceExport} className="nav-link" activeClassName="active">
                      <i className="fa fa-cloud-download"></i>{'Export'}
                    </NavLink>
                  </li>
                </SecureContent>
                <li className="nav-item">
                  <NavLink to={StaticRoutes.UserFileSystem} className="nav-link" activeClassName="active">
                    <i className="fa fa-folder"></i>{'My Files'}
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
                    <i className="fa fa-search"></i>{'Search'}
                  </NavLink>
                </li>
                <li className="nav-item">
                  <NavLink to={StaticRoutes.ProcessExecutionExplorer} className="nav-link" activeClassName="active">
                    <i className="fa fa-cogs"></i>{'Workflow Executions'}
                  </NavLink>
                </li>
                <SecureContent roles={[Roles.ADMIN, Roles.AUTHOR]}>
                  <li className="nav-item">
                    <NavLink to={DynamicRoutes.ProcessDesignerCreate} className="nav-link" activeClassName="active">
                      <i className="fa fa-magic"></i>{'Design'}
                    </NavLink>
                  </li>
                </SecureContent>
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

            <SecureContent roles={[Roles.ADMIN, Roles.DEVELOPER]}>
              <li className={'nav-item nav-dropdown ' + (expanded(Sections.Admin) ? 'open' : '')}>
                <a className="nav-link nav-dropdown-toggle" onClick={() => (toggle(Sections.Admin), false)}>
                  {'Admin'}
                </a>
                <ul className="nav-dropdown-items">
                  <SecureContent roles={[Roles.ADMIN]}>
                    <li className="nav-item">
                      <NavLink to={StaticRoutes.UserManager} className="nav-link" activeClassName="active">
                        <i className="fa fa-users"></i>{'Users'}
                      </NavLink>
                    </li>
                  </SecureContent>
                  <SecureContent roles={[Roles.ADMIN, Roles.DEVELOPER]}>
                    <li className="nav-item">
                      <NavLink to={StaticRoutes.EventViewer} className="nav-link" activeClassName="active">
                        <i className="fa fa-heartbeat"></i>{'Event Log'}
                      </NavLink>
                    </li>
                  </SecureContent>
                  <SecureContent roles={[Roles.ADMIN]}>
                    <li className="nav-item">
                      <NavLink to={StaticRoutes.ApplicationKeyViewer} className="nav-link" activeClassName="active">
                        <i className="fa fa-cogs"></i>{'Application Keys'}
                      </NavLink>
                    </li>
                  </SecureContent>
                  <SecureContent roles={[Roles.ADMIN]}>
                    <li className="nav-item">
                      <NavLink to={StaticRoutes.ApiUsage} className="nav-link" activeClassName="active">
                        <i className="fa fa-pie-chart"></i>{'Api Usage'}
                      </NavLink>
                    </li>
                  </SecureContent>
                </ul>
              </li>
            </SecureContent>

            <SecureContent roles={[Roles.ADMIN, Roles.DEVELOPER]}>
              <li className={'nav-item nav-dropdown ' + (expanded(Sections.Utilities) ? 'open' : '')}>
                <a className="nav-link nav-dropdown-toggle" onClick={() => (toggle(Sections.Utilities), false)}>
                  {'Utilities'}
                </a>
                <ul className="nav-dropdown-items">
                  <SecureContent roles={[Roles.ADMIN, Roles.DEVELOPER]}>
                    <li className="nav-item">
                      <NavLink to={StaticRoutes.KpiViewer} className="nav-link" activeClassName="active">
                        <i className="fa fa-wrench"></i>{'KPI Viewer'}
                      </NavLink>
                    </li>
                  </SecureContent>
                </ul>
              </li>
            </SecureContent>

            <SecureContent roles={[Roles.ADMIN, Roles.DEVELOPER]}>
              <li className={'nav-item nav-dropdown ' + (expanded(Sections.Documentation) ? 'open' : '')}>
                <a className="nav-link nav-dropdown-toggle" onClick={() => (toggle(Sections.Documentation), false)}>
                  {'Documentation'}
                </a>
                <ul className="nav-dropdown-items">
                  <SecureContent roles={[Roles.ADMIN, Roles.DEVELOPER]}>
                    <li className="nav-item">
                      <a href="/docs/webapp-api/index.html" className="nav-link" target="_blank">
                        <i className="fa fa-book"></i>{'SLIPO Web API'}
                      </a>
                    </li>
                  </SecureContent>
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

const mapStateToProps = (state) => ({
  expanded: state.ui.menu.expanded,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({ toggleMenu }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(Sidebar);
