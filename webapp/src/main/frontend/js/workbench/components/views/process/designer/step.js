import * as React from 'react';
import PropTypes from 'prop-types';
import { DragSource, DropTarget } from 'react-dnd';
import classnames from 'classnames';

import {
  Roles,
} from '../../../../model';

import {
  EnumDragSource,
  EnumStepProperty,
  EnumResourceType,
  EnumSelection,
  EnumTool,
  ToolConfigurationSettings,
} from '../../../../model/process-designer';

import dom from '../../../../service/api/dom';

import {
  writeConfiguration,
} from '../../../../service/toolkit';

import {
  JobStatus,
  SecureContent,
} from '../../../helpers';

import {
  StepDataSourceContainer,
  StepInputContainer,
} from './';

/**
 * Drag source specification
 */
const stepSource = {
  /**
   * Specify whether the dragging is currently allowed
   *
   * @param {any} props
   * @param {any} monitor
   */
  canDrag(props, monitor) {
    return !props.readOnly;
  },

  /**
   * Returns a plain JavaScript object describing the data being dragged
   *
   * @param {any} props
   * @returns a plain JavaScript object
   */
  beginDrag(props) {
    return {
      key: props.step.key,
      order: props.step.order,
      step: props.step,
    };
  }
};

/**
 * Drop target specification
 */
const stepTarget = {
  /**
   * Called when a compatible item is dropped on the target
   *
   * @param {any} props
   * @param {any} monitor
   * @param {any} component
   */
  drop(props, monitor, component) {
    if (!monitor.didDrop()) {
      // TODO: Reorder
    }
  },

  /**
   * Called when an item is hovered over the component
   *
   * @param {any} props
   * @param {any} monitor
   * @param {any} component
   */
  hover(props, monitor, component) {
    const dragOrder = monitor.getItem().order;
    const hoverOrder = props.step.order;

    if (!monitor.canDrop()) {
      return;
    }

    // Don't replace items with themselves
    if (dragOrder === hoverOrder) {
      return;
    }

    // Time to actually perform the action
    props.moveStep(dragOrder, hoverOrder);

    // Note: we're mutating the monitor item here!
    // Generally it's better to avoid mutations,
    // but it's good here for the sake of performance
    // to avoid expensive index searches.
    monitor.getItem().order = hoverOrder;
  },

  /**
   * Specify whether the drop target is able to accept the item
   *
   * @param {any} props
   * @param {any} monitor
   * @returns true if the item is accepted
   */
  canDrop(props, monitor) {
    const source = monitor.getItem().step.tool;
    const target = props.step.tool;

    if (target === EnumTool.CATALOG) {
      return false;
    }
    if (source === EnumTool.TripleGeo) {
      return (target === EnumTool.TripleGeo);
    } else {
      return (target !== EnumTool.TripleGeo);
    }
  },
};

/**
 * A presentational component for rendering a process step. Using example from
 * http://react-dnd.github.io/react-dnd/examples-sortable-simple.html for
 * implementing step reordering.
 *
 *
 * @class Step
 * @extends {React.Component}
 */
@DropTarget(EnumDragSource.Step, stepTarget, (connect, monitor) => ({
  connectDropTarget: connect.dropTarget(),
}))
@DragSource(EnumDragSource.Step, stepSource, (connect, monitor) => ({
  connectDragSource: connect.dragSource(),
  isDragging: monitor.isDragging(),
}))
class Step extends React.Component {

  constructor(props) {
    super(props);

    this.setTitle = this.setTitle.bind(this);
  }

  static propTypes = {
    active: PropTypes.object.isRequired,
    step: PropTypes.shape({
      key: PropTypes.number.isRequired,
      order: PropTypes.number.isRequired,
    }),
    resources: PropTypes.arrayOf(PropTypes.shape({
      key: PropTypes.number.isRequired,
      inputType: PropTypes.string.isRequired,
      resourceType: PropTypes.string.isRequired,
      name: PropTypes.string.isRequired,
      iconClass: PropTypes.string.isRequired,
    })).isRequired,
    stepExecution: PropTypes.object,

    // Action creators
    removeStep: PropTypes.func.isRequired,
    moveStep: PropTypes.func.isRequired,
    configureStepBegin: PropTypes.func.isRequired,
    setStepProperty: PropTypes.func.isRequired,
    showStepExecutionDetails: PropTypes.func.isRequired,

    addStepInput: PropTypes.func.isRequired,
    removeStepInput: PropTypes.func.isRequired,

    addStepDataSource: PropTypes.func.isRequired,
    removeStepDataSource: PropTypes.func.isRequired,
    configureStepDataSourceBegin: PropTypes.func.isRequired,

    setActiveStep: PropTypes.func.isRequired,
    setActiveStepInput: PropTypes.func.isRequired,
    setActiveStepDataSource: PropTypes.func.isRequired,

    // Top-level designer properties
    readOnly: PropTypes.bool.isRequired,

    // Injected by React DnD
    connectDragSource: PropTypes.func.isRequired,
    connectDropTarget: PropTypes.func.isRequired,
    isDragging: PropTypes.bool.isRequired,
  };

