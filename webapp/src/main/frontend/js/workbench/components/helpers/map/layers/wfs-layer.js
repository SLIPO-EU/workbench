import * as React from 'react';
import * as PropTypes from 'prop-types';

import OpenLayersMap from 'ol/map';

import Style from 'ol/style/style';
import Stroke from 'ol/style/stroke';

import VectorSource from 'ol/source/vector';
import GeoJSON from 'ol/format/geojson';
import LoadingStrategy from 'ol/loadingstrategy';

import VectorLayer from 'ol/layer/vector';

import URI from 'urijs';

/**
 * WFS layer
 *
 * @class WfsLayer
 * @extends {React.Component}
 */
class WfsLayer extends React.Component {

  constructor(props) {
    super(props);

    this.layer = null;
  }

  static propTypes = {
    map: PropTypes.instanceOf(OpenLayersMap),
    url: PropTypes.string.isRequired,
    typename: PropTypes.string.isRequired,
    version: PropTypes.string,
    outputFormat: PropTypes.string,
    srsName: PropTypes.string,
  }

  static defaultProps = {
    version: '1.1.0',
    outputFormat: 'application/json',
    srsName: 'EPSG:3857',
  }

  buildRequest(extent) {
    const typenameParameter = (this.props.version.startsWith('2') ? 'typeNames' : 'typeName');

    return URI(this.props.url)
      .query({
        service: 'WFS',
        version: this.props.version,
        request: 'GetFeature',
        [typenameParameter]: this.props.typename,
        outputFormat: this.props.outputFormat,
        srsName: this.props.srsName,
        bbox: extent.join(',') + ',' + this.props.srsName,
      })
      .toString();
  }

  componentDidMount() {
    if (this.props.map) {
      const source = new VectorSource({
        format: new GeoJSON(),
        url: this.buildRequest.bind(this),
        strategy: LoadingStrategy.bbox
      });

      const style = new Style({
        stroke: new Stroke({
          color: 'rgba(0, 0, 255, 1.0)',
          width: 2
        })
      });

      this.layer = new VectorLayer({
        source,
        style,
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

export default WfsLayer;
