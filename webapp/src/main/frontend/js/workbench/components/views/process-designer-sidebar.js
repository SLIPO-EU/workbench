import * as React from 'react';
import {
  Row, Col,
} from 'reactstrap';

import Placeholder from './placeholder';

/**
 * Sidebar for process designer
 *
 * @class ProcessDesignerSidebar
 * @extends {React.Component}
 */
class ProcessDesignerSidebar extends React.Component {

  render() {
    return (
      <div style={{ height: '100%' }}>
        <Row style={{ height: '44vh' }} className="mb-2">
          <Col>
            <Placeholder label="Resources" iconClass="fa fa-list" />
          </Col>
        </Row>
        <Row style={{ height: '44vh' }}>
          <Col>
            <Placeholder label="Steps" iconClass="fa fa-cogs" />
          </Col>
        </Row>
      </div >
    );
  }

}

export default ProcessDesignerSidebar;
