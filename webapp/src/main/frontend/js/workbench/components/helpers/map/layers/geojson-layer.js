import * as React from 'react';
import * as PropTypes from 'prop-types';

import OpenLayersMap from 'ol/map';

import Style from 'ol/style/style';
import Stroke from 'ol/style/stroke';

import VectorSource from 'ol/source/vector';
import GeoJSON from 'ol/format/geojson';

import VectorLayer from 'ol/layer/vector';

/**
 * Vector layer from GeoJSON data
 *
 * @class GeoJsonLayer
 * @extends {React.Component}
 */
class GeoJsonLayer extends React.Component {

  constructor(props) {
    super(props);

    this.layer = null;
  }

  static propTypes = {
    map: PropTypes.instanceOf(OpenLayersMap),
    index: PropTypes.number,
    features: PropTypes.object,
    fitToExtent: PropTypes.bool,
  }

  static defaultProps = {
    fitToExtent: true,
  }

  parseFeatures(features, fitToExtent) {
    if (!this.layer) {
      return;
    }

    const source = this.layer.getSource();
    source.clear();

    if (!features) {
      return;
    }

    const format = new GeoJSON();
    source.addFeatures(format.readFeatures(features, {
      featureProjection: 'EPSG:3857',
    }));

    if ((fitToExtent) && (source.getFeatures().length > 0)) {
      this.props.map.getView().fit(source.getExtent());
    }
  }

  componentDidMount() {
    if (this.props.map) {
      const source = new VectorSource();

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

      this.parseFeatures(this.props.features, this.props.fitToExtent);

      this.props.map.getLayers().insertAt(this.props.index, this.layer);
    }
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.features != nextProps.features) {
      this.parseFeatures(nextProps.features, nextProps.fitToExtent);
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

export default GeoJsonLayer;
