import * as React from 'react';
import PropTypes from 'prop-types';
import {
  Button, Card, CardBody, Row, Col,
} from 'reactstrap';
import {
  EnumTool
} from './constants';
import Form from '../../../helpers/forms/form';

import * as metadata from '../../resource/register/metadata';
import * as triplegeo from '../../resource/register/triplegeo';

import Placeholder from '../../../helpers/placeholder';

/**
 * Presentational component that wraps the step configuration options
 *
 * @class StepConfig
 * @extends {React.Component}
 */
class StepConfig extends React.Component {

  constructor(props) {
    super(props);

    this.cancel = this.cancel.bind(this);
    this.save = this.save.bind(this);
    this.setError = this.setError.bind(this);
    this.setValue = this.setValue.bind(this);
  }

  static propTypes = {
    step: PropTypes.object.isRequired,
    configuration: PropTypes.object,
    errors: PropTypes.object,
    configureStepValidate: PropTypes.func.isRequired,
    configureStepUpdate: PropTypes.func.isRequired,
    configureStepEnd: PropTypes.func.isRequired,
  }

  setValue(configuration) {
    this.props.configureStepUpdate(this.props.step, configuration);
  }

  setError(errors) {
    if (errors) {
      this.props.configureStepValidate(this.props.step, errors);
    }
  }

  createForm(Component, validator) {
    return (
      <Form
        title={this.props.step.title}
        iconClass={this.props.step.iconClass}
        validate={validator}
        setError={this.setError}
        setValue={this.setValue}
        cancel={this.cancel}
        save={this.save}
        values={this.props.configuration}
        errors={this.props.errors}
      >
        {
          React.isValidElement(Component) ?
            Component
            :
            <Component />
        }
      </Form>
    );
  }

  save() {
    this.props.configureStepEnd(this.props.step, this.props.configuration, this.props.errors);
  }

  cancel() {
    this.props.configureStepEnd(this.props.step, null, this.props.errors);
  }

  render() {
    return (
      <Card>
        <CardBody className="card-body">
          {this.props.step.tool === EnumTool.TripleGeo &&
            this.createForm(triplegeo.Component, triplegeo.validator)
          }
          {this.props.step.tool === EnumTool.LIMES &&
            this.createForm(<Placeholder style={{ height: '100%' }} label="Context" iconClass="fa fa-magic" />, null)
          }
          {this.props.step.tool === EnumTool.FAGI &&
            this.createForm(<Placeholder style={{ height: '100%' }} label="Context" iconClass="fa fa-magic" />, null)
          }
          {this.props.step.tool === EnumTool.DEER &&
            this.createForm(<Placeholder style={{ height: '100%' }} label="Context" iconClass="fa fa-magic" />, null)
          }
          {this.props.step.tool === EnumTool.CATALOG &&
            this.createForm(metadata.Component, metadata.validator)
          }
        </CardBody>
      </Card>
    );
  }
}

export default StepConfig;
