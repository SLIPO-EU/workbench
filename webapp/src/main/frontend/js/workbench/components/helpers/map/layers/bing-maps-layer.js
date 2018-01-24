import * as React from 'react';
import * as PropTypes from 'prop-types';

import OpenLayersMap from 'ol/map';
import TileLayer from 'ol/layer/tile';
import BingMaps from 'ol/source/bingmaps';

/**
 * Microsoft Bing Maps layer
 *
 * @class BingMapsLayer
 * @extends {React.Component}
 */
class BingMapsLayer extends React.Component {

  constructor(props) {
    super(props);

    this.layer = null;
  }

  static propTypes = {
    map: PropTypes.instanceOf(OpenLayersMap),
    applicationKey: PropTypes.string.isRequired,
    imagerySet: PropTypes.string,
  }

  static defaultProps = {
    imagerySet: 'Aerial',
  }

  componentDidMount() {
    if (this.props.map) {
      this.layer = new TileLayer({
        source: new BingMaps({
          key: this.props.applicationKey,
          imagerySet: this.props.imagerySet || 'Aerial',
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

export default BingMapsLayer;
