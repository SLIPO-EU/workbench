import * as React from 'react';
import * as PropTypes from 'prop-types';

import OpenLayersMap from 'ol/Map';

import VectorSource from 'ol/source/Vector';
import GeoJSON from 'ol/format/GeoJSON';
import VectorLayer from 'ol/layer/Vector';

import { bbox as bboxLoadingStrategy } from 'ol/loadingstrategy';

import URI from 'urijs';

import {
  FEATURE_COLOR_PROPERTY,
  FEATURE_ICON_PROPERTY,
} from '../model/constants';

import {
  createStyle,
} from '../shared/utils';

/**
 * WFS layer
 *
 * @class WfsLayer
 * @extends {React.Component}
 */
class WfsLayer extends React.Component {

  constructor(props) {
    super(props);

    const { icon, color, style: layerStyle } = props;

    this.layer = null;
    this.styles = createStyle(icon, color, layerStyle);
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
    extra: PropTypes.object,
    filters: PropTypes.arrayOf(PropTypes.object),
    style: PropTypes.object,
    maxZoom: PropTypes.number,
  }

  static defaultProps = {
    version: '1.1.0',
    outputFormat: 'application/json',
    srsName: 'EPSG:3857',
    maxZoom: 15,
  }

  buildRequest(extent) {
    const { filters = [], outputFormat, url, srsName, typename, version } = this.props;
    const typenameParameter = (version.startsWith('2') ? 'typeNames' : 'typeName');

    return URI(url)
      .query({
        service: 'WFS',
        version,
        request: 'GetFeature',
        [typenameParameter]: typename,
        outputFormat,
        srsName,
        bbox: extent.join(',') + ',' + srsName,
        ...filters.reduce((result, filter) => {
          const name = `filter-${filter.attribute}-${filter.type}`;
          const value = filter.value;
          result[name] = value;
          return result;
        }, {}),
      })
      .toString();
  }

  buildStyleFunction() {
    return ((feature) => (this.styles[feature.getGeometry().getType()] || this.styles['Point']));
  }

  componentDidMount() {
    const { color, icon, index, extra, map, maxZoom } = this.props;

    if (map) {
      const source = new VectorSource({
        format: new GeoJSON(),
        url: this.buildRequest.bind(this),
        strategy: bboxLoadingStrategy,
      });

      source.on('addfeature', (e) => {
        e.feature.set(FEATURE_COLOR_PROPERTY, color || null, true);
        e.feature.set(FEATURE_ICON_PROPERTY, icon || null, true);
        if (extra) {
          Object.keys(extra).map((key) => {
            e.feature.set(key, extra[key] || null, true);
          });
        }
      }, this);

      const style = this.buildStyleFunction();

      this.layer = new VectorLayer({
        source,
        style,
        maxResolution: map.getView().getResolutionForZoom(maxZoom),
      });

      map.getLayers().insertAt(index, this.layer);
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
