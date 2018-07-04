import * as React from 'react';
import { withRouter } from 'react-router';

import { matchRoute, getRoute } from '../../model/routes';

import Placeholder from './placeholder';

/**
 * A component for displaying contextual information based on the current
 * router state
 *
 * @class ContextSidebar
 * @extends {React.Component}
 */
class ContextSidebar extends React.Component {

  _getComponent() {
    let route = getRoute(matchRoute(this.props.location.pathname));
    if ((route) && (route.contextComponent)) {
      return route.contextComponent;
    }
    return null;
  }

  render() {
    let ContextComponent = this._getComponent();

    return (
      <div style={{ height: '100%' }}>
        {ContextComponent ?
          <ContextComponent {...this.props} />
          :
          <Placeholder style={{ height: '100%' }} label="Context" iconClass="fa fa-magic" />
        }
      </div>
    );
  }

}

export default withRouter(ContextSidebar);
