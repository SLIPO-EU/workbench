import * as React from 'react';
import * as PropTypes from 'prop-types';

import OpenLayersMap from 'ol/map';

/**
 * A collection of map interactions
 *
 * @class Interactions
 * @extends {React.Component}
 */
class Interactions extends React.Component {

  constructor(props) {
    super(props);
  }

  static propTypes = {
    map: PropTypes.instanceOf(OpenLayersMap),
  }

  render() {
    const children = this.props.children;
    const map = this.props.map;

    if (!map) {
      return null;
    }

    if (!children) {
      return null;
    }

    return (
      React.Children.map(children, (child, index) => {
        return React.cloneElement(child, {
          map: this.props.map,
        });
      }, this)
    );
  }
}

export default Interactions;
