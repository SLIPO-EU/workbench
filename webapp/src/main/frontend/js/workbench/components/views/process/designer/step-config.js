import * as React from 'react';
import PropTypes from 'prop-types';

import {
  Card,
  CardBody,
} from 'reactstrap';

import {
  EnumDataSource,
  EnumTool,
} from '../../../../model/process-designer';

import {
  TripleGeoConfigurationLevels,
  TripleGeoConfigurationLevelOptions,
  LimesConfigurationLevelOptions,
  FagiConfigurationLevelOptions,
  DeerConfigurationLevelOptions,
} from '../../../../model/process-designer/configuration';

import Form from '../../../helpers/forms/form';

import { default as TripleGeoConfiguration } from '../../process/designer/configuration/triplegeo';
import { default as LimesConfiguration } from '../../process/designer/configuration/limes';
import { default as FagiConfiguration } from '../../process/designer/configuration/fagi';
import { default as DeerConfiguration } from '../../process/designer/configuration/deer';
import { default as MetadataConfiguration } from '../../process/designer/configuration/metadata';
import { default as ReverseTripleGeoConfiguration } from '../../process/designer/configuration/triplegeo-reverse';

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
import {
  validateConfiguration as validateReverseTripleGeo,
} from '../../../../service/toolkit/triplegeo-reverse';

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
    configureStepEnd: PropTypes.func.isRequired,
    configureStepUpdate: PropTypes.func.isRequired,
    configureStepValidate: PropTypes.func.isRequired,
    errors: PropTypes.object,
    filesystem: PropTypes.object,
    readOnly: PropTypes.bool.isRequired,
    setConfiguration: PropTypes.func.isRequired,
    step: PropTypes.object.isRequired,
    stepConfiguration: PropTypes.object,
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

  get isFileSystemDataSource() {
    const { step: { dataSources = [] } } = this.props;
    const dataSource = dataSources.length === 1 ? dataSources[0] : null;

    return !!(
      (dataSource) && (dataSource.source === EnumDataSource.FILESYSTEM) && (dataSource.configuration) && (dataSource.configuration.resource)
    );
  }

  get fileDataSource() {
    const { step: { dataSources = [] } } = this.props;
    const dataSource = dataSources.length === 1 ? dataSources[0] : null;

    if ((dataSource) && (dataSource.source === EnumDataSource.FILESYSTEM) && (dataSource.configuration) && (dataSource.configuration.resource)) {
      return dataSource.configuration.resource.path || null;
    }
    return null;
  }

  render() {
    const enableAutoMappings = this.isFileSystemDataSource;

    return (
      <Card>
        <CardBody className="card-body">
          {this.props.step.tool === EnumTool.TripleGeo &&
            this.createForm(TripleGeoConfiguration, validateTripleGeo, {
              allowDelete: true,
              allowNewFolder: true,
              allowUpload: true,
              appConfiguration: this.props.appConfiguration,
              createFolder: this.props.createFolder,
              deletePath: this.props.deletePath,
              // Enabled configuration levels
              enabledLevels: enableAutoMappings ? TripleGeoConfigurationLevelOptions.map(l => l.value) : [TripleGeoConfigurationLevels.ADVANCED],
              filesystem: this.props.filesystem,
              // Mappings methods
              getTripleGeoMappings: this.props.getTripleGeoMappings,
              getTripleGeoMappingFileAsText: this.props.getTripleGeoMappingFileAsText,
              // Optional input file required for ML mappings generation
              inputFile: this.fileDataSource,
              uploadFile: this.props.uploadFile,
            })
          }
          {this.props.step.tool === EnumTool.LIMES &&
            this.createForm(LimesConfiguration, validateLimes, {
              allowDelete: true,
              allowNewFolder: true,
              allowUpload: true,
              appConfiguration: this.props.appConfiguration,
              createFolder: this.props.createFolder,
              deletePath: this.props.deletePath,
              // Enabled configuration levels
              enabledLevels: LimesConfigurationLevelOptions.map(l => l.value),
              filesystem: this.props.filesystem,
              setConfiguration: this.props.setConfiguration,
              step: this.props.step,
              uploadFile: this.props.uploadFile,
            })
          }
          {this.props.step.tool === EnumTool.FAGI &&
            this.createForm(FagiConfiguration, validateFagi, {
              allowDelete: true,
              allowNewFolder: true,
              allowUpload: true,
              appConfiguration: this.props.appConfiguration,
              createFolder: this.props.createFolder,
              deletePath: this.props.deletePath,
              // Enabled configuration levels
              enabledLevels: FagiConfigurationLevelOptions.map(l => l.value),
              filesystem: this.props.filesystem,
              setConfiguration: this.props.setConfiguration,
              step: this.props.step,
              uploadFile: this.props.uploadFile,
            })
          }
          {this.props.step.tool === EnumTool.DEER &&
            this.createForm(DeerConfiguration, validateDeer, {
              allowDelete: true,
              allowNewFolder: true,
              allowUpload: true,
              appConfiguration: this.props.appConfiguration,
              createFolder: this.props.createFolder,
              deletePath: this.props.deletePath,
              // Enabled configuration levels
              enabledLevels: DeerConfigurationLevelOptions.map(l => l.value),
              filesystem: this.props.filesystem,
              uploadFile: this.props.uploadFile,
            })
          }
          {this.props.step.tool === EnumTool.CATALOG &&
            this.createForm(MetadataConfiguration, validateMetadata)
          }
          {this.props.step.tool === EnumTool.ReverseTripleGeo &&
            this.createForm(ReverseTripleGeoConfiguration, validateReverseTripleGeo, {
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
        </CardBody>
      </Card>
    );
  }
}

export default StepConfig;
