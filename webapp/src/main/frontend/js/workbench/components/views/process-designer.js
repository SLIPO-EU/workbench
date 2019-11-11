import _ from 'lodash';
import * as React from 'react';
import * as ReactRedux from 'react-redux';

import moment from '../../moment-localized';

import {
  bindActionCreators
} from 'redux';

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
  FormFeedback,
  FormGroup,
  Input,
  Label,
  Row,
} from 'reactstrap';

import {
  FormattedTime,
} from 'react-intl';

import {
  buildPath,
  DynamicRoutes,
  EnumErrorLevel,
  ServerError,
  StaticRoutes,
} from '../../model';

import {
  EnumDesignerMode,
  EnumDesignerSaveAction,
  EnumDesignerView,
  EnumSelection,
} from '../../model/process-designer';

import {
  Dialog,
} from '../helpers';

import {
  DataSourceConfig,
  Designer,
  StepConfig,
  Toolbox,
} from './process/designer';

import {
  DockerLogDetails,
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
  checkLog,
  downloadFile,
  downloadLog,
  fetchDraft,
  fetchProcess,
  fetchProcessRevision,
  fetchExecutionDetails,
  fetchExecutionKpiData,
  fetchExecutionLogData,
  save,
  saveDraft,
  addStep,
  cloneStep,
  removeStep,
  moveStep,
  moveStepInput,
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
  setConfiguration,
  undo,
  redo,
  showStepExecutionDetails,
  showDockerLogDetails,
  hideStepExecutionDetails,
  hideDockerLogDetails,
  resetSelectedFile,
  selectFile,
  selectOutputPart,
  getTripleGeoMappings,
  getTripleGeoMappingFileAsText,
  restoreDraft,
  rejectDraft,
} from '../../ducks/ui/views/process-designer';

import {
  message,
} from '../../service';

const DRAFT_SAVER_INTERVAL = 1000;

/**
 * Component actions
 */
