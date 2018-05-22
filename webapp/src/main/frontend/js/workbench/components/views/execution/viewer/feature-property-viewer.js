import * as React from 'react';
import PropTypes from 'prop-types';

import {
  Card,
  CardBody,
  CardHeader,
  Col,
  Row,
} from 'reactstrap';

function isNotEmpty(value) {
  return ((value !== null) && (value !== ''));
}

class FeaturePropertyViewer extends React.Component {

  constructor(props) {
    super(props);
  }

  static propTypes = {
    features: PropTypes.arrayOf(PropTypes.object).isRequired,
  }

  renderFeature(feature, index) {
    const properties = feature.getProperties();
    const keys = Object.keys(properties)
      .filter((k) => k !== feature.getGeometryName() && !k.startsWith('__') && isNotEmpty(properties[k]));

    return (
      <Card key={index}>
        <CardHeader>
          <i className="fa fa-map-marker"></i>
          <span>{`Feature ${index + 1} - ${keys.length} Properties`}</span>
        </CardHeader>
        <CardBody>
          {
            keys
              .map((key) => {
                return (
                  <Row key={key}>
                    <Col>
                      <div className="font-weight-bold mb-2">{key}</div>
                      <div className="font-weight-italic">{properties[key]}</div>
                    </Col>
                  </Row>
                );
              })
          }
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
