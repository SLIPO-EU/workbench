import * as React from 'react';
import * as PropTypes from 'prop-types';
import * as ReactRedux from 'react-redux';

import StatusLabel from './status-label';

/**
 * A presentational component for error level labels
 *
 * @class ErrorLevel
 * @extends {React.Component}
 */
class ErrorLevel extends React.Component {

  constructor(props) {
    super(props);
  }

  static propTypes = {
    value: PropTypes.string.isRequired,
    label: PropTypes.string,
  };

  static mappings = [{
    value: 'INFO',
    className: 'slipo-error-level-info',
  }, {
    value: 'WARN',
    className: 'slipo-error-level-warn',
  }, {
    value: 'ERROR',
    className: 'slipo-error-level-error',
  }];

  render() {
    return (
      <StatusLabel
        mappings={ErrorLevel.mappings}
        value={this.props.value}
        label={this.props.label || this.props.value}
      />
    );
  }
}

export default ErrorLevel;