  /**
   * Returns plain JavaScript object with required input counters
   *
   * @param {any} step
   * @returns a plain JavaScript object
   */
  isInputMissing() {
    const step = this.props.step;
    const { source, poi, linked, any } = ToolConfigurationSettings[step.tool];

    const counters = this.props.resources.reduce((counters, resource) => {
      switch (resource.resourceType) {
        case EnumResourceType.POI:
          counters.poi++;
          break;
        case EnumResourceType.LINKED:
          counters.linked++;
          break;
      }

      return counters;
    }, { poi: 0, linked: 0 });

    return (
      ((poi - counters.poi) > 0) ||
      ((linked - counters.linked) > 0) ||
      ((any - counters.poi - counters.linked) > 0) ||
      ((source - step.dataSources.length) > 0)
    );
  }


  setTitle(e) {
    this.props.setStepProperty(this.props.step.key, EnumStepProperty.Title, e.target.value);
  }

  /**
   * Resolve step icon class
   *
   * @returns a CSS class
   * @memberof Step
   */
  getIconClassName() {
    if (this.props.step.iconClass) {
      return this.props.step.iconClass + ' slipo-pd-step-icon';
    }
    return 'fa fa-cogs slipo-pd-step-icon';
  }

  /**
   * Remove referenced step from the workflow
   *
   * @param {any} e
   * @memberof Step
   */
  remove(e) {
    e.stopPropagation();

    this.props.removeStep(this.props.step);
  }

  /**
   * Set referenced step as the active one
   *
   * @memberof Step
   */
  select(e) {
    e.stopPropagation();

    this.props.setActiveStep(this.props.step);
  }

  /**
   * Download configuration as JSON
   *
   * @param {*} e
   * @memberof Step
   */
  downloadConfiguration(e) {
    e.stopPropagation();

    const { step: { name, tool, configuration } } = this.props;

    if ((!ToolConfigurationSettings[tool].allowExport) || (!configuration)) {
      return;
    }
    const data = writeConfiguration(tool, configuration);
    const blob = new Blob([JSON.stringify(data, null, 2)], {
      type: 'application/json'
    });
    const fileName = name.split(' ').filter(value => !!value).join('-').toLowerCase();
    dom.downloadBlob(blob, `${tool}-config-${fileName}.json`);
  }

  /**
   * Clone selected step
   *
   * @param {*} e
   * @memberof Step
   */
  clone(e) {
    e.stopPropagation();

    this.props.cloneStep(this.props.step);
  }

  /**
   * Initialize the configuration of the current step
   *
   * @param {any} e
   * @memberof Step
   */
  configure(e) {
    e.stopPropagation();

    this.props.setActiveStep(this.props.step);
    this.props.configureStepBegin(this.props.step, this.props.step.configuration);
  }

  /**
   * Shows details for the current step
   *
   * @param {any} e
   * @memberof Step
   */
  viewDetails(e) {
    e.stopPropagation();

    this.props.setActiveStep(this.props.step);
    this.props.showStepExecutionDetails();
  }

  /**
   * Resolve if the current step is active
   *
   * @returns true if the step is selected
   * @memberof Step
   */
  isActive() {
    return (
      (this.props.active.type === EnumSelection.Step) &&
      (this.props.active.step === this.props.step.key)
    );
  }

  /**
   * Resolve if step configuration is valid
   *
   * @returns return true if the step configuration is valid
   * @memberof Step
   */
  isValid() {
    return (
      (this.props.step.name) &&
      (this.props.step.configuration) &&
      (!this.props.step.errors || Object.keys(this.props.step.errors).length === 0) &&
      (!this.isInputMissing())
    );
  }

