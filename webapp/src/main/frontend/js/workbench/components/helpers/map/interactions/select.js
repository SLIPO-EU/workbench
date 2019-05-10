import * as React from 'react';
import * as PropTypes from 'prop-types';

import OpenLayersMap from 'ol/Map';

import GeoJSON from 'ol/format/GeoJSON';
import Select from 'ol/interaction/Select';
import Feature from 'ol/Feature';

import { click as clickCondition, noModifierKeys as noModifierKeysCondition } from 'ol/events/condition';

import {
  FEATURE_LAYER_PROPERTY,
} from '../model/constants';

import {
  createStyle, mergeExtent,
} from '../shared/utils';

import {
  EnumSymbol,
} from '../../../../model/map-viewer';

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
    color: PropTypes.string,
    fitToExtent: PropTypes.bool,
    hitTolerance: PropTypes.number,
    map: PropTypes.instanceOf(OpenLayersMap),
    multi: PropTypes.bool,
    onFeatureSelect: PropTypes.func,
    selected: PropTypes.object,
    styles: PropTypes.object,
    width: PropTypes.number,
  }

  static defaultProps = {
    active: true,
    color: '#0D47A1',
    fitToExtent: false,
    hitTolerance: 5,
    multi: true,
    width: 1,
  }

  parseFeatures(selected, fitToExtent) {
    const features = this.interaction.getFeatures();
    features.clear();

    if (!selected) {
      return;
    }

    // Handle selected type
    if (selected instanceof Feature) {
      // The selection is a single OpenLayers Feature instance
      features.push(selected);
    } else {
      // By default attempt to parse JSON
      const format = new GeoJSON();
      const features = format.readFeatures(selected, {
        featureProjection: 'EPSG:3857',
      });

      for (let index in features) {
        features.push(features[index]);
      }
    }

    // Fit map to the selected features
    if ((fitToExtent) && (features)) {
      const extent = mergeExtent(features);

      if (extent) {
        this.props.map.getView().fit(extent);
      }
    }
  }

  buildDefaultStyles() {
    const { icon = null, color, width } = this.props;

    const layerStyle = {
      symbol: EnumSymbol.Square,
      stroke: {
        color,
        width,
      },
      fill: {
        color,
      },
      opacity: 50,
      size: 20,
    };

    return createStyle(icon, color, layerStyle, false);
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
      const type = feature.getGeometry() ? feature.getGeometry().getType() : null;
      const layer = feature.get(FEATURE_LAYER_PROPERTY);
      const styleMap = this.styles[layer];

      return (styleMap ? styleMap[type] : this.defaultStyles[type]);
    });
  }

  componentDidMount() {
    const { active, hitTolerance, map, multi, onFeatureSelect, selected, fitToExtent } = this.props;

    if (map) {
      const style = this.buildStyleFunction();

      this.interaction = new Select({
        condition: (e) => {
          return clickCondition(e) && noModifierKeysCondition(e);
        },
        multi,
        hitTolerance,
        style,
      });

      this.parseFeatures(selected, fitToExtent);

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
      this.parseFeatures(nextProps.selected, nextProps.fitToExtent);
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

  clear() {
    if (this.interaction) {
      this.interaction.getFeatures().clear();
    }
  }

  render() {
    return null;
  }

}

export default SelectInteraction;
