import * as React from 'react';
import PropTypes from 'prop-types';
import {
  Button, Card, CardBody, Row, Col,
} from 'reactstrap';
import {
  EnumDataSource
} from './constants';
import Form from '../../../helpers/forms/form';

import * as externalUrl from '../../resource/register/url-select';
import * as filesystem from '../../resource/register/filesystem';

import Placeholder from '../../../helpers/placeholder';

/**
 * Presentational component that wraps the data source configuration options
 *
 * @class StepConfig
 * @extends {React.Component}
 */
class DataSourceConfig extends React.Component {

  constructor(props) {
    super(props);

    this.cancel = this.cancel.bind(this);
    this.save = this.save.bind(this);
    this.setError = this.setError.bind(this);
    this.setValue = this.setValue.bind(this);
  }

  static propTypes = {
    step: PropTypes.object.isRequired,
    dataSource: PropTypes.object.isRequired,
    configuration: PropTypes.object,
    errors: PropTypes.object,
    configureStepDataSourceValidate: PropTypes.func.isRequired,
    configureStepDataSourceUpdate: PropTypes.func.isRequired,
    configureStepDataSourceEnd: PropTypes.func.isRequired,
  }

  setValue(configuration) {
    this.props.configureStepDataSourceUpdate(this.props.step, this.props.dataSource, configuration);
  }

  setError(errors) {
    if (errors) {
      this.props.configureStepDataSourceValidate(this.props.step, this.props.dataSource, errors);
    }
  }

  createForm(Component, validator, props) {
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
            <Component {...props} />
        }
      </Form>
    );
  }

  save() {
    this.props.configureStepDataSourceEnd(this.props.step, this.props.dataSource, this.props.configuration, this.props.errors);
  }

  cancel() {
    this.props.configureStepDataSourceEnd(this.props.step, this.props.dataSource, null, this.props.errors);
  }

  render() {
    return (
      <Card>
        <CardBody className="card-body">
          {this.props.dataSource.source === EnumDataSource.EXTERNAL_URL &&
            this.createForm(externalUrl.Component, externalUrl.validator)
          }
          {this.props.dataSource.source === EnumDataSource.FILESYSTEM &&
            this.createForm(filesystem.Component, filesystem.validator, { filesystem: this.props.filesystem })
          }
          {this.props.dataSource.source === EnumDataSource.HARVESTER &&
            this.createForm(<Placeholder style={{ height: '100%' }} label="Context" iconClass="fa fa-magic" />, null)
          }
        </CardBody>
      </Card>
    );
  }
}

export default DataSourceConfig;
