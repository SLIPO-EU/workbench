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

import {
  FEATURE_LAYER_PROPERTY,
  FEATURE_COLOR_PROPERTY,
  FEATURE_ICON_PROPERTY,
} from '../model/constants';

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
    this.styles = this.buildStyles();
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

  buildStyles() {
    const image = new Circle({
      radius: 5,
      fill: new Fill({
        color: this.props.color + '4D',
      }),
      stroke: new Stroke({
        color: this.props.color,
        width: 2
      }),
    });

    const stroke = new Stroke({
      color: this.props.color,
      width: 1
    });

    const fill = new Fill({
      color: this.props.color + '4D',
    });

    const style = new Style({
      fill,
      stroke,
    });

    const styles = {
      'Point': (
        this.props.icon ?
          new Style({
            text: new Text({
              text: this.props.icon,
              font: 'normal 32px FontAwesome',
              fill: new Fill({
                color: this.props.color,
              }),
            }),
          })
          :
          new Style({
            image: image,
          })),
      'MultiPoint': (
        this.props.icon ?
          new Style({
            text: new Text({
              text: this.props.icon,
              font: 'normal 32px FontAwesome',
              fill: new Fill({
                color: this.props.color,
              }),
            }),
          })
          :
          new Style({
            image: image,
          })),
      'LineString': style,
      'MultiLineString': style,
      'Polygon': style,
      'MultiPolygon': style,
    };

    return styles;
  }

  buildStyleFunction() {
    return ((feature) => (this.styles[feature.getGeometry().getType()] || this.styles['Point']));
  }

  componentDidMount() {
    if (this.props.map) {
      const source = new VectorSource({
        format: new GeoJSON(),
        url: this.buildRequest.bind(this),
        strategy: LoadingStrategy.bbox,
      });

      source.on('addfeature', (e) => {
        e.feature.set(FEATURE_LAYER_PROPERTY, this.props.typename, true);
        e.feature.set(FEATURE_COLOR_PROPERTY, this.props.color || null, true);
        e.feature.set(FEATURE_ICON_PROPERTY, this.props.icon || null, true);
      }, this);

      const style = this.buildStyleFunction();

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
