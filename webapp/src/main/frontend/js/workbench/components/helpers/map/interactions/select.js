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
  }

  static defaultProps = {
    width: 1,
    color: '#0D47A1',
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

  buildStyles() {
    const image = new Circle({
      radius: this.props.width + 3,
      fill: new Fill({
        color: this.props.color + '80',
      }),
      stroke: new Stroke({
        color: this.props.color,
        width: this.props.width,
      }),
    });

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
      const image = (type === 'Point' ? style.getImage() : null);
      const color = feature.get('__color') || this.props.color;

      if (image) {
        if (image.getFill()) {
          image.getFill().setColor(color + '80');
        }
        if (image.getStroke()) {
          image.getStroke().setColor(color);
        }
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
        hitTolerance: 5,
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
    if ((this.props.map) && (this.layer)) {
      this.props.map.removeInteraction(this.interaction);
      this.interaction = null;
    }
  }

  render() {
    return null;
  }
}

export default SelectInteraction;
