
import * as React from 'react';
import * as PropTypes from 'prop-types';

import Map from 'ol/Map';
import Feature from 'ol/Feature';
import Collection from 'ol/Collection';
import Draw from 'ol/interaction/Draw';
import Translate from 'ol/interaction/Translate';

import {
  createStyle,
} from '../shared/utils';

import {
  EnumSymbol,
} from '../../../../model/map-viewer';

/**
 * Draw interaction
 *
 * @class DrawInteraction
 * @extends {React.Component}
 */
class DrawInteraction extends React.Component {

  constructor(props) {
    super(props);

    // OpenLayers interactions added to the map instance by this component
    this.interactions = [];
    // Feature collection for the OpenLayers Draw interaction
    this.features = new Collection();
  }

  static propTypes = {
    // Enable/Disable interaction
    active: PropTypes.bool,
    // Feature to edit
    feature: PropTypes.instanceOf(Feature),
    // Map instance
    map: PropTypes.instanceOf(Map),
    // Allow drawing only a single feature
    single: PropTypes.bool,
    // True if Translate interaction is enabled
    translate: PropTypes.bool,
  }

  static defaultProps = {
    translate: false,
    single: true,
  }

  createInteractions(type, active) {
    const { map, onDrawEnd, onDrawStart, translate, single } = this.props;

    this.removeInteractions();

    const style = this.buildInteractionStyles();

    this.interactions.push(new Draw({
      features: this.features,
      type,
      style,
    }));

    this.interactions[0].on('drawstart', (e) => {
      if (single) {
        this.features.clear();
      }
      if (typeof onDrawStart === 'function') {
        onDrawEnd(e.feature);
      }
    }, this);

    this.interactions[0].on('drawend', (e) => {
      if (e.feature) {
        this.features.push(e.feature);
      }
      if (typeof onDrawEnd === 'function') {
        onDrawEnd(this.features);
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

  removeInteractions() {
    const { map } = this.props;

    if (map) {
      this.interactions.forEach(i => map.removeInteraction(i));
      this.interactions = [];
    }
  }

  componentDidMount() {
    const { active, feature = null, map, type } = this.props;

    // Wait for map instance to initialize
    if (!map) {
      return;
    }

    if (feature) {
      this.setFeatureStyle(feature);
      this.features.push(feature);
    }

    this.createInteractions(type, active);
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
    if (this.props.type != nextProps.type) {
      this.createInteractions(nextProps.type, nextProps.active);
    }
  }

  componentWillUnmount() {
    this.removeInteractions();

    this.features.clear();
    this.features = null;
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
      opacity: 70,
      size: 20,
    };

    return createStyle(icon, fontColor, layerStyle, false);
  }

  buildInteractionStyles() {
    return ((feature) => {
      const styles = this.buildFeatureStyles(feature);
      const type = feature.getGeometry() ? feature.getGeometry().getType() : null;

      return styles[type];
    });
  }

  setFeatureStyle(feature) {
    const styles = this.buildFeatureStyles(feature);
    const type = feature.getGeometry() ? feature.getGeometry().getType() : null;
    const style = styles[type];

    feature.setStyle(style);
  }

  render() {
    return null;
  }

}

export default DrawInteraction;
