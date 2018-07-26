import * as React from 'react';
import PropTypes from 'prop-types';

import {
  Card,
  CardBody,
} from 'reactstrap';

import {
  EnumTool
} from '../../../../model/process-designer';

import Form from '../../../helpers/forms/form';

import { default as TripleGeoConfiguration } from '../../process/designer/configuration/triplegeo';
import { default as LimesConfiguration } from '../../process/designer/configuration/limes';
import { default as FagiConfiguration } from '../../process/designer/configuration/fagi';
import { default as DeerConfiguration } from '../../process/designer/configuration/deer';
import { default as MetadataConfiguration } from '../../process/designer/configuration/metadata';

import {
  validateConfiguration as validateTripleGeo,
} from '../../../../service/toolkit/triplegeo';
import {
  validateConfiguration as validateLimes,
} from '../../../../service/toolkit/limes';
import {
  validateConfiguration as validateFagi,
} from '../../../../service/toolkit/fagi';
import {
  validateConfiguration as validateDeer,
} from '../../../../service/toolkit/deer';
import {
  validateConfiguration as validateMetadata,
} from '../../../../service/toolkit/metadata';

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
    appConfiguration: PropTypes.object,
    filesystem: PropTypes.object,
    step: PropTypes.object.isRequired,
    stepConfiguration: PropTypes.object,
    errors: PropTypes.object,
    configureStepValidate: PropTypes.func.isRequired,
    configureStepUpdate: PropTypes.func.isRequired,
    configureStepEnd: PropTypes.func.isRequired,
    readOnly: PropTypes.bool.isRequired,
    setConfiguration: PropTypes.func.isRequired,
  }

  setValue(configuration) {
    this.props.configureStepUpdate(this.props.step, configuration);
  }

  setError(errors) {
    if (errors) {
      this.props.configureStepValidate(this.props.step, errors);
    }
  }

  createForm(Component, validator, extraComponentProps = {}) {
    return (
      <Form
        title={this.props.step.name}
        iconClass={this.props.step.iconClass}
        validate={validator}
        setError={this.setError}
        setValue={this.setValue}
        cancel={this.cancel}
        save={this.save}
        values={this.props.stepConfiguration}
        errors={this.props.errors}
        readOnly={this.props.readOnly}
        appConfiguration={this.props.appConfiguration}
      >
        {
          React.isValidElement(Component) ?
            Component
            :
            <Component {...extraComponentProps} />
        }
      </Form>
    );
  }

  save() {
    this.props.configureStepEnd(this.props.step, this.props.stepConfiguration || {}, this.props.errors);
  }

  cancel() {
    this.props.configureStepEnd(this.props.step, null, this.props.errors);
  }

  render() {
    return (
      <Card>
        <CardBody className="card-body">
          {this.props.step.tool === EnumTool.TripleGeo &&
            this.createForm(TripleGeoConfiguration, validateTripleGeo, {
              appConfiguration: this.props.appConfiguration,
              filesystem: this.props.filesystem,
              allowUpload: true,
              allowNewFolder: true,
              allowDelete: true,
              createFolder: this.props.createFolder,
              uploadFile: this.props.uploadFile,
              deletePath: this.props.deletePath,
            })
          }
          {this.props.step.tool === EnumTool.LIMES &&
            this.createForm(LimesConfiguration, validateLimes, {
              appConfiguration: this.props.appConfiguration,
              filesystem: this.props.filesystem,
              allowUpload: true,
              allowNewFolder: true,
              allowDelete: true,
              createFolder: this.props.createFolder,
              uploadFile: this.props.uploadFile,
              deletePath: this.props.deletePath,
              step: this.props.step,
              setConfiguration: this.props.setConfiguration,
            })
          }
          {this.props.step.tool === EnumTool.FAGI &&
            this.createForm(FagiConfiguration, validateFagi, {
              appConfiguration: this.props.appConfiguration,
              filesystem: this.props.filesystem,
              allowUpload: true,
              allowNewFolder: true,
              allowDelete: true,
              createFolder: this.props.createFolder,
              uploadFile: this.props.uploadFile,
              deletePath: this.props.deletePath,
              step: this.props.step,
              setConfiguration: this.props.setConfiguration,
            })
          }
          {this.props.step.tool === EnumTool.DEER &&
            this.createForm(DeerConfiguration, validateDeer, {
              appConfiguration: this.props.appConfiguration,
              filesystem: this.props.filesystem,
              allowUpload: true,
              allowNewFolder: true,
              allowDelete: true,
              createFolder: this.props.createFolder,
              uploadFile: this.props.uploadFile,
              deletePath: this.props.deletePath,
            })
          }
          {this.props.step.tool === EnumTool.CATALOG &&
            this.createForm(MetadataConfiguration, validateMetadata)
          }
        </CardBody>
      </Card>
    );
  }
}

export default StepConfig;
