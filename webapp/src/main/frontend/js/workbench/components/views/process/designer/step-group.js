import * as React from 'react';
import PropTypes from 'prop-types';
import { DropTarget } from 'react-dnd';
import classnames from 'classnames';

import {
  EnumTool,
  EnumDragSource,
} from '../../../../model/process-designer';

import {
  Step,
} from './';

/**
 * Drop target specification
 */
const groupTarget = {
  /**
   * Specify whether the drop target is able to accept the item
   *
   * @param {any} props
   * @param {any} monitor
   * @returns true if the item can be accepted
   */
  canDrop(props, monitor) {
    const item = monitor.getItem();
    switch (item.type) {
      case EnumDragSource.DataSource:
        return true;
      case EnumDragSource.Operation:
        switch (item.tool) {
          case EnumTool.TripleGeo:
            return (props.group.key === 0);
          default:
            return (props.group.key !== 0);
        }
      default:
        return false;
    }
  },

  /**
   * Called when a compatible item is dropped on the target
   *
   * @param {any} props
   * @param {any} monitor
   * @param {any} component
   */
  drop(props, monitor, component) {
    if (!monitor.didDrop()) {
      const group = {
        ...props.group,
      };
      const step = {
        ...monitor.getItem(),
      };
      props.addStep(group, step, props.appConfiguration);
    }
  },

};

/**
 * A presentational component which acts as a drop target for {@link EnumToolboxItem}
 * items. The component is used for designing a POI data integration workflow.
 *
 * @class StepGroup
 * @extends {React.Component}
 */
@DropTarget([EnumDragSource.Operation], groupTarget, (connect, monitor) => ({
  connectDropTarget: connect.dropTarget(),
  isOver: monitor.isOver(),
  canDrop: monitor.canDrop(),
}))
class StepGroup extends React.Component {

  constructor(props) {
    super();
  }

  static propTypes = {
    // Group object
    group: PropTypes.object.isRequired,
    // An array of all steps in this group
    steps: PropTypes.arrayOf(PropTypes.object.isRequired).isRequired,
    // An array of all resources
    resources: PropTypes.arrayOf(PropTypes.object.isRequired).isRequired,
    // Current designer selection
    active: PropTypes.object.isRequired,

    // Step actions
    addStep: PropTypes.func.isRequired,
    cloneStep: PropTypes.func.isRequired,
    removeStep: PropTypes.func.isRequired,
    moveStep: PropTypes.func.isRequired,
    configureStepBegin: PropTypes.func.isRequired,
    showStepExecutionDetails: PropTypes.func.isRequired,
    setStepProperty: PropTypes.func.isRequired,

    // Step input actions
    addStepInput: PropTypes.func.isRequired,
    removeStepInput: PropTypes.func.isRequired,

    // Step data source actions
    addStepDataSource: PropTypes.func.isRequired,
    removeStepDataSource: PropTypes.func.isRequired,
    configureStepDataSourceBegin: PropTypes.func.isRequired,

    // Selection actions
    setActiveStep: PropTypes.func.isRequired,
    setActiveStepInput: PropTypes.func.isRequired,
    setActiveStepDataSource: PropTypes.func.isRequired,

    // Step execution data
    stepExecutions: PropTypes.arrayOf(PropTypes.object.isRequired),

    // Top-level designer properties
    readOnly: PropTypes.bool.isRequired,
    appConfiguration: PropTypes.object.isRequired,

    // Injected by React DnD
    connectDropTarget: PropTypes.func.isRequired,
    isOver: PropTypes.bool.isRequired,
    canDrop: PropTypes.bool.isRequired,

  };

  /**
   * Renders a single {@link Step}
   *
   * @param {any} step
   * @returns a {@link Step} component instance
   * @memberof Designer
   */
  renderStep(step) {
    const resources = this.props.resources.filter((r) => !!step.input.find((i) => i.inputKey === r.key));
    const stepExecution = this.props.stepExecutions.find((e) => e.key === step.key) || null;

    return (
      <Step
        key={step.key}
        active={this.props.active}
        step={step}
        resources={resources}
        removeStep={this.props.removeStep}
        moveStep={this.props.moveStep}
        configureStepBegin={this.props.configureStepBegin}
        showStepExecutionDetails={this.props.showStepExecutionDetails}
        setStepProperty={this.props.setStepProperty}
        addStepInput={this.props.addStepInput}
        cloneStep={this.props.cloneStep}
        removeStepInput={this.props.removeStepInput}
        addStepDataSource={this.props.addStepDataSource}
        removeStepDataSource={this.props.removeStepDataSource}
        configureStepDataSourceBegin={this.props.configureStepDataSourceBegin}
        setActiveStep={this.props.setActiveStep}
        setActiveStepInput={this.props.setActiveStepInput}
        setActiveStepDataSource={this.props.setActiveStepDataSource}
        readOnly={this.props.readOnly}
        stepExecution={stepExecution}
        selectOutputPart={this.props.selectOutputPart}
      />
    );
  }

  render() {
    const { connectDropTarget, isOver, canDrop } = this.props;

    return connectDropTarget(
      <div className="slipo-pd-step-group">
        <div className="slipo-pd-step-group-header">
          {this.props.group.key === 0 &&
            <span>Data Transform </span>
          }
          {this.props.group.key !== 0 &&
            <span>Group {this.props.group.key}</span>
          }
        </div>
        <div className={
          classnames({
            'slipo-pd-step-group-content': true,
            'slipo-pd-step-group-can-drop': canDrop,
          })
        }>
          {this.props.group.key === 0 && this.props.steps.length == 0 &&
            <div className="slipo-pd-step-group-label">
              <i className="fa fa-paint-brush mr-2"></i> Drop a TripleGeo operation ...
            </div>
          }
          {this.props.group.key !== 0 && this.props.steps.length == 0 &&
            <div className="slipo-pd-step-group-label">
              <i className="fa fa-paint-brush mr-2"></i> Drop a SLIPO Toolkit component ...
            </div>
          }
          {this.props.steps.map((s) => this.renderStep(s))}
        </div>
      </div>
    );
  }

}

export default StepGroup;