  render() {
    const {
      isDragging,
      connectDragSource,
      connectDropTarget,
    } = this.props;

    return connectDragSource(
      connectDropTarget(
        <div
          className={
            classnames({
              "slipo-pd-step": true,
              "slipo-pd-step-active": this.isActive(),
              "slipo-pd-step-invalid": !this.isValid(),
            })
          }
          onClick={(e) => this.select(e)}
        >
          <div className={
            classnames({
              "slipo-pd-step-header": true,
              "slipo-pd-step-invalid": !this.isValid(),
            })
          }>
            <div>
              <i className={this.getIconClassName()}></i>
            </div>
            {!this.props.readOnly &&
              <div className="slipo-pd-step-name-input-wrapper">
                <input
                  id={`step-${this.props.step.key}-name`}
                  className={
                    classnames({
                      "slipo-pd-step-name-input": true,
                      "slipo-pd-step-name-input-invalid": (!this.props.step.name)
                    })
                  }
                  value={this.props.step.name}
                  ref={(el) => { this._titleElement = el; }}
                  onClick={(e) => { this._titleElement.focus; this._titleElement.select(e); }}
                  onChange={this.setTitle}
                />
              </div>
            }
            {this.props.readOnly &&
              <div className="slipo-pd-step-name-input-wrapper">
                <input
                  id={`step-${this.props.step.key}-name`}
                  className={
                    classnames({
                      "slipo-pd-step-name-input": true,
                    })
                  }
                  value={this.props.step.name}
                  readOnly
                />
              </div>
            }
            {this.props.readOnly ?
              <div className="slipo-pd-step-actions">
                {this.props.stepExecution && this.props.stepExecution.files && this.props.stepExecution.files.length !== 0 &&
                  <i
                    className="slipo-pd-step-action slipo-pd-step-config fa fa-folder-open"
                    title="View step files"
                    onClick={(e) => { this.viewDetails(e); }}></i>
                }
                {ToolConfigurationSettings[this.props.step.tool].editable &&
                  <i
                    className="slipo-pd-step-action slipo-pd-step-config fa fa-wrench"
                    title="View configuration"
                    onClick={(e) => { this.configure(e); }}></i>
                }
              </div>
              :
              <div className="slipo-pd-step-actions">
                {ToolConfigurationSettings[this.props.step.tool].allowExport &&
                  <SecureContent roles={[Roles.DEVELOPER]}>
                    <i
                      className="slipo-pd-step-action slipo-pd-step-config fa fa-cloud-download"
                      title="Download configuration as JSON"
                      onClick={(e) => { this.downloadConfiguration(e); }}></i>
                  </SecureContent>
                }
                {ToolConfigurationSettings[this.props.step.tool].allowClone &&
                  <i
                    className="slipo-pd-step-action slipo-pd-step-clone fa fa-clone"
                    title="Clone step"
                    onClick={(e) => { this.clone(e); }}></i>
                }
                {ToolConfigurationSettings[this.props.step.tool].editable &&
                  <i
                    className="slipo-pd-step-action slipo-pd-step-config fa fa-wrench"
                    title="Edit configuration"
                    onClick={(e) => { this.configure(e); }}></i>
                }
                <i
                  className="slipo-pd-step-action slipo-pd-step-delete fa fa-trash"
                  title="Delete step"
                  onClick={(e) => { this.remove(e); }}></i>
              </div>
            }
          </div>
          {this.props.step.tool != EnumTool.TripleGeo &&
            <StepInputContainer
              active={this.props.active}
              step={this.props.step}
              resources={this.props.resources}
              addStepInput={this.props.addStepInput}
              removeStepInput={this.props.removeStepInput}
              setActiveStepInput={this.props.setActiveStepInput}
              readOnly={this.props.readOnly}
              selectOutputPart={this.props.selectOutputPart}
            />
          }
          {this.props.step.tool == EnumTool.TripleGeo &&
            <StepDataSourceContainer
              active={this.props.active}
              step={this.props.step}
              addStepDataSource={this.props.addStepDataSource}
              removeStepDataSource={this.props.removeStepDataSource}
              configureStepDataSourceBegin={this.props.configureStepDataSourceBegin}
              setActiveStepDataSource={this.props.setActiveStepDataSource}
              readOnly={this.props.readOnly}
            />
          }
          {this.props.stepExecution &&
            <div className='m-1'>
              <JobStatus status={this.props.stepExecution.status} />
            </div>
          }
        </div >
      )
    );
  }

}

export default Step;
