import * as React from 'react';
import { withRouter } from 'react-router';
import * as PropTypes from 'prop-types';

import { matchRoute, getRoute } from '../../model/routes';

/**
 * Simple button for displaying aside menu. The button is shown only
 * when the active route has a context component assigned.
 *
 * @class AsideToggle
 * @extends {React.Component}
 */
class AsideToggle extends React.Component {

  _getComponent() {
    let route = getRoute(matchRoute(this.props.location.pathname));
    if ((route) && (route.contextComponent)) {
      return route.contextComponent;
    }
    return null;
  }

  componentDidMount() {
    this.props.setAsideMenuVisibility(this._getComponent());
  }

  render() {
    this.contextComponent = this._getComponent();

    if (this.contextComponent) {
      return (
        <li className="nav-item d-md-down-none">
          <button className="nav-link navbar-toggler aside-menu-toggler" type="button"
            onClick={this.props.toggleAsideMenu}>
            <i className="fa fa-navicon"></i>
          </button>
        </li>
      );
    }

    return null;
  }

}

AsideToggle.propTypes = {
  setAsideMenuVisibility: PropTypes.func.isRequired,
  toggleAsideMenu: PropTypes.func.isRequired,
};

module.exports = withRouter(AsideToggle);
