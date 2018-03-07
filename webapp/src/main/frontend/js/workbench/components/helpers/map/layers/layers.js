import * as React from 'react';
import * as PropTypes from 'prop-types';

import OpenLayersMap from 'ol/map';

/**
 * A collection of map layers
 *
 * @class Layers
 * @extends {React.Component}
 */
class Layers extends React.Component {

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
          index,
        });
      }, this)
    );
  }
}

export default Layers;
