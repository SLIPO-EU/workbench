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
  }

  static propTypes = {
    map: PropTypes.instanceOf(OpenLayersMap),
    onFeatureSelect: PropTypes.func,
    selected: PropTypes.object,
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


  buildStyle() {
    if (this.props.icon) {
      return new Style({
        text: new Text({
          text: this.props.icon,
          font: 'normal 32px FontAwesome',
        }),
      });
    }

    return new Style({
      image: new Circle({
        radius: 5,
        fill: new Fill({
          color: 'rgba(0, 0, 255, 0.4)'
        }),
        stroke: new Stroke({
          color: 'rgba(0, 0, 255, 1.0)',
          width: 1
        })
      })
    });
  }

  componentDidMount() {
    if (this.props.map) {
      this.interaction = new Select({
        style: this.buildStyle(),
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
