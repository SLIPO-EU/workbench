import * as React from 'react';
import * as ReactRedux from 'react-redux';

import {
  bindActionCreators
} from 'redux';

import {
  FormattedTime
} from 'react-intl';

import {
  toast
} from 'react-toastify';

import {
  Button,
  ButtonDropdown,
  ButtonGroup,
  Card,
  CardBody,
  Col,
  DropdownItem,
  DropdownMenu,
  DropdownToggle,
  Row,
} from 'reactstrap';

import {
  EnumProcessSaveAction,
  StaticRoutes,
} from '../../model';

import {
  Dialog,
  ToastTemplate,
} from '../helpers';

import {
  DataSourceConfig,
  Designer,
  EnumMode,
  EnumHarvester,
  EnumOperation,
  EnumTool,
  EnumViews,
  Operation,
  StepConfig,
  Toolbox,
} from './process/designer';

// TODO: Remove unused references
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
  reset as resetProcess,
  fetchProcess,
  fetchProcessRevision,
  save,
  addStep,
  removeStep,
  moveStep,
  configureStepBegin,
  configureStepValidate,
  configureStepUpdate,
  configureStepEnd,
  setStepProperty,
  addStepInput,
  removeStepInput,
  addStepDataSource,
  removeStepDataSource,
  configureStepDataSourceBegin,
  configureStepDataSourceValidate,
  configureStepDataSourceUpdate,
  configureStepDataSourceEnd,
  resetActive,
  setActiveProcess,
  setActiveStep,
  setActiveStepInput,
  setActiveStepDataSource,
  setActiveResource,
  undo,
  redo,
} from '../../ducks/ui/views/process-designer';

// TODO: Remove unused references
import {
  addToMap,
  fetchExecutionDetails,
  fetchExecutionKpiData,
  removeFromMap,
  reset as resetExecution,
  resetKpi,
  selectFeatures,
  selectFile,
  selectStep,
  toggleMapView,
} from '../../ducks/ui/views/process-execution-viewer';

/**
 * Component actions
 */
const EnumComponentAction = {
  'Save': 'Save',
  'SaveAndExecute': 'SaveAndExecute',
  'SaveAsTemplate': 'SaveAsTemplate',
  'Discard': 'Discard',
  'CloseCancelDialog': 'CloseCancelDialog',
};

/**
 * Create/Update a POI data integration process
 *
 * @class ProcessDesigner
 * @extends {React.Component}
 */
