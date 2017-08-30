import * as React from 'react';
import * as PropTypes from 'prop-types';
import { NavLink } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';
import { userPropType } from '../../common-prop-structs';

class ShortcutList extends React.Component {

  constructor(props) {
    super(props);
  }

  render() {
    return (
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
          <NavLink to={'/dashboard'} className="nav-link" activeClassName="active">
            <FormattedMessage id="links.dashboard" defaultMessage="Dashboard" />
          </NavLink>
        </li>
        <li className="nav-item px-3">
          <NavLink to={'/users'} className="nav-link" activeClassName="active">
            <FormattedMessage id="links.users" defaultMessage="Users" />
          </NavLink>
        </li>
        <li className="nav-item px-3">
          <NavLink to={'/settings'} className="nav-link" activeClassName="active">
            <FormattedMessage id="links.settings" defaultMessage="Settings" />
          </NavLink>
        </li>
      </ul>
    );
  }
}

ShortcutList.propTypes = {
  user: userPropType,
  toggleSidebar: PropTypes.func.isRequired,
};

export default ShortcutList;
