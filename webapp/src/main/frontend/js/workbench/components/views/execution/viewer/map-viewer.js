import * as React from 'react';
import PropTypes from 'prop-types';
import moment from 'moment';

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
  }

  static propTypes = {
  };

  render() {
    return (
      <Card>
        <CardBody>
          <OpenLayers.Map minZoom={5} maxZoom={15} zoom={1}>
            <OpenLayers.Layers>
              <OpenLayers.Layer.OSM
                url="http://tile.stamen.com/terrain/{z}/{x}/{y}.jpg"
              />
            </OpenLayers.Layers>
          </OpenLayers.Map>
        </CardBody>
      </Card>
    );
  }

}

export default MapViewer;