const EnumComponentAction = {
  Accept: 'Accept',
  Discard: 'Discard',
  CloseDialog: 'CloseDialog',
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
      saveAsDialogOpen: false,
      saveAsProcessName: '',
      saveDropdownOpen: false,
      cancelDialogOpen: false,
      draftDialogOpen: false,
      remoteDraft: null,
    };

    this.cancelDialogHandler = this.cancelDialogHandler.bind(this);
    this.draftDialogHandler = this.draftDialogHandler.bind(this);
    this.goToProcessExplorer = this.goToProcessExplorer.bind(this);
    this.onFetchError = this.onFetchError.bind(this);
    this.reset = this.reset.bind(this);
    this.saveAsDialogHandler = this.saveAsDialogHandler.bind(this);
    this.select = this.select.bind(this);
    this.viewKpi = this.viewKpi.bind(this);
    this.viewLog = this.viewLog.bind(this);
    this.toggleCancelDialog = this.toggleCancelDialog.bind(this);
    this.toggleSaveAsDialog = this.toggleSaveAsDialog.bind(this);
    this.toggleSaveButtonDropdown = this.toggleSaveButtonDropdown.bind(this);
    this.viewMap = this.viewMap.bind(this);

    this.draftSaveInterval = null;
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
    const { reloadDraft } = this.props;

    const action = this.resolveMode();

    const reload = this.shouldReload(id, version);

    switch (action) {
      case EnumDesignerMode.CREATE:
        if (reload) {
          this.reset();
        }

        if (reload || reloadDraft) {
          this.props.fetchDraft()
            .then(result => this.onDraftFetchSuccess(action, result))
            .catch(this.onFetchError);
        } else {
          this.onDraftFetchSuccess(action, null);
        }

        break;

      case EnumDesignerMode.EDIT:
        if (reload) {
          this.reset();
          this.props.fetchProcess(Number.parseInt(id))
            .then(result => this.onProcessFetchSuccess(action, result))
            .catch(this.onFetchError);
        }
        break;

      case EnumDesignerMode.VIEW:
        this.reset();
        this.props.fetchProcessRevision(Number.parseInt(id), Number.parseInt(version))
          .then(result => this.onProcessFetchSuccess(action, result))
          .catch(this.onFetchError);
        break;

      case EnumDesignerMode.EXECUTION:
        this.reset();
        this.props.fetchExecutionDetails(Number.parseInt(id), Number.parseInt(version), Number.parseInt(execution))
          .then(result => this.onProcessFetchSuccess(action, result))
          .catch(this.onFetchError);
        break;

      default:
        this.error('Action is not supported');
    }
  }

  componentWillUnmount() {
    this.stopDraftTimer();
  }

  shouldReload(id, version) {
    id = id || null;
    version = version || null;

    if (this.props.process.clone) {
      return false;
    }

    return ((this.props.process.id !== id) || (this.props.process.version !== version));
  }

  error(text) {
    const isTemplate = this.props.process.template;

    message.error(text, 'fa-warning');

    setTimeout(() => {
      this.props.history.push(isTemplate ? StaticRoutes.RecipeExplorer : StaticRoutes.ProcessExplorer);
    }, 500);
  }

  resume() {
    this.setState({ isLoading: false });
  }

  startDraftTimer() {
    this.stopDraftTimer();

    this.draftSaveInterval = setInterval(() => {
      this.props.saveDraft();
    }, DRAFT_SAVER_INTERVAL);
  }

  stopDraftTimer() {
    if (this.draftSaveInterval) {
      clearInterval(this.draftSaveInterval);
      this.draftSaveInterval = null;
    }
  }

  onProcessFetchSuccess(action, result) {
    const { draft = null } = result;

    this.select();
    this.resume();

    switch (action) {
      case EnumDesignerMode.EDIT:
        if (draft) {
          this.toggleDraftDialog(draft);
        } else {
          this.startDraftTimer();
        }
        break;

      default:
      // No action
    }
  }

  onDraftFetchSuccess(action, result) {
    this.select();
    this.resume();

    switch (action) {
      case EnumDesignerMode.CREATE:
        if (result) {
          this.toggleDraftDialog(result);
        } else {
          this.props.rejectDraft();

          this.startDraftTimer();
        }
        break;

      default:
      // No action
    }
  }

  onFetchError(err) {
    this.error(err.message);
  }

  toggleSaveButtonDropdown() {
    this.setState({ saveDropdownOpen: !this.state.saveDropdownOpen });
  }

  toggleSaveAsDialog() {
    const { designer: { process } } = this.props;

    this.setState({
      saveAsDialogOpen: !this.state.saveAsDialogOpen,
      saveAsProcessName: this.state.saveAsDialogOpen ? '' : process.properties.name,
    });
  }

  toggleCancelDialog() {
    this.setState({ cancelDialogOpen: !this.state.cancelDialogOpen });
  }

  toggleDraftDialog(draft = null) {
    this.setState({
      draftDialogOpen: !this.state.draftDialogOpen,
      remoteDraft: this.state.draftDialogOpen ? null : draft,
    });
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

  saveAsDialogHandler(action) {
    switch (action.key) {
      case EnumComponentAction.Accept:
        this.save(EnumDesignerSaveAction.Save, true);
        break;

      default:
        this.toggleSaveAsDialog();
        break;
    }
  }

  draftDialogHandler(action) {
    switch (action.key) {
      case EnumComponentAction.Accept:
        this.restoreDraft();
        break;

      default:
        this.props.rejectDraft();

        this.toggleDraftDialog();
        this.startDraftTimer();
        break;
    }
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

  formatErrors(err) {
    if (err instanceof ServerError) {
      return err.errors.map(e => e.description).join('<br/>');
    } else {
      return err.message;
    }
  }

  save(action, createNew = false) {
    // Stop draft save timer
    this.stopDraftTimer();

    const isTemplate = this.props.process.template || action === EnumDesignerSaveAction.SaveAsTemplate;

    if (action === EnumDesignerSaveAction.SaveAs) {
      this.toggleSaveAsDialog();
      return;
    }

    const designer = _.cloneDeep(this.props.designer);

    if (createNew) {
      designer.process.id = null;
      designer.process.version = null;
      designer.process.properties.name = this.state.saveAsProcessName;
    }

    this.props.save(action, designer, isTemplate)
      .then(() => {
        if (createNew) {
          this.toggleSaveAsDialog();
        }

        const text = `${isTemplate ? "Template" : "Process"} has been saved successfully!`;
        message.success(text, 'fa-save');
        this.reset();
        this.props.history.push(isTemplate ? StaticRoutes.RecipeExplorer : StaticRoutes.ProcessExplorer);
      })
      .catch((err) => {
        // Restart draft saver timer
        this.startDraftTimer();

        switch (err.level) {
          case EnumErrorLevel.INFO:
            message.infoHtml(this.formatErrors(err), 'fa-warning');
            this.props.history.push(StaticRoutes.ProcessExplorer);
            break;
          case EnumErrorLevel.WARN:
            message.warnHtml(this.formatErrors(err), 'fa-warning');
            this.props.history.push(StaticRoutes.ProcessExplorer);
            break;
          default:
            message.errorHtml(this.formatErrors(err), 'fa-warning');
            break;
        }
      });
  }

  restoreDraft() {
    const { remoteDraft: draft } = this.state;

    this.props.restoreDraft(draft.definition);

    this.toggleDraftDialog();
    this.startDraftTimer();
  }

  viewKpi(file) {
    const { id, version, execution } = this.props.match.params;
    const { active: { step: index }, steps } = this.props;

    this.props.fetchExecutionKpiData(
      Number.parseInt(id), Number.parseInt(version), Number.parseInt(execution), file, steps[index].tool
    ).catch(err => {
      message.error(`Failed to load KPI data. ${err.message}`, 'fa-warning');
    });
  }

  viewLog(file) {
    const { id, version, execution } = this.props.match.params;

    this.props.fetchExecutionLogData(Number.parseInt(id), Number.parseInt(version), Number.parseInt(execution), file)
      .catch(err => {
        message.error(`Failed to load docker log data. ${err.message}`, 'fa-warning');
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
                    <Button color="primary" onClick={() => this.save(EnumDesignerSaveAction.Save)} className="mr-1">Save</Button>
                    {!this.props.process.template && this.props.process.id &&
                      <Button color="primary" onClick={this.toggleSaveAsDialog} className="mr-1">Save As ...</Button>
                    }
                    {!this.props.process.template &&
                      <ButtonDropdown isOpen={this.state.saveDropdownOpen} toggle={this.toggleSaveButtonDropdown} direction={'down'}>
                        <DropdownToggle caret>
                          More ...
                        </DropdownToggle>
                        <DropdownMenu>
                          <DropdownItem onClick={() => this.save(EnumDesignerSaveAction.SaveAndExecute)}>Save and Execute</DropdownItem>
                          <DropdownItem onClick={() => this.save(EnumDesignerSaveAction.SaveAndExecuteAndMap)}>Save, Execute and Create Map</DropdownItem>
                          {!this.props.process.id &&
                            <DropdownItem onClick={() => this.save(EnumDesignerSaveAction.SaveAsTemplate)}>Save Recipe </DropdownItem>
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
                  cloneStep={this.props.cloneStep}
                  configureStepBegin={this.props.configureStepBegin}
                  showStepExecutionDetails={this.props.showStepExecutionDetails}
                  showDockerLogDetails={this.props.showDockerLogDetails}
                  removeStep={this.props.removeStep}
                  moveStep={this.props.moveStep}
                  moveStepInput={this.props.moveStepInput}
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
                  selectOutputPart={this.props.selectOutputPart}
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
        setConfiguration={this.props.setConfiguration}
        getTripleGeoMappings={this.props.getTripleGeoMappings}
        getTripleGeoMappingFileAsText={this.props.getTripleGeoMappingFileAsText}
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
        selectedRow={this.props.selectedRow}
        selectedKpi={this.props.selectedKpi}
        selectRow={this.props.selectFile}
        step={step}
        viewKpi={this.viewKpi}
      />
    );
  }

  renderDockerLogDetails() {
    const { active, execution } = this.props;

    if ((!active) || (active.type !== EnumSelection.Step)) {
      return null;
    }

    const step = execution.steps.find((s) => s.key === active.step);
    const logs = (step ? step.logs : []);

    return (
      <DockerLogDetails
        checkLog={this.props.checkLog}
        downloadLog={this.props.downloadLog}
        execution={this.props.execution}
        logs={logs}
        process={this.props.process}
        hideDockerLogDetails={this.props.hideDockerLogDetails}
        resetSelectedFile={this.props.resetSelectedFile}
        selectedRow={this.props.selectedRow}
        selectedLog={this.props.selectedLog}
        selectRow={this.props.selectFile}
        step={step}
        viewLog={this.viewLog}
      />
    );
  }

  renderSaveAsDialog() {
    const { saveAsProcessName } = this.state;
    const { designer: { process } } = this.props;

    const name = process.properties.name;
    const error = !saveAsProcessName || saveAsProcessName === name;

    return (
      <Dialog className="modal-dialog-centered"
        header={
          <span>
            <i className={'fa fa-save mr-2'}></i>
            <span>Save <b>{name}</b> As ...</span>
          </span>
        }
        modal={this.state.saveAsDialogOpen}
        handler={this.saveAsDialogHandler}
        actions={[
          {
            key: EnumComponentAction.Accept,
            label: 'Yes',
            iconClass: 'fa fa-check',
            color: 'primary',
            disabled: error,
          }, {
            key: EnumComponentAction.CloseDialog,
            label: 'No',
            iconClass: 'fa fa-times',
          }
        ]}
      >
        <div style={{ minWidth: 400 }}>
          <FormGroup color={error ? 'danger' : null}>
            <Label for="process-name">Enter a new name for the workflow</Label>
            <Input
              type="text"
              name="process-name"
              id="process-name"
              value={saveAsProcessName || ''}
              autoComplete="off"
              onChange={e => this.setState({ saveAsProcessName: e.target.value })}
              maxLength={80}
            />
            {!saveAsProcessName ? <FormFeedback>Process name is required</FormFeedback> : null}
            {saveAsProcessName && saveAsProcessName === name ? <FormFeedback>A different process name is required</FormFeedback> : null}
          </FormGroup>
        </div>
      </Dialog>
    );
  }

  renderCancelDialog() {
    return (
      <Dialog className="modal-dialog-centered"
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
            key: EnumComponentAction.CloseDialog,
            label: 'No',
            iconClass: 'fa fa-undo',
          }
        ]}
      >
        <div>The form's changes will be discarded. Are you sure you want to continue?</div>
      </Dialog>
    );
  }

  renderDraftDialog() {
    const { remoteDraft: { id, owner, updatedOn } } = this.state;

    return (
      <Dialog className="modal-dialog-centered"
        header={
          <span>
            <i className={'fa fa-database mr-2'}></i>
            <span>{id ? 'Restore most recent version' : 'Restore unsaved draft'}</span>
          </span>
        }
        hideHeaderToggle={true}
        modal={this.state.draftDialogOpen}
        handler={this.draftDialogHandler}
        actions={[
          {
            key: EnumComponentAction.Accept,
            label: 'Yes',
            iconClass: 'fa fa-repeat',
            color: 'primary',
          }, {
            key: EnumComponentAction.CloseDialog,
            label: 'No',
            iconClass: 'fa fa-times',
          }
        ]}
      >
        <div style={{ minWidth: 400 }}>
          <span>{id ? 'A newer version' : 'An unsaved workflow draft'}</span>
          <span>, modified at </span>
          <span className="text-danger"><b>{<FormattedTime value={moment(updatedOn).toDate()} day='numeric' month='numeric' year='numeric' />}</b></span>
          <span> by </span>
          <span className="text-danger"><b>{owner.name}</b></span>
          <span>, has been found</span>
          <span>{id ? ' for the selected workflow' : ''}</span>
          <span>. Would you like to switch to the unsaved </span>
          <span>{id ? 'version' : 'draft'}</span>
          <span>?</span>
        </div>
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
            {this.props.view.type === EnumDesignerView.DockerLogViewer && this.renderDockerLogDetails()}
          </Col>
        </Row>
        {this.state.saveAsDialogOpen &&
          this.renderSaveAsDialog()
        }
        {this.state.cancelDialogOpen &&
          this.renderCancelDialog()
        }
        {this.state.draftDialogOpen &&
          this.renderDraftDialog()
        }
      </div>
    );
  }
}

const mapStateToProps = (state) => ({
  // Workflow designer
  active: state.ui.views.process.designer.active,
  designer: state.ui.views.process.designer,
  draft: state.ui.views.process.designer.draft,
  groups: state.ui.views.process.designer.groups,
  process: state.ui.views.process.designer.process,
  readOnly: state.ui.views.process.designer.readOnly,
  redo: state.ui.views.process.designer.redo,
  reloadDraft: state.ui.views.process.designer.reloadDraft,
  resources: state.ui.views.process.designer.resources,
  steps: state.ui.views.process.designer.steps,
  undo: state.ui.views.process.designer.undo,
  view: state.ui.views.process.designer.view,
  // Execution viewer
  execution: state.ui.views.process.designer.execution.data,
  selectedRow: state.ui.views.process.designer.execution.selectedRow,
  selectedKpi: state.ui.views.process.designer.execution.selectedKpi,
  selectedLog: state.ui.views.process.designer.execution.selectedLog,
  // File system
  filesystem: state.config.filesystem,
  // Application configuration
  appConfiguration: state.config,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  // Workflow designer
  reset,
  checkFile,
  checkLog,
  downloadFile,
  downloadLog,
  fetchProcess,
  fetchProcessRevision,
  save,
  saveDraft,
  addStep,
  cloneStep,
  removeStep,
  moveStep,
  moveStepInput,
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
  setConfiguration,
  undoAction: undo,
  redoAction: redo,
  showStepExecutionDetails,
  showDockerLogDetails,
  hideStepExecutionDetails,
  hideDockerLogDetails,
  fetchDraft,
  fetchExecutionDetails,
  fetchExecutionKpiData,
  fetchExecutionLogData,
  resetSelectedFile,
  restoreDraft,
  rejectDraft,
  selectFile,
  selectOutputPart,
  // File system
  createFolder,
  uploadFile,
  deletePath,
  // TripleGeo ML Mappings
  getTripleGeoMappings,
  getTripleGeoMappingFileAsText,
}, dispatch);

const mergeProps = (stateProps, dispatchProps, ownProps) => {
  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
  };
};

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(ProcessDesigner);
