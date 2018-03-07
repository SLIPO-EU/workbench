import * as React from 'react';
import * as PropTypes from 'prop-types';

import OpenLayersMap from 'ol/map';

import Style from 'ol/style/style';
import Text from 'ol/style/text';
import Circle from 'ol/style/circle';
import Stroke from 'ol/style/stroke';
import Fill from 'ol/style/fill';

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
    index: PropTypes.number,
    url: PropTypes.string.isRequired,
    typename: PropTypes.string.isRequired,
    version: PropTypes.string,
    outputFormat: PropTypes.string,
    srsName: PropTypes.string,
    icon: PropTypes.string,
    color: PropTypes.string,
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

  buildStyle() {
    if ((this.props.icon) && (this.props.color)) {

      return new Style({
        text: new Text({
          text: this.props.icon,
          font: 'normal 32px FontAwesome',
          fill: new Fill({
            color: this.props.color,
          }),
        }),
      });
    }

    return new Style({
      image: new Circle({
        radius: 5,
        fill: new Fill({
          color: 'rgba(0, 0, 255, 0.4)'
        }),
        stroke: new Stroke({
          color: 'rgba(0, 0, 255, 1.0)',
          width: 1
        })
      })
    });
  }

  componentDidMount() {
    if (this.props.map) {
      const source = new VectorSource({
        format: new GeoJSON(),
        url: this.buildRequest.bind(this),
        strategy: LoadingStrategy.bbox
      });

      const style = this.buildStyle();

      this.layer = new VectorLayer({
        source,
        style,
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

export default WfsLayer;
