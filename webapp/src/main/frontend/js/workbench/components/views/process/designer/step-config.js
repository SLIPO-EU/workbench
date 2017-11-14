import * as React from 'react';
import PropTypes from 'prop-types';
import {
  Button, Card, CardBlock, Row, Col,
} from 'reactstrap';

/**
 * Presentational component that wraps the step configuration options
 *
 * @class StepConfig
 * @extends {React.Component}
 */
class StepConfig extends React.Component {

  constructor(props) {
    super(props);
  }

  static propTypes = {
    step: PropTypes.object.isRequired,
    configuration: PropTypes.object,
    configureStepEnd: PropTypes.func.isRequired,
  }

  save(e) {
    // TODO: Return a valid configuration
    this.props.configureStepEnd(this.props.step, {});
  }

  cancel(e) {
    this.props.configureStepEnd(this.props.step, null);
  }

  render() {
    return (
      <Card>
        <CardBlock className="card-body">
          <Row className="mb-2">
            <Col>
              <i className={this.props.step.iconClass + ' mr-2'}></i><span>{this.props.step.title}</span>
            </Col>
          </Row>
          <Row className="mb-2">
            <Col>
              <Button color="danger" onClick={(e) => { this.cancel(e); }} className="float-left">Cancel</Button>
              <Button color="primary" onClick={(e) => { this.save(e); }} className="float-right">Save</Button>
            </Col>
          </Row>
        </CardBlock>
      </Card>
    );
  }
}

export default StepConfig;
