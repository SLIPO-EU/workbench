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
   * Determines if the current user has the specific role
   *
   * @param {String} role
   * @returns
   * @memberof Toolbar
   */
  hasRole(role) {
    if (!role) {
      return true;
    }

    let user = this.props.user;
    return (user && user.roles.indexOf(role) !== -1);
  }

  /**
   * Gets a list of li elements based on route configuration and user roles
   *
   * @returns
   * @memberof Toolbar
   */
  getLinks() {
    let route = getRoute(this.props.location.pathname);
    let links = [];

    if ((route) && (route.links)) {
      for (let path of route.links) {
        let r = getRoute(path);
        if (this.hasRole(r.role)) {
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
