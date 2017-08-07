const React = require('react');
const { Route, Link } = require('react-router-dom');
const { Breadcrumb, BreadcrumbItem } = require('reactstrap');

const routeInfo = require('../../route-info');

const MAX_LENGTH = 3; // maximum number of parts for a breadcrumb

module.exports = ({ location, match }) => {
  var paths = location.pathname.split('/')
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
        var active = location.pathname == path;
        var r = routeInfo.get(path);
        return r == null ? null : (
          <BreadcrumbItem key={path} active={active}>
            {active ? r.title : (<Link to={path}>{r.title}</Link>)}
          </BreadcrumbItem>
        );
      })}
    </Breadcrumb>
  );
};
