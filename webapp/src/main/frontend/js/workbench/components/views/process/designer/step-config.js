import * as React from 'react';
import PropTypes from 'prop-types';
import {
  Button, Card, CardBlock, Row, Col,
} from 'reactstrap';

import * as metadata from '../../resource/register/metadata';
import * as triplegeo from '../../resource/register/triple';

import { SingleStep } from '../../../helpers/forms/';

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
          { this.props.step.tool=== 'TripleGeo'?(
            <SingleStep
              onComplete={(values) => { this.props.configureStepEnd(this.props.step,values);}}
            >
              <triplegeo.Component
                id="triplegeo"
                title="TripleGeo"
                initialValue={triplegeo.initialValue}
                validate={triplegeo.validator}
              />    
            </SingleStep>)
            :
            (<SingleStep
              onComplete={(values) => { this.props.configureStepEnd(this.props.step,values);}}
            >
              <metadata.Component
                id="register"
                title="Register Resource"
                initialValue={metadata.initialValue}
                validate={metadata.validator}
              />    
            </SingleStep>)}
          <Button color="danger" onClick={(e) => { this.cancel(e); }} className="float-left">Cancel</Button>
        </CardBlock>
      </Card>
    );
  }
}

export default StepConfig;




