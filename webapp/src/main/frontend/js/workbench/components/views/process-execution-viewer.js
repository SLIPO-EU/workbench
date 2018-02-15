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
  EnumKpiViewMode
} from '../../model/constants';

import {
  ToastTemplate,
} from '../helpers';

import {
  Execution,
  ExecutionStep,
  ExecutionFiles,
  FeaturePropertyViewer,
  KpiChartView,
  KpiGridView,
  MapViewer,
} from './execution/viewer';

import {
  addToMap,
  fetchExecutionDetails,
  fetchExecutionKpiData,
  removeFromMap,
  resetKpi,
  selectFeatures,
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

    this.selectKpi = this.selectKpi.bind(this);
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
    this.props.toggleMapView();
  }

  selectKpi(file, mode) {
    const { id, version, execution } = this.props.match.params;

    this.props.fetchExecutionKpiData(Number.parseInt(id), Number.parseInt(version), Number.parseInt(execution), file, mode);
  }

  renderStep(step) {
    return (
      <ExecutionStep
        key={step.key}
        selected={this.props.selectedStep === step.id}
        selectStep={this.props.selectStep}
        step={step}
      />
    );
  }

  renderMap() {
    const features = this.props.selectedFeatures;

    return (
      <div>
        <Row>
          <Col>
            <MapViewer
              selectFeatures={this.props.selectFeatures}
            />
          </Col>
        </Row>
        {features && features.length !== 0 &&
          <FeaturePropertyViewer
            features={features}
          />
        }
      </div>
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
                removeFromMap={this.props.removeFromMap}
                selectedFile={this.props.selectedFile}
                selectFile={this.props.selectFile}
                selectKpi={this.selectKpi}
                step={step}
              />
            }
            {this.props.selectedKpi && this.props.selectedKpi.mode === EnumKpiViewMode.GRID &&
              <KpiGridView
                data={this.props.selectedKpi.data}
                hide={this.props.resetKpi}
              />
            }
            {this.props.selectedKpi && this.props.selectedKpi.mode === EnumKpiViewMode.CHART &&
              <KpiChartView
                data={this.props.selectedKpi.data}
                hide={this.props.resetKpi}
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
              <Input type="checkbox" className="switch-input" checked={this.props.displayMap} onChange={this.toggleMap} disabled={this.props.layers.length === 0} />
              <span className="switch-label" data-on="Map" data-off="Data"></span>
              <span className="switch-handle"></span>
            </Label>
          </Col>
        </Row>

        {this.props.displayMap ?
          this.renderMap()
          :
          this.renderData()
        }

      </div>
    );
  }

}

const mapStateToProps = (state) => ({
  displayMap: state.ui.views.execution.viewer.displayMap,
  execution: state.ui.views.execution.viewer.execution,
  layers: state.ui.views.execution.viewer.layers,
  selectedFeatures: state.ui.views.execution.viewer.selectedFeatures,
  selectedFile: state.ui.views.execution.viewer.selectedFile,
  selectedKpi: state.ui.views.execution.viewer.selectedKpi,
  selectedStep: state.ui.views.execution.viewer.selectedStep,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  addToMap,
  fetchExecutionDetails,
  fetchExecutionKpiData,
  removeFromMap,
  resetKpi,
  selectFeatures,
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
