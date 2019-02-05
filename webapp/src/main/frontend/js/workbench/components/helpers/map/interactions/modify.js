
import * as React from 'react';
import * as PropTypes from 'prop-types';

import Map from 'ol/map';
import Feature from 'ol/feature';
import GeometryType from 'ol/geom/geometrytype';
import Collection from 'ol/collection';
import Modify from 'ol/interaction/modify';
import Translate from 'ol/interaction/translate';

import Style from 'ol/style/style';
import Circle from 'ol/style/circle';
import Stroke from 'ol/style/stroke';
import Fill from 'ol/style/fill';

import {
  FEATURE_LAYER_PROPERTY,
} from '../model/constants';

import {
  createStyleForSymbol,
} from '../shared/utils';

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
        onGeometryChange();
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
    const styles = {};

    const white = '#ffffff';
    const black = '#424242';
    const width = 3;

    const { styles: layerStyles } = this.props;
    const layerName = feature.get(FEATURE_LAYER_PROPERTY);
    const layerStyle = layerStyles ? layerStyles[layerName] : null;

    const image = layerStyle ?
      createStyleForSymbol(
        layerStyle.symbol,
        black,
        3,
        white,
        layerStyle.size,
        50,
      ) :
      new Circle({
        radius: width * 2,
        fill: new Fill({
          color: black
        }),
        stroke: new Stroke({
          color: white,
          width: width / 2
        })
      });

    styles[GeometryType.POLYGON] = [
      new Style({
        fill: new Fill({
          color: [255, 255, 255, 0.5]
        }),
        stroke: new Stroke({
          color: black,
          width: width
        }),
      })
    ];

    styles[GeometryType.MULTI_POLYGON] = styles[GeometryType.POLYGON];

    styles[GeometryType.LINE_STRING] = [
      new Style({
        stroke: new Stroke({
          color: white,
          width: width + 2
        })
      }),
      new Style({
        stroke: new Stroke({
          color: black,
          width: width
        })
      })
    ];

    styles[GeometryType.MULTI_LINE_STRING] = styles[GeometryType.LINE_STRING];

    styles[GeometryType.POINT] = [
      new Style({
        image,
        zIndex: Infinity
      })
    ];

    styles[GeometryType.MULTI_POINT] = styles[GeometryType.POINT];

    styles[GeometryType.CIRCLE] = styles[GeometryType.POLYGON].concat(styles[GeometryType.LINE_STRING]);

    styles[GeometryType.GEOMETRY_COLLECTION] =
      styles[GeometryType.POLYGON].concat(
        styles[GeometryType.LINE_STRING],
        styles[GeometryType.POINT]
      );

    return styles;
  }

  buildInteractionStyles() {
    return ((feature) => {
      const styles = this.buildFeatureStyles(feature);
      const type = feature.getGeometry().getType();

      return styles[type];
    });
  }

  setFeatureStyle(feature) {
    const styles = this.buildFeatureStyles(feature);
    const type = feature.getGeometry().getType();
    const style = styles[type];

    feature.setStyle(style);
  }

  render() {
    return null;
  }

}

export default ModifyInteraction;
