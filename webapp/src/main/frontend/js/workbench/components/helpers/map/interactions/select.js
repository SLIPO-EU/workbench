import * as React from 'react';
import * as PropTypes from 'prop-types';

import OpenLayersMap from 'ol/map';

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

  componentDidMount() {
    if (this.props.map) {
      this.interaction = new Select();

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
