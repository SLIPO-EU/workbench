import * as React from 'react';
import * as PropTypes from 'prop-types';
import { NavLink, withRouter } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';
import { userPropType } from '../../common-prop-structs';
import { getRouteFromPath, getRouteFromName } from '../../model/routes';

/**
 * Renders a list of links based on route configuration and user roles
 * 
 * @class ShortcutList
 * @extends {React.Component}
 */
class ShortcutList extends React.Component {

  constructor(props) {
    super(props);
  }

  /**
   * Determines if the current user has the specific role
   * 
   * @param {String} role 
   * @returns 
   * @memberof ShortcutList
   */
  hasRole(role) {
    if (!role) {
      return true;
    }

    let user = this.props.user;
    return (user && user.profile.roles.indexOf(role) !== -1);
  }

  /**
   * Gets a list of li elements based on route configuration and user roles
   * 
   * @returns 
   * @memberof ShortcutList
   */
  getLinks() {
    let route = getRouteFromPath(this.props.location.pathname);
    let links = [];

    if ((route) && (route.links)) {
      for (let name of route.links) {
        let r = getRouteFromName(name);
        if (this.hasRole(r.role)) {
          links.push(
            <li className="nav-item px-3" key={r.name}>
              <NavLink to={r.path} className="nav-link" activeClassName="active">
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

ShortcutList.propTypes = {
  user: userPropType,
  toggleSidebar: PropTypes.func.isRequired,
};

// Component must have access to router properties location and match
export default withRouter(ShortcutList);
