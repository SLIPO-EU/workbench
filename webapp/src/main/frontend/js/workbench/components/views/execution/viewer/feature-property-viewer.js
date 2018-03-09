import * as React from 'react';
import PropTypes from 'prop-types';

import {
  Card,
  CardBody,
  CardHeader,
  Col,
  Row,
} from 'reactstrap';

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

    return (
      <Card key={index}>
        <CardHeader>
          <i className="fa fa-map-marker"></i>
          <span>{`Feature ${index + 1}`}</span>
        </CardHeader>
        <CardBody>
          <Row>
            {
              keys.map((key) => {
                return (

                  <Col key={key}>
                    <div className="font-weight-bold mb-2">{key}</div>
                    <div className="font-weight-italic">{properties[key]}</div>
                  </Col>
                );
              })
            }
          </Row>
        </CardBody>
      </Card>
    );
  }

  render() {
    if (!this.props.features.length) {
      return null;
    }

    return (
      <div>
        {this.props.features.map(this.renderFeature)}
      </div>
    );
  }

}

export default FeaturePropertyViewer;
