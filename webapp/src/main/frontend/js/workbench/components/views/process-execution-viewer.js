import * as React from 'react';
import * as ReactRedux from 'react-redux';

import {
  bindActionCreators
} from 'redux';

import {
  Card,
  CardBody,
  Col,
  Input,
  Label,
  Row,
} from 'reactstrap';

import {
  toast
} from 'react-toastify';

import {
  ToastTemplate,
} from '../helpers';

import {
  Execution,
  ExecutionStep,
  ExecutionFiles,
  MapViewer,
} from './execution/viewer';

import {
  addToMap,
  fetchExecutionDetails,
  removeFromMap,
  selectFile,
  selectStep,
  toggleMapView,
} from '../../ducks/ui/views/process-execution-viewer';

/**
 * Display process execution data
 *
 * @class ProcessExecutionViewer
 * @extends {React.Component}
 */
class ProcessExecutionViewer extends React.Component {

  constructor(props) {
    super(props);

    this.toggleMap = this.toggleMap.bind(this);
  }

  componentDidMount() {
    const { id, version, execution } = this.props.match.params;

    if (id) {
      this.props.fetchExecutionDetails(Number.parseInt(id), Number.parseInt(version), Number.parseInt(execution))
        .catch((err) => {
          this.error(err.message || 'Failed loading execution details');
        });
    } else {
      this.error('Invalid id.Failed to load execution details');
    }
  }

  error(message, goBack) {
    toast.dismiss();

    toast.error(
      <ToastTemplate iconClass='fa-warning' text={message} />
    );

    if ((typeof goBack === 'undefined') || (goBack)) {
      setTimeout(() => this.props.history.goBack(), 500);
    }
  }

  toggleMap(e) {
    console.log(e);
    this.props.toggleMapView();
  }

  renderStep(step) {
    return (
      <ExecutionStep
        key={step.key}
        selectedStep={this.props.selectedStep}
        selectStep={this.props.selectStep}
        step={step}
      />
    );
  }

  renderMap() {
    return (
      <MapViewer />
    );
  }

  renderData() {
    const step = this.props.execution.steps.find((s) => s.id === this.props.selectedStep);
    const files = (step ? step.files : []);
    return (
      <div>
        <Execution execution={this.props.execution} />
        <Row>
          <Col>
            {this.props.execution.steps.map((s) => this.renderStep(s))}
          </Col>
          <Col>
            {this.props.selectedStep &&
              <ExecutionFiles
                addToMap={this.props.addToMap}
                files={files}
                mapFiles={this.props.mapFiles}
                removeFromMap={this.props.removeFromMap}
                selectedFile={this.props.selectedFile}
                selectedStep={this.props.selectedStep}
                selectFile={this.props.selectFile}
                step={step}
              />
            }
          </Col>
        </Row>
      </div>
    );
  }

  render() {
    if (!this.props.execution) {
      return null;
    }
    return (
      <div className="animated fadeIn">
        <Row>
          <Col>
            <Label className="switch switch-text switch-pill switch-primary">
              <Input type="checkbox" className="switch-input" checked={this.props.displayMapView} onChange={this.toggleMap} />
              <span className="switch-label" data-on="Map" data-off="Data"></span>
              <span className="switch-handle"></span>
            </Label>
          </Col>
        </Row>

        {this.props.displayMapView ?
          this.renderMap()
          :
          this.renderData()
        }

      </div>
    );
  }

}

const mapStateToProps = (state) => ({
  displayMapView: state.ui.views.execution.viewer.displayMapView,
  execution: state.ui.views.execution.viewer.execution,
  mapFiles: state.ui.views.execution.viewer.mapFiles,
  selectedFile: state.ui.views.execution.viewer.selectedFile,
  selectedStep: state.ui.views.execution.viewer.selectedStep,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  addToMap,
  fetchExecutionDetails,
  removeFromMap,
  selectFile,
  selectStep,
  toggleMapView,
}, dispatch);

const mergeProps = (stateProps, dispatchProps, ownProps) => {
  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
  };
};

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(ProcessExecutionViewer);
