import * as React from 'react';
import PropTypes from 'prop-types';
import { DragSource, DropTarget } from 'react-dnd';
import classnames from 'classnames';

import {
  EnumTool,
  EnumToolboxItem,
  EnumDragSource,
} from './constants';

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

    // Determine rectangle on screen
    const hoverBoundingRect = component.decoratedComponentInstance._element.getBoundingClientRect();

    // Get vertical middle
    const hoverMiddleY = (hoverBoundingRect.bottom - hoverBoundingRect.top) / 2;

    // Determine mouse position
    const clientOffset = monitor.getClientOffset();

    // Get pixels to the top
    const hoverClientY = clientOffset.y - hoverBoundingRect.top;

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
    super();
  }

  static propTypes = {
    active: PropTypes.object.isRequired,
    step: PropTypes.shape({
      order: PropTypes.number.isRequired,
      index: PropTypes.number.isRequired,
    }),

    // Action creators
    removeStep: PropTypes.func.isRequired,
    moveStep: PropTypes.func.isRequired,
    configureStepBegin: PropTypes.func.isRequired,

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
   * Resolve step icon class
   *
   * @returns a CSS class
   * @memberof Step
   */
  getIconClassName() {
    if (this.props.step.iconClass) {
      return this.props.step.iconClass;
    }
    return 'fa fa-cogs mr-2';
  }

  /**
   * Resolve step title
   *
   * @returns a string
   * @memberof Step
   */
  getTitle() {
    if (this.props.step.title) {
      return this.props.step.title;
    }
    switch (this.props.step.type) {
      case EnumToolboxItem.Operation:
        return this.props.step.tool;
      case EnumToolboxItem.Harvester:
        return this.props.step.harvester;
      case EnumToolboxItem.DataSource:
        return this.props.step.source;
    }
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
  select() {
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
      (this.props.active.step == this.props.step.index) &&
      (this.props.active.stepInput == null) &&
      (this.props.active.stepDataSource == null)
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
              "slipo-pd-step-invalid": (!this.props.step.configuration),
            })
          }
          onClick={() => this.select()}
          ref={(el) => { this._element = el; }}
        >
          <div className={
            classnames({
              "slipo-pd-step-header": true,
              "slipo-pd-step-invalid": (!this.props.step.configuration),
            })
          }>
            <i className={this.getIconClassName()}></i> <span>{this.getTitle()}</span>
            <div className="slipo-pd-step-actions">
              <i className="slipo-pd-step-action slipo-pd-step-config fa fa-wrench" onClick={(e) => { this.configure(e); }}></i>
              <i className="slipo-pd-step-action slipo-pd-step-delete fa fa-trash" onClick={(e) => { this.remove(e); }}></i>
            </div>
          </div>
          {this.props.step.type == EnumToolboxItem.Operation && this.props.step.tool != EnumTool.TripleGeo &&
            < StepInputContainer
              active={this.props.active}
              step={this.props.step}
              addStepInput={this.props.addStepInput}
              removeStepInput={this.props.removeStepInput}
              setActiveStepInput={this.props.setActiveStepInput}
            />
          }
          {this.props.step.type == EnumToolboxItem.Operation && this.props.step.tool == EnumTool.TripleGeo &&
            <StepDataSourceContainer
              active={this.props.active}
              step={this.props.step}
              addStepDataSource={this.props.addStepDataSource}
              removeStepDataSource={this.props.removeStepDataSource}
              configureStepDataSourceBegin={this.props.configureStepDataSourceBegin}
              setActiveStepDataSource={this.props.setActiveStepDataSource}
            />
          }
        </div>
      )
    );
  }

}

export default Step;
