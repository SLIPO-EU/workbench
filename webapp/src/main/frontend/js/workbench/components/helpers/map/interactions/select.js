import * as React from 'react';
import * as PropTypes from 'prop-types';

import OpenLayersMap from 'ol/map';

import Style from 'ol/style/style';
import Text from 'ol/style/text';
import Circle from 'ol/style/circle';
import Stroke from 'ol/style/stroke';
import Fill from 'ol/style/fill';

import GeoJSON from 'ol/format/geojson';
import Select from 'ol/interaction/select';

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
    this.styles = this.buildStyles();
  }

  static propTypes = {
    map: PropTypes.instanceOf(OpenLayersMap),
    onFeatureSelect: PropTypes.func,
    selected: PropTypes.object,
    color: PropTypes.string,
    width: PropTypes.number,
    hitTolerance: PropTypes.number,
    multi: PropTypes.bool,
  }

  static defaultProps = {
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

  buildPointStyle(color, icon, width) {
    const stroke = new Stroke({
      color,
      width,
    });
    const fill = new Fill({
      color: color + '80',
    });

    return (icon ?
      new Style({
        text: new Text({
          text: icon,
          font: 'normal 32px FontAwesome',
          fill,
          stroke,
        }),
      })
      :
      new Style({
        image: new Circle({
          radius: width,
          fill,
          stroke,
        }),
      }));
  }

  buildStyles() {
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

  buildStyleFunction() {
    return ((feature) => {
      const type = feature.getGeometry().getType();
      const style = this.styles[type];
      const color = feature.get('__color') || this.props.color;
      const icon = feature.get('__icon') || null;

      if (type === 'Point') {
        return this.buildPointStyle(color, icon, this.props.width);
      } else {
        if (style.getFill()) {
          style.getFill().setColor(color + '80');
        }
        if (style.getStroke()) {
          style.getStroke().setColor(color);
        }
      }

      return style;
    });
  }


  componentDidMount() {
    if (this.props.map) {
      const style = this.buildStyleFunction();

      this.interaction = new Select({
        multi: this.props.multi,
        hitTolerance: this.props.hitTolerance,
        style,
      });

      this.parseFeatures(this.props.selected);

      this.interaction.on('select', (e) => {
        if (typeof this.props.onFeatureSelect === 'function') {
          this.props.onFeatureSelect([...this.interaction.getFeatures().getArray()]);
        }
      }, this);

      this.props.map.addInteraction(this.interaction);
    }
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.selected != nextProps.selected) {
      this.parseFeatures(nextProps.selected);
    }
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
