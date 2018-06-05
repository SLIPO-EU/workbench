import * as React from 'react';
import * as ReactRedux from 'react-redux';

import {
  bindActionCreators
} from 'redux';

import {
  toast,
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
  buildPath,
  DynamicRoutes,
  EnumErrorLevel,
  StaticRoutes,
} from '../../model';

import {
  EnumDesignerMode,
  EnumDesignerSaveAction,
  EnumDesignerView,
  EnumHarvester,
  EnumSelection,
} from '../../model/process-designer';

import {
  Dialog,
  ToastTemplate,
} from '../helpers';

import {
  DataSourceConfig,
  Designer,
  Operation,
  StepConfig,
  Toolbox,
} from './process/designer';

import {
  Execution,
  ExecutionStepDetails,
} from './execution/viewer';

import {
  createFolder,
  uploadFile,
  deletePath,
} from '../../ducks/config';

import {
  reset,
  checkFile,
  downloadFile,
  fetchProcess,
  fetchProcessRevision,
  fetchExecutionDetails,
  fetchExecutionKpiData,
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
  showStepExecutionDetails,
  hideStepExecutionDetails,
  resetSelectedFile,
  resetSelectedKpi,
  selectFile,
} from '../../ducks/ui/views/process-designer';

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
    this.selectKpi = this.selectKpi.bind(this);
    this.toggleCancelDialog = this.toggleCancelDialog.bind(this);
    this.toggleSaveButtonDropdown = this.toggleSaveButtonDropdown.bind(this);
    this.viewMap = this.viewMap.bind(this);
  }

  resolveMode() {
    const { id, version, execution, ...rest } = this.props.match.params;

    if (id && version && execution) {
      return EnumDesignerMode.EXECUTION;
    }
    if (id && version) {
      return EnumDesignerMode.VIEW;
    }
    if (id) {
      return EnumDesignerMode.EDIT;
    }

    return EnumDesignerMode.CREATE;
  }

  componentDidMount() {
    const { id, version, execution, ...rest } = this.props.match.params;
    const action = this.resolveMode();

    switch (action) {
      case EnumDesignerMode.CREATE:
        if (this.shouldReload(id, version)) {
          this.reset();
        }
        this.select();
        this.resume();
        break;

      case EnumDesignerMode.EDIT:
        if (this.shouldReload(id, version)) {
          this.reset();
          this.props.fetchProcess(Number.parseInt(id))
            .then(this.onFetchSuccess)
            .catch(this.onFetchError);
        }
        break;

      case EnumDesignerMode.VIEW:
        this.reset();
        this.props.fetchProcessRevision(Number.parseInt(id), Number.parseInt(version))
          .then(this.onFetchSuccess)
          .catch(this.onFetchError);
        break;

      case EnumDesignerMode.EXECUTION:
        this.reset();
        this.props.fetchExecutionDetails(Number.parseInt(id), Number.parseInt(version), Number.parseInt(execution))
          .then(this.onFetchSuccess)
          .catch(this.onFetchError);
        break;

      default:
        this.error('Action is not supported');
    }
  }

  shouldReload(id, version) {
    id = id || null;
    version = version || null;

    if (this.props.process.clone) {
      return false;
    }

    return ((this.props.process.id !== id) || (this.props.process.version !== version));
  }

  error(message, redirect) {
    const isTemplate = this.props.process.template;

    toast.dismiss();
    toast.error(
      <ToastTemplate iconClass='fa-warning' text={message} />
    );

    if ((typeof redirect === 'undefined') || (redirect)) {
      setTimeout(() => {
        this.props.history.push(isTemplate ? StaticRoutes.RecipeExplorer : StaticRoutes.ProcessExplorer);
      }, 500);
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
        return EnumDesignerSaveAction.Save;
      case EnumComponentAction.SaveAndExecute:
        return EnumDesignerSaveAction.SaveAndExecute;
      case EnumComponentAction.SaveAsTemplate:
        return EnumDesignerSaveAction.SaveAsTemplate;
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

    this.props.reset();
  }

  select() {
    this.props.setActiveProcess(this.props.process);
  }

  cancelDialogHandler(action) {
    const isTemplate = this.props.process.template;

    switch (action.key) {
      case EnumComponentAction.Discard:
        this.reset();
        this.props.history.push(isTemplate ? StaticRoutes.RecipeExplorer : StaticRoutes.ProcessExplorer);
        break;

      default:
        this.toggleCancelDialog();
        break;

    }
  }

  save(action) {
    const isTemplate = this.props.process.template || action === EnumComponentAction.SaveAsTemplate;

    toast.dismiss();
    this.props.save(this.mapToSaveAction(action), this.props.designer, isTemplate)
      .then((result) => {
        const text = `${isTemplate ? "Template" : "Process"} has been saved successfully!`;
        toast.success(
          <ToastTemplate iconClass='fa-save' text={text} />
        );
        this.reset();
        this.props.history.push(isTemplate ? StaticRoutes.RecipeExplorer : StaticRoutes.ProcessExplorer);
      })
      .catch((err) => {
        switch (err.level) {
          case EnumErrorLevel.INFO:
            toast.info(
              <ToastTemplate iconClass='fa-warning' text={err.message} />
            );
            this.props.history.push(StaticRoutes.ProcessExplorer);
            break;
          case EnumErrorLevel.WARN:
            toast.warn(
              <ToastTemplate iconClass='fa-warning' text={err.message} />
            );
            this.props.history.push(StaticRoutes.ProcessExplorer);
            break;
          default:
            toast.error(
              <ToastTemplate iconClass='fa-warning' text={err.message} />
            );
            break;
        }
      });
  }

  selectKpi(file, mode) {
    const { id, version, execution } = this.props.match.params;

    this.props.fetchExecutionKpiData(Number.parseInt(id), Number.parseInt(version), Number.parseInt(execution), file, mode)
      .catch(err => {
        toast.error(
          <ToastTemplate iconClass='fa-warning' text={`Failed to load KPI data. ${err.message}`} />
        );
      });
  }

  viewMap() {
    const { id, version, execution } = this.props.match.params;
    const path = buildPath(DynamicRoutes.ProcessExecutionMapViewer, [id, version, execution]);

    this.props.history.push(path);
  }

  renderDesigner() {
    return (
      <div onClick={this.select}>
        {!this.props.readOnly &&
          <Card className="mb-1">
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
          <Execution
            execution={this.props.execution}
            viewMap={this.viewMap}
          />
        }
        <Card>
          {!this.props.readOnly &&
            <CardBody className="card-body">
              <Row>
                <Col>
                  <Button color="danger" onClick={this.toggleCancelDialog} className="float-left">Discard</Button>
                  <Button color="warning" onClick={this.reset} className="float-left ml-3">Clear</Button>
                  <Button color="default" onClick={this.props.undoAction} className="float-left ml-3" disabled={this.props.undo.length === 1}>Undo</Button>
                  <Button color="default" onClick={this.props.redoAction} className="float-left ml-3" disabled={this.props.redo.length === 0}>Redo</Button>
                  <ButtonGroup className="float-right">
                    <Button color="primary" onClick={this.save.bind(this, EnumComponentAction.Save)}>Save</Button>
                    {!this.props.process.template &&
                      <ButtonDropdown isOpen={this.state.saveDropdownOpen} toggle={this.toggleSaveButtonDropdown}>
                        <DropdownToggle caret>
                          More ...
                      </DropdownToggle>
                        <DropdownMenu>
                          <DropdownItem onClick={this.save.bind(this, EnumComponentAction.SaveAndExecute)}>Save & Execute</DropdownItem>
                          {!this.props.process.id &&
                            <DropdownItem onClick={this.save.bind(this, EnumComponentAction.SaveAsTemplate)}>Save Recipe </DropdownItem>
                          }
                        </DropdownMenu>
                      </ButtonDropdown>
                    }
                  </ButtonGroup>
                </Col>
              </Row>
            </CardBody>
          }
          <CardBody className="card-body">
            <Row className="mb-2">
              <Col style={{ padding: '9px' }}>
                <Designer
                  appConfiguration={this.props.appConfiguration}
                  active={this.props.active}
                  execution={this.props.execution}
                  groups={this.props.groups}
                  steps={this.props.steps}
                  resources={this.props.resources}
                  addStep={this.props.addStep}
                  configureStepBegin={this.props.configureStepBegin}
                  showStepExecutionDetails={this.props.showStepExecutionDetails}
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
        appConfiguration={this.props.appConfiguration}
        filesystem={this.props.filesystem}
        step={this.props.view.step}
        stepConfiguration={this.props.view.configuration}
        errors={this.props.view.errors}
        configureStepValidate={this.props.configureStepValidate}
        configureStepUpdate={this.props.configureStepUpdate}
        configureStepEnd={this.props.configureStepEnd}
        readOnly={this.props.readOnly}
        createFolder={this.props.createFolder}
        uploadFile={this.props.uploadFile}
        deletePath={this.props.deletePath}
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
        createFolder={this.props.createFolder}
        uploadFile={this.props.uploadFile}
        deletePath={this.props.deletePath}
      />
    );
  }

  renderStepExecutionDetails() {
    const { active, steps, execution, ...rest } = this.props;

    if ((!active) || (active.type !== EnumSelection.Step)) {
      return null;
    }

    const step = execution.steps.find((s) => s.key === active.step);
    const files = (step ? step.files : []);

    return (
      <ExecutionStepDetails
        checkFile={this.props.checkFile}
        downloadFile={this.props.downloadFile}
        execution={this.props.execution}
        files={files}
        process={this.props.process}
        hideStepExecutionDetails={this.props.hideStepExecutionDetails}
        resetSelectedFile={this.props.resetSelectedFile}
        resetSelectedKpi={this.props.resetSelectedKpi}
        selectedFile={this.props.selectedExecutionFile}
        selectedKpi={this.props.selectedKpi}
        selectFile={this.props.selectFile}
        selectKpi={this.selectKpi}
        step={step}
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
            {this.props.view.type === EnumDesignerView.Designer && this.renderDesigner()}
            {this.props.view.type === EnumDesignerView.StepConfiguration && this.renderStepConfiguration()}
            {this.props.view.type === EnumDesignerView.DataSourceConfiguration && this.renderDataSourceConfiguration()}
            {this.props.view.type === EnumDesignerView.StepExecution && this.renderStepExecutionDetails()}
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
  groups: state.ui.views.process.designer.groups,
  process: state.ui.views.process.designer.process,
  readOnly: state.ui.views.process.designer.readOnly,
  redo: state.ui.views.process.designer.redo,
  resources: state.ui.views.process.designer.resources,
  steps: state.ui.views.process.designer.steps,
  undo: state.ui.views.process.designer.undo,
  view: state.ui.views.process.designer.view,
  // Execution viewer
  execution: state.ui.views.process.designer.execution.data,
  selectedExecutionFile: state.ui.views.process.designer.execution.selectedFile,
  selectedKpi: state.ui.views.process.designer.execution.selectedKpi,
  // File system
  filesystem: state.config.filesystem,
  // Application configuration
  appConfiguration: state.config,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  // Workflow designer
  reset,
  checkFile,
  downloadFile,
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
  showStepExecutionDetails,
  hideStepExecutionDetails,
  fetchExecutionDetails,
  fetchExecutionKpiData,
  resetSelectedFile,
  resetSelectedKpi,
  selectFile,
  // File system
  createFolder,
  uploadFile,
  deletePath,
}, dispatch);

const mergeProps = (stateProps, dispatchProps, ownProps) => {
  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
  };
};

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(ProcessDesigner);
