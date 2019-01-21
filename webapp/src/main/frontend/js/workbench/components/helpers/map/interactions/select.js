import * as React from 'react';
import * as PropTypes from 'prop-types';

import OpenLayersMap from 'ol/map';

import Style from 'ol/style/style';
import Circle from 'ol/style/circle';
import Stroke from 'ol/style/stroke';
import Fill from 'ol/style/fill';

import GeoJSON from 'ol/format/geojson';
import Select from 'ol/interaction/select';

import {
  FEATURE_LAYER_PROPERTY,
} from '../model/constants';

import {
  createStyle,
} from '../shared/utils';

/**
 * Select interaction
 *
 * @class SelectInteraction
 * @extends {React.Component}
 */
class SelectInteraction extends React.Component {

  constructor(props) {
    super(props);

    this.interaction = null;
    this.defaultStyles = this.buildDefaultStyles();
    this.styles = this.buildStyles(props.styles);
  }

  static propTypes = {
    active: PropTypes.bool,
    map: PropTypes.instanceOf(OpenLayersMap),
    onFeatureSelect: PropTypes.func,
    selected: PropTypes.object,
    color: PropTypes.string,
    width: PropTypes.number,
    hitTolerance: PropTypes.number,
    multi: PropTypes.bool,
    styles: PropTypes.object,
  }

  static defaultProps = {
    active: true,
    width: 1,
    color: '#0D47A1',
    hitTolerance: 5,
    multi: true,
  }

  parseFeatures(selected) {
    this.interaction.getFeatures().clear();

    if (!selected) {
      return;
    }

    const format = new GeoJSON();
    const features = format.readFeatures(selected, {
      featureProjection: 'EPSG:3857',
    });

    for (let index in features) {
      this.interaction.getFeatures().push(features[index]);
    }
  }

  buildDefaultStyles() {
    const stroke = new Stroke({
      color: this.props.color,
      width: this.props.width,
    });

    const fill = new Fill({
      color: this.props.color + '80',
    });

    const style = new Style({
      fill,
      stroke,
    });

    const image = new Circle({
      radius: this.props.width,
      fill,
      stroke,
    });

    const styles = {
      'Point': new Style({
        image,
      }),
      'MultiPoint': new Style({
        image,
      }),
      'LineString': style,
      'MultiLineString': style,
      'Polygon': style,
      'MultiPolygon': style,
    };

    return styles;
  }

  buildStyles(styles) {
    const { icon, color } = this.props;
    const result = {};

    if (!styles) {
      return result;
    }

    Object.keys(styles).forEach(key => {
      const layerStyle = styles[key];

      result[key] = createStyle(icon, color, layerStyle, true, 1.4);
    });

    return result;
  }


  buildStyleFunction() {
    return ((feature) => {
      const type = feature.getGeometry().getType();
      const layer = feature.get(FEATURE_LAYER_PROPERTY);
      const styleMap = this.styles[layer];

      return (styleMap ? styleMap[type] : this.defaultStyles[type]);
    });
  }

  componentDidMount() {
    const { active, hitTolerance, map, multi, onFeatureSelect, selected } = this.props;

    if (map) {
      const style = this.buildStyleFunction();

      this.interaction = new Select({
        multi,
        hitTolerance,
        style,
      });

      this.parseFeatures(selected);

      this.interaction.on('select', () => {
        if (typeof onFeatureSelect === 'function') {
          onFeatureSelect([...this.interaction.getFeatures().getArray()]);
        }
      }, this);

      map.addInteraction(this.interaction);
      this.interaction.setActive(active);
    }
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.selected != nextProps.selected) {
      this.parseFeatures(nextProps.selected);
    }
    if (this.props.active != nextProps.active) {
      this.interaction.setActive(nextProps.active);
    }
    this.styles = nextProps.styles ? this.buildStyles(nextProps.styles) : this.styles;
  }

  componentWillUnmount() {
    if ((this.props.map) && (this.interaction)) {
      this.props.map.removeInteraction(this.interaction);
      this.interaction = null;
    }
  }

  render() {
    return null;
  }

}

export default SelectInteraction;
