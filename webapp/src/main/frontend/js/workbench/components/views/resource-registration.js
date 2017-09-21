import * as React from 'react';

import {
  Card,
  CardBlock,
  Col,
  Row,
} from 'reactstrap';

import ResourceWizard from './resource-wizard-example';

/**
 * Register a new resource
 *
 * @class ResourceRegistration
 * @extends {React.Component}
 */
class ResourceRegistration extends React.Component {

  render() {
    return (
      <Row>
        <Col sm="12" md="12" lg="6">
          <Card>
            <CardBlock className="card-body">
              <ResourceWizard />
            </CardBlock>
          </Card>
        </Col>
      </Row>
    );
  }

}

export default ResourceRegistration;
