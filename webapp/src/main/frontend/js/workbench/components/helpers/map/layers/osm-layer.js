import * as React from 'react';
import * as PropTypes from 'prop-types';

import OpenLayersMap from 'ol/Map';
import TileLayer from 'ol/layer/Tile';
import OSM from 'ol/source/OSM';

/**
 * OSM layer
 *
 * @class OsmLayer
 * @extends {React.Component}
 */
class OsmLayer extends React.Component {

  constructor(props) {
    super(props);

    this.layer = null;
  }

  static propTypes = {
    map: PropTypes.instanceOf(OpenLayersMap),
    index: PropTypes.number,
    url: PropTypes.string,
  }

  static defaultProps = {
    url: 'https://{a-c}.tile.openstreetmap.org/{z}/{x}/{y}.png',
  }

  componentDidMount() {
    if (this.props.map) {
      this.layer = new TileLayer({
        source: new OSM({
          url: this.props.url,
        }),
      });

      this.props.map.getLayers().insertAt(this.props.index, this.layer);
    }
  }

  componentWillUnmount() {
    if ((this.props.map) && (this.layer)) {
      this.props.map.removeLayer(this.layer);
      this.layer = null;
    }
  }

  render() {
    return null;
  }
}

export default OsmLayer;
