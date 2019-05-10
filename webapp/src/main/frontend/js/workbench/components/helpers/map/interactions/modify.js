
import * as React from 'react';
import * as PropTypes from 'prop-types';

import Map from 'ol/Map';
import Feature from 'ol/Feature';
import Collection from 'ol/Collection';
import Modify from 'ol/interaction/Modify';
import Translate from 'ol/interaction/Translate';

import { default as GeometryType } from 'ol/geom/GeometryType';

import {
  FEATURE_LAYER_PROPERTY,
} from '../model/constants';

import {
  createStyle,
} from '../shared/utils';

import {
  EnumSymbol,
} from '../../../../model/map-viewer';

/**
 * Modify interaction
 *
 * @class ModifyInteraction
 * @extends {React.Component}
 */
class ModifyInteraction extends React.Component {

  constructor(props) {
    super(props);

    // OpenLayers interactions added to the map instance by this component
    this.interactions = [];
    // Feature collection for the OpenLayers Modify interaction
    this.features = new Collection();
  }

  static propTypes = {
    // Enable/Disable interaction
    active: PropTypes.bool,
    // Feature to edit
    feature: PropTypes.instanceOf(Feature),
    // Map instance
    map: PropTypes.instanceOf(Map),
    // True if Translate interaction is enabled
    translate: PropTypes.bool.isRequired,
  }

  static defaultProps = {
    translate: false,
  }

  componentDidMount() {
    const { active, feature = null, map, onGeometryChange, translate } = this.props;

    // Wait for map instance to initialize
    if (!map) {
      return;
    }

    const style = this.buildInteractionStyles();

    if (feature) {
      this.setFeatureStyle(feature);
      this.features.push(feature);
    }

    this.interactions.push(new Modify({
      features: this.features,
      style,
    }));

    this.interactions[0].on('modifyend', (e) => {
      if (typeof onGeometryChange === 'function') {
        onGeometryChange(e.features || null);
      }
    }, this);

    if (translate) {
      this.interactions.push(new Translate({
        features: this.features,
      }));
    }

    this.interactions.forEach(i => {
      i.setActive(active);
      map.addInteraction(i);
    });
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.feature != nextProps.feature) {
      this.features.clear();
      if (nextProps.feature) {
        this.setFeatureStyle(nextProps.feature);
        this.features.push(nextProps.feature);
      }
    }
    if (this.props.active != nextProps.active) {
      this.interactions.forEach(i => i.setActive(nextProps.active));
    }
  }

  componentWillUnmount() {
    const { map } = this.props;

    if (map) {
      this.interactions.forEach(i => map.removeInteraction(i));
      this.interactions = null;
      this.features.clear();
      this.features = null;
    }
  }

  buildFeatureStyles(feature) {
    const {
      fillColor = '#ffffff',
      fontColor = null,
      icon = null,
      strokeColor = '#424242',
      width = 3,
    } = this.props;

    const layerStyle = {
      symbol: EnumSymbol.Square,
      stroke: {
        color: strokeColor,
        width,
      },
      fill: {
        color: fillColor
      },
      opacity: 50,
      size: 40,
    };

    return createStyle(icon, fontColor, layerStyle, false);
  }

  buildInteractionStyles() {
    return ((feature) => {
      const styles = this.buildFeatureStyles(feature);
      const type = feature.getGeometry() ? feature.getGeometry().getType() : GeometryType.POINT;

      return styles[type];
    });
  }

  setFeatureStyle(feature) {
    const styles = this.buildFeatureStyles(feature);
    const type = feature.getGeometry() ? feature.getGeometry().getType() : GeometryType.POINT;
    const style = styles[type];

    feature.setStyle(style);
  }

  render() {
    return null;
  }

}

export default ModifyInteraction;
