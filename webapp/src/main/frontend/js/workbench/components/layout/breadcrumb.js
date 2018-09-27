import * as React from 'react';
import * as ReactRedux from 'react-redux';
import { Link } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';
import { Breadcrumb as ReactBreadcrumb, BreadcrumbItem } from 'reactstrap';
import { getRoute, matchRoute } from '../../model/routes';

const MAX_LENGTH = 10; // maximum number of parts for a breadcrumb

class Breadcrumb extends React.Component {

  constructor(props) {
    super(props);
  }

  checkRoles(routeRoles, userRoles, views) {
    if (typeof routeRoles === 'function') {
      return routeRoles(userRoles, views);
    }

    if ((!routeRoles) || (routeRoles.length === 0)) {
      return true;
    }

    if ((!userRoles) || (userRoles.length === 0)) {
      return false;
    }

    for (let role of userRoles) {
      if (routeRoles.indexOf(role) !== -1) {
        return true;
      }
    }
    return false;
  }

  renderItem(path, active, title, locked) {
    return (
      <BreadcrumbItem key={path} active={active} className={locked ? 'text-danger' : ''}>
        {locked &&
          <i className="fa fa-lock mr-1"></i>
        }
        {active || locked ? title : (<Link to={path}>{title}</Link>)}
      </BreadcrumbItem>
    );
  }

  render() {
    const { location, user, views } = this.props;
    const roles = user ? user.roles : [];

    const paths = location.pathname.split('/')
      .slice(1, 1 + MAX_LENGTH)
      .reduce((res, part) => {
        if (part.length > 0) {
          var prevPath = res.length > 0 ? res[res.length - 1] : "";
          res.push(prevPath + (prevPath.endsWith("/") ? "" : "/") + part);
        }
        return res;
      }, ["/"]);

    return (
      <ReactBreadcrumb>
        {paths.map((path) => {
          const active = location.pathname === path;
          const r = getRoute(matchRoute(path));
          if (!r) {
            return null;
          }
          const title = (
            <FormattedMessage id={r.title} defaultMessage={r.defaultTitle} />
          );
          const locked = !this.checkRoles(r.roles, roles, views);
          return this.renderItem(path, active, title, locked);
        })}
      </ReactBreadcrumb>
    );
  }
}

//
// Container component
//

const mapStateToProps = (state) => ({
  user: state.user.profile,
  views: state.ui.views,
});

export default ReactRedux.connect(mapStateToProps, {})(Breadcrumb);
