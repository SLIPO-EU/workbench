import * as React from 'react';
import PropTypes from 'prop-types';

import {
  Card,
  CardBody,
  CardHeader,
  Col,
  Row,
} from 'reactstrap';

import {
  Table,
} from '../../../helpers';

export const PropertyGridColumns = [{
  Header: 'Property',
  accessor: 'key',
  headerStyle: { 'textAlign': 'left' },
}, {
  Header: 'Value',
  accessor: 'value',
  headerStyle: { 'textAlign': 'center' },
  style: { 'textAlign': 'center' },
}];

class FeaturePropertyViewer extends React.Component {

  constructor(props) {
    super(props);
  }

  static propTypes = {
    features: PropTypes.arrayOf(PropTypes.object).isRequired,
  }

  renderFeature(feature, index) {
    const properties = feature.getProperties();
    const keys = Object.keys(properties).filter((k) => k !== feature.getGeometryName());
    //map((k) => ({ key: k, value: properties[k] }));
    return (
      <div class="col-sm-6 col-lg-4" key={index}>
        <Card>
          <CardHeader>
            <i className="fa fa-map-marker"></i>
            <span>{`Feature ${index + 1}`}</span>
          </CardHeader>
          <CardBody>
            <Row>
              {
                keys.map((key) => {
                  return (

                    <div class="col-sm-6 col-md-4" key={key}>
                      <div className="font-weight-bold mb-2">{key}</div>
                      <div className="font-weight-italic">{properties[key]}</div>
                    </div>
                  );
                })
              }
            </Row>
          </CardBody>
        </Card>
      </div >
    );
  }

  render() {
    if (!this.props.features.length) {
      return null;
    }

    return (
      <div class="row">
        {this.props.features.map(this.renderFeature)}
      </div>
    );
  }

}

export default FeaturePropertyViewer;
