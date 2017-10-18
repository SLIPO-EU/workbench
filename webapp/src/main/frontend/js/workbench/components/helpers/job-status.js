import * as React from 'react';
import * as PropTypes from 'prop-types';
import * as ReactRedux from 'react-redux';

/**
 * A presentational component for displaying job execution status as a label
 *
 * @class JobStatus
 * @extends {React.Component}
 */
class JobStatus extends React.Component {

  constructor(props) {
    super(props);
  }

  mapStatusToClassName() {
    switch (this.props.status) {
      case 'Completed':
        return 'slipo-job-status-completed';
      case 'Failed':
        return 'slipo-job-status-failed';
      case 'Stopped':
        return 'slipo-job-status-stopped';
      case 'Running':
        return 'slipo-job-status-running';
    }
  }
  render() {
    return (
      <div className={this.mapStatusToClassName()}>{this.props.status}</div>
    );
  }
}

JobStatus.propTypes = {
  status: function (props, propName, componentName) {
    if (!/Completed|Failed|Stopped|Running/.test(props[propName])) {
      return new Error('Validation failed!');
    }
  },
};

export default JobStatus;
