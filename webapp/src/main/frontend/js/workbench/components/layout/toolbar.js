import * as React from 'react';
import * as PropTypes from 'prop-types';
import { NavLink, withRouter } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';
import { userPropType } from '../../model/prop-types/user';
import { getRoute } from '../../model/routes';

/**
 * Renders a list of links based on route configuration and user roles
 *
 * @class Toolbar
 * @extends {React.Component}
 */
class Toolbar extends React.Component {

  constructor(props) {
    super(props);
  }

  /**
   * Determines if the current user has any of the given roles
   *
   * @param {String} roles - the roles to check
   * @returns true if the user has any of the given roles or the roles array is null or empty
   * @memberof Toolbar
   */
  checkRoles(roles) {
    if ((!roles) || (roles.length === 0)) {
      return true;
    }

    const user = this.props.user;
    if (!user) {
      return false;
    }

    for (let role of roles) {
      if (user.roles.indexOf(role) !== -1) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets a list of li elements based on route configuration and user roles
   *
   * @returns a list of {@code li} elements for each route link
   * @memberof Toolbar
   */
  getLinks() {
    const route = getRoute(this.props.location.pathname);
    const links = [];

    if ((route) && (route.links)) {
      for (let path of route.links) {
        const r = getRoute(path);
        if (this.checkRoles(r.roles || [])) {
          links.push(
            <li className="nav-item px-3" key={path}>
              <NavLink to={path} className="nav-link" activeClassName="active">
                <FormattedMessage id={r.title} defaultMessage={r.defaultTitle} />
              </NavLink>
            </li>
          );
        }
      }
    }

    return (links.length === 0 ? null : links);
  }

  render() {
    return (
      <ul className="nav navbar-nav d-md-down-none mr-auto">
        {/* toggle sidebar */}
        <li className="nav-item" key="toggler">
          <button className="nav-link navbar-toggler sidebar-toggler" type="button"
            onClick={this.props.toggleSidebar}>
            <i className="fa fa-navicon"></i>
          </button>
        </li>
        {/* left-aligned top navbar items */}
        {this.getLinks()}
      </ul>
    );
  }
}

Toolbar.propTypes = {
  user: userPropType,
  toggleSidebar: PropTypes.func.isRequired,
};

export default withRouter(Toolbar);
