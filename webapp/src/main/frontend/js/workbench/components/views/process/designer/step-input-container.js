import * as React from 'react';
import { DropTarget } from 'react-dnd';
import classnames from 'classnames';

import {
  EnumDragSource,
  EnumTool,
  EnumProcessInput,
  EnumResourceType,
  EnumSelection,
} from './constants';
import { ToolInput } from './config';
import StepInput from './step-input';

/**
 * Returns plain JavaScript object with required input counters
 *
 * @param {any} step
 * @returns a plain JavaScript object
 */
function getRequiredResources(step, resources) {
  let { poi, linked, any } = ToolInput[step.tool];

  let counters = resources.reduce((counters, resource) => {
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

  return {
    poi: poi - counters.poi,
    linked: linked - counters.linked,
    any: any - counters.poi - counters.linked,
  };
}

/**
 * Drop target specification
 */
const containerTarget = {
  /**
   * Called when a compatible item is dropped on the target
   *
   * @param {any} props
   * @param {any} monitor
   * @param {any} component
   */
  drop(props, monitor, component) {
    if (!monitor.didDrop()) {
      props.addStepInput(props.step, {
        ...monitor.getItem(),
      });
    }
  },

  /**
   * Specify whether the drop target is able to accept the item
   *
   * @param {any} props
   * @param {any} monitor
   * @returns true if the item is accepted
   */
  canDrop(props, monitor) {
    const resource = monitor.getItem();
    const counters = getRequiredResources(props.step, props.resources);

    // Do not accept owner step output
    if ((resource.inputType === EnumProcessInput.OUTPUT) && (resource.stepIndex == props.step.index)) {
      return false;
    }
    // Catalog registration should accept only step output as input
    if ((resource.inputType !== EnumProcessInput.OUTPUT) && (props.step.tool === EnumTool.CATALOG)) {
      return false;
    }
    // Do not accept existing input
    if (props.resources.filter((r) => r.index === resource.index).length > 0) {
      return false;
    }

    switch (resource.resourceType) {
      case EnumResourceType.POI:
        return ((counters.poi > 0) || (counters.any > 0));
      case EnumResourceType.LINKED:
        return ((counters.linked != 0) || (counters.any > 0));
    }
    return false;
  }
};

function collect(connect, monitor) {
  return {
    connectDropTarget: connect.dropTarget(),
    isOver: monitor.isOver()
  };
}

/**
 * A presentational component which acts as a drop target for resource items
 * inside a process step.
 *
 * @class StepInputContainer
 * @extends {React.Component}
 */
@DropTarget([EnumDragSource.Resource], containerTarget, (connect, monitor) => ({
  connectDropTarget: connect.dropTarget(),
  isOver: monitor.isOver(),
  canDrop: monitor.canDrop(),
}))
class StepInputContainer extends React.Component {

  /**
   * Renders a single {@link StepInput}
   *
   * @param {any} resource
   * @returns a {@link StepInput} component instance
   * @memberof StepInputContainer
   */
  renderResource(resource) {
    return (
      <StepInput
        key={resource.index}
        active={
          (this.props.active.type === EnumSelection.Input) &&
          (this.props.active.step === this.props.step.index) &&
          (this.props.active.item === resource.index)
        }
        step={this.props.step}
        resource={resource}
        remove={this.props.removeStepInput}
        setActiveStepInput={this.props.setActiveStepInput}
      />
    );
  }

  render() {
    const { connectDropTarget, isOver } = this.props;

    const counters = getRequiredResources(this.props.step, this.props.resources);
    const message = (
      <div>
        {counters.poi > 0 && counters.any <= 0 &&
          <div className="slipo-pd-step-footer pl-2">
            <i className="fa fa-exclamation mr-2"></i> <span>Drop {counters.poi} POI dataset(s) ...</span>
          </div>
        }
        {counters.linked > 0 && counters.any <= 0 &&
          <div className="slipo-pd-step-footer pl-2">
            <i className="fa fa-exclamation mr-2"></i> <span>Drop {counters.linked} POI Linked dataset(s) ...</span>
          </div>
        }
        {counters.any > 0 &&
          <div className="slipo-pd-step-footer pl-2">
            <i className="fa fa-exclamation mr-2"></i> <span>Drop {counters.any} POI or POI Linked dataset(s) ...</span>
          </div>
        }
      </div>
    );

    return connectDropTarget(
      <div className="slipo-pd-step-input-container-wrapper">
        <div
          className={classnames({
            'slipo-pd-step-input-container': true,
            'slipo-pd-step-input-container-full': (counters.poi <= 0 && counters.linked <= 0 && counters.any <= 0)
          })}
          style={{ opacity: (this.props.resources.length != 0 || isOver ? 1 : 0.2) }}>
          {this.props.resources.map((r) => this.renderResource(r))}
        </div>
        {message}
      </div>
    );
  }

}

export default StepInputContainer;
