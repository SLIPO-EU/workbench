import * as React from 'react';

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
    switch (this.props.status.toLowerCase()) {
      case 'completed':
        return 'slipo-job-status-completed';
      case 'failed':
        return 'slipo-job-status-failed';
      case 'stopped':
        return 'slipo-job-status-stopped';
      case 'running': case 'started':
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
    if (!/Completed|Failed|Stopped|Started|Running/i.test(props[propName])) {
      return new Error('Validation failed!');
    }
  },
};

export default JobStatus;
