import * as React from 'react';
import PropTypes from 'prop-types';
import * as ReactRedux from 'react-redux';
import { FormattedTime } from 'react-intl';
import {
  EnumInputType,
  EnumResourceType,
} from '../../../../model/process-designer';
import { Form } from '../../../helpers/forms';
import * as process from './process-form';

/**
 * A presentational component for displaying the properties of a process
 *
 * @class ProcessDetails
 * @extends {React.Component}
 */
class ProcessDetails extends React.Component {

  constructor(props) {
    super(props);

    this._setError = this._setError.bind(this);
    this._setValue = this._setValue.bind(this);
  }

  static propTypes = {
    values: PropTypes.object,
    errors: PropTypes.object,
    processValidate: PropTypes.func.isRequired,
    processUpdate: PropTypes.func.isRequired,
    readOnly: PropTypes.oneOfType([PropTypes.bool, PropTypes.func]).isRequired,
  };

  _setError(errors) {
    if (errors) {
      this.props.processValidate(errors);
    }
  }

  _setValue(values) {
    this.props.processUpdate(values);
  }

  render() {
    return (
      <Form
        header={false}
        validate={process.validator}
        setError={this._setError}
        setValue={this._setValue}
        values={this.props.values}
        errors={this.props.errors}
        readOnly={this.props.readOnly}
      >
        {
          <process.Component />
        }
      </Form>
    );
  }
}

export default ProcessDetails;
