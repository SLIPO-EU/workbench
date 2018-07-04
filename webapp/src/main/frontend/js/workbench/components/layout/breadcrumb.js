import * as React from 'react';
import { Link } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';
import { Breadcrumb as ReactBreadcrumb, BreadcrumbItem } from 'reactstrap';
import { getRoute, matchRoute } from '../../model/routes';

const MAX_LENGTH = 10; // maximum number of parts for a breadcrumb

class Breadcrumb extends React.Component {

  constructor(props) {
    super(props);
  }

  checkRoles(routeRoles, userRoles) {
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

  render() {
    const { location, roles = [] } = this.props;

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
          const active = location.pathname == path;
          const r = getRoute(matchRoute(path));
          if (!r) {
            return null;
          }
          const title = (
            <FormattedMessage id={r.title} defaultMessage={r.defaultTitle} />
          );
          return (
            <BreadcrumbItem key={path} active={active}>
              {active || !this.checkRoles(r.roles, roles) ? title : (<Link to={path}>{title}</Link>)}
            </BreadcrumbItem>
          );
        })}
      </ReactBreadcrumb>
    );
  }
}

export default Breadcrumb;
