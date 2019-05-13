import React from 'react';
import * as ReactRedux from 'react-redux';

import {
  bindActionCreators
} from 'redux';

import {
  checkFile,
  checkLog,
  downloadFile,
  downloadLog,
  fetchExecution,
  fetchExecutionKpiData,
  fetchExecutionLogData,
} from '../../ducks/ui/views/api-usage';

import {
  DockerLogDetails,
  Execution,
  ExecutionStepDetails,
} from './execution/viewer';

import {
  message,
} from '../../service';

class ExecutionViewer extends React.Component {

  constructor(props) {
    super(props);

    this.viewKpi = this.viewKpi.bind(this);
    this.viewLog = this.viewLog.bind(this);

    this.state = {
      selectedKpiRow: null,
      selectedLogRow: null,
      kpiData: null,
      logData: null,
    };
  }

  componentDidMount() {
    const { processId } = this.props.match.params;

    this.props.fetchExecution(Number.parseInt(processId))
      .catch((err) => {
        this.error(err.message);
      });
  }

  error(text) {
    message.error(text, 'fa-warning');

    setTimeout(() => {
      this.props.history.goBack();
    }, 500);
  }

  viewKpi(fileId) {
    const {
      process: { id: processId, version: processVersion },
      execution: { id: executionId }
    } = this.props.data;

    this.props.fetchExecutionKpiData(processId, processVersion, executionId, fileId)
      .then((data) => {
        this.setState({
          selectedKpiRow: fileId,
          kpiData: {
            id: fileId,
            data: data.values,
            original: data.original,
          },
        });
      })
      .catch(err => {
        message.error(`Failed to load KPI data. ${err.message}`, 'fa-warning');
      });
  }

  viewLog(fileId) {
    const {
      process: { id: processId, version: processVersion },
      execution: { id: executionId }
    } = this.props.data;

    this.props.fetchExecutionLogData(processId, processVersion, executionId, fileId)
      .then((data) => {
        this.setState({
          selectedLogRow: fileId,
          logData: {
            id: fileId,
            data,
          },
        });
      })
      .catch(err => {
        message.error(`Failed to load docker log data. ${err.message}`, 'fa-warning');
      });
  }

  render() {
    const { data } = this.props;
    if (!data) {
      return null;
    }

    const { process, execution } = data;
    const step = execution.steps[0];

    return (
      <React.Fragment>
        <div>
          <Execution
            execution={execution}
          />
        </div>
        <div>
          <ExecutionStepDetails
            checkFile={this.props.checkFile}
            downloadFile={this.props.downloadFile}
            execution={execution}
            files={step.files}
            process={process}
            resetSelectedFile={() => {
              this.setState({
                selectedKpiRow: null,
              });
            }}
            selectedRow={this.state.selectedKpiRow}
            selectedKpi={this.state.kpiData}
            selectRow={(fileId) => {
              this.setState({
                selectedKpiRow: fileId,
              });
            }}
            step={step}
            viewKpi={this.viewKpi}
          />
        </div>
        <div>
          <DockerLogDetails
            checkLog={this.props.checkLog}
            downloadLog={this.props.downloadLog}
            execution={execution}
            logs={step.logs}
            process={process}
            resetSelectedFile={() => {
              this.setState({
                selectedLogRow: null,
              });
            }}
            selectedRow={this.state.selectedLogRow}
            selectedLog={this.state.logData}
            selectRow={(fileId) => {
              this.setState({
                selectedLogRow: fileId,
              });
            }}
            step={step}
            viewLog={this.viewLog}
          />
        </div>
      </React.Fragment>
    );
  }
}

const mapStateToProps = (state) => ({
  data: state.ui.views.admin.api.execution,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  checkFile,
  checkLog,
  downloadFile,
  downloadLog,
  fetchExecution,
  fetchExecutionKpiData,
  fetchExecutionLogData,
}, dispatch);

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps)(ExecutionViewer);
