import * as React from 'react';
import PropTypes from 'prop-types';
import { DragSource, DropTarget } from 'react-dnd';
import classnames from 'classnames';

import {
  EnumDragSource,
  EnumStepProperty,
  EnumResourceType,
  EnumSelection,
  EnumTool,
} from './constants';
import {
  ToolInput
} from './config';
import StepInputContainer from './step-input-container';
import StepDataSourceContainer from './step-data-source-container';

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
    if (props.step.tool === EnumTool.CATALOG) {
      return false;
    }
    return true;
  },

  /**
   * Returns a plain JavaScript object describing the data being dragged
   *
   * @param {any} props
   * @returns a plain JavaScript object
   */
  beginDrag(props) {
    return {
      index: props.step.index,
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
      order: PropTypes.number.isRequired,
      index: PropTypes.number.isRequired,
    }),
    resources: PropTypes.arrayOf(PropTypes.shape({
      index: PropTypes.number.isRequired,
      inputType: PropTypes.string.isRequired,
      resourceType: PropTypes.string.isRequired,
      title: PropTypes.string.isRequired,
      iconClass: PropTypes.string.isRequired,
    })).isRequired,

    // Action creators
    removeStep: PropTypes.func.isRequired,
    moveStep: PropTypes.func.isRequired,
    configureStepBegin: PropTypes.func.isRequired,
    setStepProperty: PropTypes.func.isRequired,

    addStepInput: PropTypes.func.isRequired,
    removeStepInput: PropTypes.func.isRequired,

    addStepDataSource: PropTypes.func.isRequired,
    removeStepDataSource: PropTypes.func.isRequired,
    configureStepDataSourceBegin: PropTypes.func.isRequired,

    setActiveStep: PropTypes.func.isRequired,
    setActiveStepInput: PropTypes.func.isRequired,
    setActiveStepDataSource: PropTypes.func.isRequired,

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
    const { source, poi, linked, any } = ToolInput[step.tool];

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
    this.props.setStepProperty(this.props.step.index, EnumStepProperty.Title, e.target.value);
  }

  /**
   * Resolve step icon class
   *
   * @returns a CSS class
   * @memberof Step
   */
  getIconClassName() {
    if (this.props.step.iconClass) {
      return this.props.step.iconClass + ' mr-2 slipo-pd-step-icon';
    }
    return 'fa fa-cogs mr-2 slipo-pd-step-icon';
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
   * Initialize the configuration of the current step
   *
   * @param {any} e
   * @memberof Step
   */
  configure(e) {
    e.stopPropagation();

    this.props.configureStepBegin(this.props.step, this.props.step.configuration);
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
      (this.props.active.step === this.props.step.index)
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
      (this.props.step.title) &&
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
            <i className={this.getIconClassName()}></i>
            <input
              id={`step-${this.props.step.index}-title`}
              className={
                classnames({
                  "slipo-pd-step-title-input": true,
                  "slipo-pd-step-title-input-invalid": (!this.props.step.title)
                })
              }
              value={this.props.step.title}
              ref={(el) => { this._titleElement = el; }}
              onClick={(e) => { this._titleElement.focus; this._titleElement.select(e); }}
              onChange={this.setTitle}
            />
            <div className="slipo-pd-step-actions">
              <i className="slipo-pd-step-action slipo-pd-step-config fa fa-wrench" onClick={(e) => { this.configure(e); }}></i>
              <i className="slipo-pd-step-action slipo-pd-step-delete fa fa-trash" onClick={(e) => { this.remove(e); }}></i>
            </div>
          </div>
          {this.props.step.tool != EnumTool.TripleGeo &&
            < StepInputContainer
              active={this.props.active}
              step={this.props.step}
              resources={this.props.resources}
              addStepInput={this.props.addStepInput}
              removeStepInput={this.props.removeStepInput}
              setActiveStepInput={this.props.setActiveStepInput}
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
            />
          }
        </div >
      )
    );
  }

}

export default Step;
