import * as React from 'react';
import { DropTarget } from 'react-dnd';
import classnames from 'classnames';

import * as processService from '../../../../service/process';

import {
  EnumDragSource,
  EnumTool,
  EnumInputType,
  EnumResourceType,
  EnumSelection,
} from '../../../../model/process-designer';

import {
  StepInput,
} from './';

const canDropResource = (props, resource) => {
  const counters = processService.getStepInputRequirements(props.step, props.resources);

  // Do not accept owner step output
  if ((resource.inputType === EnumInputType.OUTPUT) && (resource.stepKey == props.step.key)) {
    return false;
  }
  // Catalog registration should accept only step output as input
  if ((resource.inputType !== EnumInputType.OUTPUT) && (props.step.tool === EnumTool.CATALOG)) {
    return false;
  }
  // Resource export should accept only catalog resources as input
  if ((resource.inputType !== EnumInputType.CATALOG) && (props.step.tool === EnumTool.ReverseTripleGeo)) {
    return false;
  }
  // Do not accept existing input
  if (props.resources.filter((r) => r.key === resource.key).length > 0) {
    return false;
  }

  switch (resource.resourceType) {
    case EnumResourceType.POI:
      return ((counters.poi > 0) || (counters.any > 0));
    case EnumResourceType.LINKED:
      return ((counters.linked != 0) || (counters.any > 0));
  }
};

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
      const item = monitor.getItem();

      switch (item.source) {
        case EnumDragSource.Resource:
          props.addStepInput(props.step, {
            ...item.data,
          });
          break;
      }
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
    const item = monitor.getItem();

    switch (item.source) {
      case EnumDragSource.Resource:
        return canDropResource(props, item.data);
      default:
        return false;
    }
  }
};

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
}))
class StepInputContainer extends React.Component {

  /**
   * Renders a single {@link StepInput}
   *
   * @param {any} resource
   * @param {any} index
   * @returns a {@link StepInput} component instance
   * @memberof StepInputContainer
   */
  renderResource(resource, index) {
    return (
      <StepInput
        key={resource.key}
        active={
          (this.props.active.type === EnumSelection.Input) &&
          (this.props.active.step === this.props.step.key) &&
          (this.props.active.item === resource.key)
        }
        order={index}
        step={this.props.step}
        resource={resource}
        remove={this.props.removeStepInput}
        moveStepInput={this.props.moveStepInput}
        setActiveStepInput={this.props.setActiveStepInput}
        readOnly={this.props.readOnly}
        selectOutputPart={this.props.selectOutputPart}
      />
    );
  }

  render() {
    const { connectDropTarget, isOver } = this.props;

    const counters = processService.getStepInputRequirements(this.props.step, this.props.resources);
    const message = (
      <div>
        {counters.poi > 0 && counters.any <= 0 &&
          <div className="slipo-pd-step-footer pl-2">
            <i className="fa fa-exclamation mr-2"></i>
            {counters.poi === 1 &&
              <span>Drop {counters.poi} POI dataset ...</span>
            }
            {counters.poi > 1 &&
              <span>Drop {counters.poi} POI datasets ...</span>
            }
          </div>
        }
        {counters.linked > 0 && counters.any <= 0 &&
          <div className="slipo-pd-step-footer pl-2">
            <i className="fa fa-exclamation mr-2"></i>
            {counters.linked === 1 &&
              <span>Drop {counters.linked} POI Linked dataset ...</span>
            }
            {counters.linked > 1 &&
              <span>Drop {counters.linked} POI Linked datasets ...</span>
            }
          </div>
        }
        {counters.any > 0 &&
          <div className="slipo-pd-step-footer pl-2">
            <i className="fa fa-exclamation mr-2"></i>
            {counters.any === 1 &&
              <span>Drop {counters.any} POI or POI Linked dataset ...</span>
            }
            {counters.any > 1 &&
              <span>Drop {counters.any} POI or POI Linked datasets ...</span>
            }
          </div>
        }
      </div>
    );

    return connectDropTarget(
      <div className="slipo-pd-step-input-container-wrapper">
        <div
          className={classnames({
            'm-1': true,
            'slipo-pd-step-input-container': true,
            'slipo-pd-step-input-container-full': (counters.poi <= 0 && counters.linked <= 0 && counters.any <= 0)
          })}
          style={{ opacity: (this.props.resources.length != 0 || isOver ? 1 : 0.2) }}>
          {this.props.resources.map((r, index) => this.renderResource(r, index))}
        </div>
        {message}
      </div>
    );
  }

}

export default StepInputContainer;
