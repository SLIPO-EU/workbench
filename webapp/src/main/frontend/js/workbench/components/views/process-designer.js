import * as React from 'react';
import * as ReactRedux from 'react-redux';
import { FormattedTime } from 'react-intl';
import {
  Card, CardBody, Row, Col, Button, ButtonGroup, DropdownMenu, DropdownItem, DropdownToggle, ButtonDropdown
} from 'reactstrap';
import { bindActionCreators } from 'redux';
import { toast } from 'react-toastify';

import {
  StaticRoutes
} from '../../model/routes';
import {
  EnumHarvester,
  EnumTool,
  EnumOperation,
  EnumViews,
  Operation,
  Designer,
  Toolbox,
  StepConfig,
  DataSourceConfig,
} from './process/designer';
import {
  reset,
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
  setActiveStep,
  setActiveStepInput,
  setActiveStepDataSource,
  setActiveResource,
  undo,
  redo,
} from '../../ducks/ui/views/process-designer';

import Dialog from '../helpers/dialog';
import ToastTemplate from '../helpers/toast-template';

const EnumAction = {
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
      dropdownOpen: false,
      showCancelDialog: false,
    };

    this.toggleButtonMenu = this.toggleButtonMenu.bind(this);
    this.toggleCancelDialog = this.toggleCancelDialog.bind(this);
    this.cancelDialogHandler = this.cancelDialogHandler.bind(this);
  }

  toggleButtonMenu() {
    this.setState({ dropdownOpen: !this.state.dropdownOpen });
  }

  toggleCancelDialog() {
    this.setState({ showCancelDialog: !this.state.showCancelDialog });
  }

  cancelDialogHandler(action) {
    switch (action.key) {
      case EnumAction.Discard:
        this.props.history.push(StaticRoutes.ProcessExplorer);
        break;
      default:
        this.toggleCancelDialog();
        break;
    }
  }

  save(action, e) {
    toast.dismiss();

    switch (action) {
      case EnumAction.Save:
        toast.success(
          <ToastTemplate iconClass='fa-save' text='Process has been saved successfully!' />
        );
        break;
      case EnumAction.SaveAndExecute:
        toast.error(
          <ToastTemplate iconClass='fa-warning' text='Process execution failed to start!' />
        );
        return;
      case EnumAction.SaveAsTemplate:
        toast.warning(
          <ToastTemplate iconClass='fa-warning' text='Failed to save template' />
        );
        return;
    }
    this.props.reset();
    this.props.history.push(StaticRoutes.ProcessExplorer);
  }

  renderDesigner() {
    return (
      <Card onClick={(e) => { this.props.resetActive(); }}>
        <CardBody className="card-body">
          <Row className="mb-2">
            <Col>
              <Toolbox />
              <p className='text-muted slipo-pd-tip float-left'>Drag SLIPO Toolkit components to the designer surface for building a workflow.</p>
            </Col>
          </Row>
          <Row>
            <Col>
              <Button color="danger" onClick={this.toggleCancelDialog} className="float-left">Discard</Button>
              <Button color="warning" onClick={this.props.reset} className="float-left ml-3">Clear</Button>
              <Button color="default" onClick={this.props.undoAction} className="float-left ml-3" disabled={this.props.undo.length === 1}>Undo</Button>
              <Button color="default" onClick={this.props.redoAction} className="float-left ml-3" disabled={this.props.redo.length === 0}>Redo</Button>
              <ButtonGroup className="float-right">
                <Button color="primary" onClick={this.save.bind(this, EnumAction.Save)}>Save</Button>
                <ButtonDropdown isOpen={this.state.dropdownOpen} toggle={this.toggleButtonMenu}>
                  <DropdownToggle caret>
                    More ...
                  </DropdownToggle>
                  <DropdownMenu>
                    <DropdownItem onClick={this.save.bind(this, EnumAction.SaveAndExecute)}>Save & Execute</DropdownItem>
                    <DropdownItem onClick={this.save.bind(this, EnumAction.SaveAsTemplate)}>Save Template </DropdownItem>
                  </DropdownMenu>
                </ButtonDropdown>
              </ButtonGroup>

            </Col>
          </Row>
        </CardBody>
        <CardBody className="card-body">
          <Row className="mb-2">
            <Col style={{ padding: '9px' }}>
              <Designer
                active={this.props.active}
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
              />
            </Col>
          </Row>
        </CardBody>
      </Card>
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
        modal={this.state.showCancelDialog}
        handler={this.cancelDialogHandler}
        actions={[
          {
            key: EnumAction.Discard,
            label: 'Yes',
            iconClass: 'fa fa-trash',
            color: 'danger',
          }, {
            key: EnumAction.CloseCancelDialog,
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
    return (
      <div className="animated fadeIn">
        <Row>
          <Col>
            {this.props.view.type === EnumViews.Designer && this.renderDesigner()}
            {this.props.view.type === EnumViews.StepConfiguration && this.renderStepConfiguration()}
            {this.props.view.type === EnumViews.DataSourceConfiguration && this.renderDataSourceConfiguration()}
          </Col>
        </Row>
        {this.state.showCancelDialog &&
          this.renderCancelDialog()
        }
      </div>
    );
  }
}


const mapStateToProps = (state) => ({
  view: state.ui.views.process.designer.view,
  active: state.ui.views.process.designer.active,
  steps: state.ui.views.process.designer.steps,
  resources: state.ui.views.process.designer.resources,
  undo: state.ui.views.process.designer.undo,
  redo: state.ui.views.process.designer.redo,
  filesystem: state.config.filesystem,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  reset,
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
  setActiveStep,
  setActiveStepInput,
  setActiveStepDataSource,
  setActiveResource,
  undoAction: undo,
  redoAction: redo,
}, dispatch);

const mergeProps = (stateProps, dispatchProps, ownProps) => {
  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
  };
};

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(ProcessDesigner);