class ProcessDesigner extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      isLoading: true,
      saveDropdownOpen: false,
      cancelDialogOpen: false,
    };

    this.cancelDialogHandler = this.cancelDialogHandler.bind(this);
    this.goToProcessExplorer = this.goToProcessExplorer.bind(this);
    this.onFetchError = this.onFetchError.bind(this);
    this.onFetchSuccess = this.onFetchSuccess.bind(this);
    this.reset = this.reset.bind(this);
    this.select = this.select.bind(this);
    this.toggleCancelDialog = this.toggleCancelDialog.bind(this);
    this.toggleSaveButtonDropdown = this.toggleSaveButtonDropdown.bind(this);
  }

  resolveAction() {
    const { id, version, execution, ...rest } = this.props.match.params;

    if (id && version && execution) {
      return EnumMode.EXECUTION;
    }
    if (id && version) {
      return EnumMode.VIEW;
    }
    if (id) {
      return EnumMode.EDIT;
    }

    return EnumMode.CREATE;
  }

  componentDidMount() {
    const { id, version, execution, ...rest } = this.props.match.params;

    const action = this.resolveAction();

    this.reset();

    switch (action) {
      case EnumMode.CREATE:
        this.select();
        this.resume();
        break;

      case EnumMode.EDIT:
        this.props.fetchProcess(Number.parseInt(id))
          .then(this.onFetchSuccess)
          .catch(this.onFetchError);
        break;

      case EnumMode.VIEW:
        this.props.fetchProcessRevision(Number.parseInt(id), Number.parseInt(version))
          .then(this.onFetchSuccess)
          .catch(this.onFetchError);
        break;

      case EnumMode.EXECUTION:
        this.props.fetchProcessRevision(Number.parseInt(id), Number.parseInt(version))
          .then(() => {
            return this.props.fetchExecutionDetails(Number.parseInt(id), Number.parseInt(version), Number.parseInt(execution));
          })
          .then(this.onFetchSuccess)
          .catch(this.onFetchError);
        break;

      default:
        this.error('Action is not supported');
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

  resume() {
    this.setState({ isLoading: false });
  }

  onFetchSuccess() {
    this.select();
    this.resume();
  }

  onFetchError(err) {
    this.error(err.message);
  }

  mapToSaveAction(action) {
    switch (action) {
      case EnumComponentAction.Save:
        return EnumProcessSaveAction.Save;
      case EnumComponentAction.SaveAndExecute:
        return EnumProcessSaveAction.SaveAndExecute;
      case EnumComponentAction.SaveAsTemplate:
        return EnumProcessSaveAction.SaveAsTemplate;
    }
    return null;
  }

  toggleSaveButtonDropdown() {
    this.setState({ saveDropdownOpen: !this.state.saveDropdownOpen });
  }

  toggleCancelDialog() {
    this.setState({ cancelDialogOpen: !this.state.cancelDialogOpen });
  }

  goToProcessExplorer() {
    if (this.props.readOnly) {
      this.props.history.push(StaticRoutes.ProcessExplorer);
    }
  }

  reset(e) {
    if (e) {
      e.stopPropagation();
    }

    this.props.resetProcess();
    this.props.resetExecution();
  }

  select() {
    this.props.setActiveProcess(this.props.process);
  }

  cancelDialogHandler(action) {
    switch (action.key) {
      case EnumComponentAction.Discard:
        this.reset();
        this.props.history.push(StaticRoutes.ProcessExplorer);
        break;
      default:
        this.toggleCancelDialog();
        break;
    }
  }

  save(action) {
    toast.dismiss();

    this.props.save(this.mapToSaveAction(action), this.props.designer)
      .then((result) => {
        toast.success(
          <ToastTemplate iconClass='fa-save' text='Process has been saved successfully!' />
        );
        this.reset();
        this.props.history.push(StaticRoutes.ProcessExplorer);
      })
      .catch((err) => {
        toast.error(
          <ToastTemplate iconClass='fa-warning' text={err.message} />
        );
      });
  }

  renderDesigner() {
    return (
      <div onClick={this.select}>
        {!this.props.readOnly &&
          <Card className="mb-2">
            <CardBody className="card-body">
              <Row>
                <Col>
                  <Toolbox />
                  <p className='text-muted slipo-pd-tip float-left'>Drag SLIPO Toolkit components to the designer surface for building a workflow.</p>
                </Col>
              </Row>
            </CardBody>
          </Card>
        }
        {this.props.readOnly && this.props.execution &&
          <Execution execution={this.props.execution} />
        }
        <Card>
          <CardBody className="card-body">
            <Row>
              {this.props.readOnly &&
                <Col>
                  <Button color="primary" onClick={this.goToProcessExplorer} className="float-right mr-3">Return</Button>
                </Col>
              }
              {!this.props.readOnly &&
                <Col>
                  <Button color="danger" onClick={this.toggleCancelDialog} className="float-left">Discard</Button>
                  <Button color="warning" onClick={this.reset} className="float-left ml-3">Clear</Button>
                  <Button color="default" onClick={this.props.undoAction} className="float-left ml-3" disabled={this.props.undo.length === 1}>Undo</Button>
                  <Button color="default" onClick={this.props.redoAction} className="float-left ml-3" disabled={this.props.redo.length === 0}>Redo</Button>
                  <ButtonGroup className="float-right">
                    <Button color="primary" onClick={this.save.bind(this, EnumComponentAction.Save)}>Save</Button>
                    <ButtonDropdown isOpen={this.state.saveDropdownOpen} toggle={this.toggleSaveButtonDropdown}>
                      <DropdownToggle caret>
                        More ...
                      </DropdownToggle>
                      <DropdownMenu>
                        <DropdownItem onClick={this.save.bind(this, EnumComponentAction.SaveAndExecute)}>Save & Execute</DropdownItem>
                        <DropdownItem onClick={this.save.bind(this, EnumComponentAction.SaveAsTemplate)}>Save Recipe </DropdownItem>
                      </DropdownMenu>
                    </ButtonDropdown>
                  </ButtonGroup>
                </Col>
              }
            </Row>
          </CardBody>
          <CardBody className="card-body">
            <Row className="mb-2">
              <Col style={{ padding: '9px' }}>
                <Designer
                  active={this.props.active}
                  execution={this.props.execution}
                  groups={this.props.groups}
                  steps={this.props.steps}
                  resources={this.props.resources}
                  addStep={this.props.addStep}
                  configureStepBegin={this.props.configureStepBegin}
                  removeStep={this.props.removeStep}
                  moveStep={this.props.moveStep}
                  setStepProperty={this.props.setStepProperty}
                  addStepInput={this.props.addStepInput}
                  removeStepInput={this.props.removeStepInput}
                  addStepDataSource={this.props.addStepDataSource}
                  removeStepDataSource={this.props.removeStepDataSource}
                  configureStepDataSourceBegin={this.props.configureStepDataSourceBegin}
                  setActiveStep={this.props.setActiveStep}
                  setActiveStepInput={this.props.setActiveStepInput}
                  setActiveStepDataSource={this.props.setActiveStepDataSource}
                  setActiveResource={this.props.setActiveResource}
                  readOnly={this.props.readOnly}
                />
              </Col>
            </Row>
          </CardBody>
        </Card>
      </div>
    );
  }

  renderStepConfiguration() {
    return (
      <StepConfig
        step={this.props.view.step}
        configuration={this.props.view.configuration}
        errors={this.props.view.errors}
        configureStepValidate={this.props.configureStepValidate}
        configureStepUpdate={this.props.configureStepUpdate}
        configureStepEnd={this.props.configureStepEnd}
        readOnly={this.props.readOnly}
      />
    );
  }

  renderDataSourceConfiguration() {
    return (
      <DataSourceConfig
        step={this.props.view.step}
        dataSource={this.props.view.dataSource}
        configuration={this.props.view.configuration}
        errors={this.props.view.errors}
        configureStepDataSourceValidate={this.props.configureStepDataSourceValidate}
        configureStepDataSourceUpdate={this.props.configureStepDataSourceUpdate}
        configureStepDataSourceEnd={this.props.configureStepDataSourceEnd}
        filesystem={this.props.filesystem}
        readOnly={this.props.readOnly}
      />
    );
  }

  renderCancelDialog() {
    return (
      <Dialog
        header={
          <span>
            <i className={'fa fa-question mr-2'}></i>System Message
          </span>
        }
        modal={this.state.cancelDialogOpen}
        handler={this.cancelDialogHandler}
        actions={[
          {
            key: EnumComponentAction.Discard,
            label: 'Yes',
            iconClass: 'fa fa-trash',
            color: 'danger',
          }, {
            key: EnumComponentAction.CloseCancelDialog,
            label: 'No',
            iconClass: 'fa fa-undo',
          }
        ]}
      >
        <div>The form's changes will be discarded. Are you sure you want to continue?</div>
      </Dialog>
    );
  }

  render() {
    if (this.state.isLoading) {
      return null;
    }

    return (
      <div className="animated fadeIn">
        <Row>
          <Col>
            {this.props.view.type === EnumViews.Designer && this.renderDesigner()}
            {this.props.view.type === EnumViews.StepConfiguration && this.renderStepConfiguration()}
            {this.props.view.type === EnumViews.DataSourceConfiguration && this.renderDataSourceConfiguration()}
          </Col>
        </Row>
        {this.state.cancelDialogOpen &&
          this.renderCancelDialog()
        }
      </div>
    );
  }
}

const mapStateToProps = (state) => ({
  // Workflow designer
  active: state.ui.views.process.designer.active,
  designer: state.ui.views.process.designer,
  filesystem: state.config.filesystem,
  groups: state.ui.views.process.designer.groups,
  process: state.ui.views.process.designer.process,
  readOnly: state.ui.views.process.designer.readOnly,
  redo: state.ui.views.process.designer.redo,
  resources: state.ui.views.process.designer.resources,
  steps: state.ui.views.process.designer.steps,
  undo: state.ui.views.process.designer.undo,
  view: state.ui.views.process.designer.view,
  // Execution viewer
  // TODO: Remove unused references
  execution: state.ui.views.execution.viewer.execution,
  layers: state.ui.views.execution.viewer.layers,
  selectedFeatures: state.ui.views.execution.viewer.selectedFeatures,
  selectedFile: state.ui.views.execution.viewer.selectedFile,
  selectedKpi: state.ui.views.execution.viewer.selectedKpi,
  selectedStep: state.ui.views.execution.viewer.selectedStep,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  // Workflow designer
  resetProcess,
  fetchProcess,
  fetchProcessRevision,
  save,
  addStep,
  removeStep,
  moveStep,
  configureStepBegin,
  configureStepValidate,
  configureStepUpdate,
  configureStepEnd,
  setStepProperty,
  addStepInput,
  removeStepInput,
  addStepDataSource,
  removeStepDataSource,
  configureStepDataSourceBegin,
  configureStepDataSourceValidate,
  configureStepDataSourceUpdate,
  configureStepDataSourceEnd,
  resetActive,
  setActiveProcess,
  setActiveStep,
  setActiveStepInput,
  setActiveStepDataSource,
  setActiveResource,
  undoAction: undo,
  redoAction: redo,
  // Execution viewer
  // TODO: Remove unused references
  addToMap,
  fetchExecutionDetails,
  fetchExecutionKpiData,
  removeFromMap,
  resetExecution,
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

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(ProcessDesigner);
