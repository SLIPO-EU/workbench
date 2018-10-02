import * as React from 'react';
import { DragSource, DropTarget } from 'react-dnd';
import { Link } from 'react-router-dom';
import { Popover, PopoverBody, } from 'reactstrap';

import classnames from 'classnames';

import {
  DynamicRoutes,
  buildPath,
} from '../../../../model/routes';

import {
  DEFAULT_OUTPUT_PART,
  EnumDragSource,
  EnumInputType,
  ToolConfigurationSettings
} from '../../../../model/process-designer';

import {
  Checkbox,
} from '../../../helpers';


/**
 * Drag source specification
 */
const stepInputSource = {
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
      source: EnumDragSource.StepInput,
      data: {
        order: props.order,
        step: props.step,
        resource: props.resource,
      }
    };
  }
};

/**
 * Drop target specification
 */
const stepInputTarget = {

  /**
   * Called when an item is hovered over the component
   *
   * @param {any} props
   * @param {any} monitor
   * @param {any} component
   */
  hover(props, monitor, component) {
    const dragOrder = monitor.getItem().data.order;
    const hoverOrder = props.order;

    if (!monitor.canDrop()) {
      return;
    }

    // Don't replace items with themselves
    if (dragOrder === hoverOrder) {
      return;
    }

    // Time to actually perform the action
    props.moveStepInput(props.step, dragOrder, hoverOrder);

    // Note: we're mutating the monitor item here!
    // Generally it's better to avoid mutations,
    // but it's good here for the sake of performance
    // to avoid expensive index searches.
    monitor.getItem().data.order = hoverOrder;
  },

  /**
   * Specify whether the drop target is able to accept the item
   *
   * @param {any} props
   * @param {any} monitor
   * @returns true if the item is accepted
   */
  canDrop(props, monitor) {
    const item = monitor.getItem();

    // Ignore the same input
    if (props.resource.key === item.data.resource.key) {
      return false;
    }
    // Allow only input from the same step
    if (props.step.key === item.data.step.key) {
      return true;
    }
    return false;
  },
};


/**
 * A presentational component for rendering a process resource input.
 *
 * @class StepInput
 * @extends {React.Component}
 */
@DragSource(EnumDragSource.StepInput, stepInputSource, (connect, monitor) => ({
  connectDragSource: connect.dragSource(),
  isDragging: monitor.isDragging(),
}))
@DropTarget(EnumDragSource.StepInput, stepInputTarget, (connect, monitor) => ({
  connectDropTarget: connect.dropTarget(),
}))
class StepInput extends React.Component {

  constructor(props) {
    super();

    this.state = {
      popoverOpen: false,
    };
  }

  /**
   * Remove resource from the step
   *
   * @memberof StepInput
   */
  onRemove() {
    this.props.remove(this.props.step, this.props.resource);
  }

  /**
   * Set referenced resource as the active one
   *
   * @param {any} e
   * @memberof StepInput
   */
  onSelect(e) {
    e.stopPropagation();

    this.props.setActiveStepInput(this.props.step, this.props.resource);
  }

  onToggleOutputPartSelection(e) {
    if (this.props.readOnly) {
      return;
    }
    this.setState({
      popoverOpen: !this.state.popoverOpen,
    });

  }

  onSelectOutputPart(checked, partKey) {
    if (checked) {
      this.props.selectOutputPart(this.props.step, this.props.resource, partKey);
    }
  }

  render() {
    const { connectDragSource, connectDropTarget, step, resource } = this.props;
    const input = step.input.find((i) => i.inputKey === resource.key);
    const popoverId = `popover-${step.key}-${resource.key}`;
    const partKey = input.partKey;
    const outputParts = resource.inputType === EnumInputType.OUTPUT ? ToolConfigurationSettings[resource.tool].outputParts : null;
    let icon = 0;

    return connectDragSource(
      connectDropTarget(
        <div
          id={popoverId}
          className={
            classnames({
              "slipo-pd-step-input": true,
              "slipo-pd-step-input-active": this.props.active,
            })
          }
          onClick={(e) => this.onSelect(e)}
        >
          <div className="slipo-pd-step-resource-actions">
            {this.props.resource.inputType === EnumInputType.CATALOG &&
              <Link to={buildPath(DynamicRoutes.ResourceViewer, [this.props.resource.id, this.props.resource.version])}>
                <i
                  className={`slipo-pd-step-resource-action slipo-pd-step-resource-view slipo-pd-step-resource-${icon++} fa fa-search`}
                  title="View resource">
                </i>
              </Link>
            }
            {!this.props.readOnly &&
              <i
                className={`slipo-pd-step-resource-action slipo-pd-step-resource-delete slipo-pd-step-resource-${icon++} fa fa-trash`}
                title="Delete"
                onClick={() => { this.onRemove(); }}>
              </i>
            }
          </div>
          <div className="slipo-pd-step-input-icon">
            <i className={this.props.resource.iconClass}></i>
          </div>
          <p className="slipo-pd-step-input-label">
            {this.props.resource.name}
          </p>
          {outputParts &&
            <p className={
              classnames({
                "slipo-pd-step-input-part-key": true,
                "slipo-pd-step-input-part-key-enabled": !this.props.readOnly,
              })
            }>
              <a
                onClick={(e) => this.onToggleOutputPartSelection(e)}
              >{partKey ? outputParts[partKey] : DEFAULT_OUTPUT_PART}</a>
              {this.props.resource.inputType !== EnumInputType.CATALOG &&
                <Popover
                  placement="bottom"
                  isOpen={this.state.popoverOpen}
                  target={popoverId}
                  toggle={(e) => this.onToggleOutputPartSelection(e)}
                  className="slipo-pd-step-input-partial-output-popover"
                >
                  <PopoverBody>
                    {this.renderOutputPartList(partKey)}
                  </PopoverBody>
                </Popover>
              }
            </p>
          }
        </div>
      )
    );
  }

  renderOutputPartList(partKey) {
    const tool = this.props.resource.tool;
    const outputParts = ToolConfigurationSettings[tool].outputParts;

    return Object.keys(outputParts).map((value) =>
      <Checkbox
        key={value || DEFAULT_OUTPUT_PART}
        id={value}
        text={outputParts[value] || DEFAULT_OUTPUT_PART}
        value={value === partKey || value === DEFAULT_OUTPUT_PART && partKey === null}
        state="success"
        readOnly={false}
        onChange={(checked) => this.onSelectOutputPart(checked, value)}
      />
    );
  }
}

export default StepInput;
