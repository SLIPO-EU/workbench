import * as React from 'react';
import { Link } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';
import { Breadcrumb, BreadcrumbItem } from 'reactstrap';
import { getRoute, matchRoute } from '../../model/routes';

const MAX_LENGTH = 3; // maximum number of parts for a breadcrumb

export default ({ location }) => {
  let paths = location.pathname.split('/')
    .slice(1, 1 + MAX_LENGTH)
    .reduce((res, part) => {
      if (part.length > 0) {
        var prevPath = res.length > 0 ? res[res.length - 1] : "";
        res.push(prevPath + (prevPath.endsWith("/") ? "" : "/") + part);
      }
      return res;
    }, ["/"]);

  return (
    <Breadcrumb>
      {paths.map((path) => {
        let active = location.pathname == path;
        let r = getRoute(matchRoute(path));
        if (!r) {
          return null;
        }
        let title = (
          <FormattedMessage id={r.title} defaultMessage={r.defaultTitle} />
        );
        return (
          <BreadcrumbItem key={path} active={active}>
            {active ? title : (<Link to={path}>{title}</Link>)}
          </BreadcrumbItem>
        );
      })}
    </Breadcrumb>
  );
};
