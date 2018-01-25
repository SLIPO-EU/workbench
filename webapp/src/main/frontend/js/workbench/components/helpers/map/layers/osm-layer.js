import * as React from 'react';
import * as PropTypes from 'prop-types';

import OpenLayersMap from 'ol/map';
import TileLayer from 'ol/layer/tile';
import OSM from 'ol/source/osm';

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
        })
      });


      this.props.map.addLayer(this.layer);
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
