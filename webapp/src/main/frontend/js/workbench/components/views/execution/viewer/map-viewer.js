import * as React from 'react';
import PropTypes from 'prop-types';

import {
  Card,
  CardBody,
  Col,
  Row,
} from 'reactstrap';

import OpenLayers from '../../../helpers/map';

class MapViewer extends React.Component {

  constructor(props) {
    super(props);

    this.onFeatureSelect = this.onFeatureSelect.bind(this);
  }

  static propTypes = {
    selectFeatures: PropTypes.func.isRequired,
  }

  /**
   * Display selected feature properties
   *
   * @param {any} features
   * @memberof MapViewer
   */
  onFeatureSelect(features) {
    this.props.selectFeatures(features);
  }

  render() {
    return (
      <Card>
        <CardBody>
          <OpenLayers.Map minZoom={5} maxZoom={15} zoom={12} center={[-8908887.277395891, 5381918.072437216]}>
            <OpenLayers.Layers>
              <OpenLayers.Layer.OSM
                url="http://tile.stamen.com/terrain/{z}/{x}/{y}.jpg"
              />
              <OpenLayers.Layer.WFS
                url=""
                version="1.1.0"
                typename=""
              />
            </OpenLayers.Layers>
            <OpenLayers.Interactions>
              <OpenLayers.Interaction.Select
                onFeatureSelect={this.onFeatureSelect}
              />
            </OpenLayers.Interactions>
          </OpenLayers.Map>
          <div className="small text-muted">Hold Shift to select multiple features</div>
        </CardBody>
      </Card>
    );
  }

}

export default MapViewer;
