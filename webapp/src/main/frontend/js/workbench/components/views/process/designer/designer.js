import * as React from 'react';
import PropTypes from 'prop-types';

import {
  StepGroup,
} from './';

/**
 * A presentational component for rendering multiple step groups
 *
 * @class Designer
 * @extends {React.Component}
 */
class Designer extends React.Component {

  constructor(props) {
    super();
  }

  static propTypes = {
    // Workflow properties
    groups: PropTypes.arrayOf(PropTypes.object.isRequired).isRequired,
    steps: PropTypes.arrayOf(PropTypes.object.isRequired).isRequired,
    resources: PropTypes.arrayOf(PropTypes.object.isRequired).isRequired,

    // Execution properties
    execution: PropTypes.object,


    // Action creators
    addStep: PropTypes.func.isRequired,
    cloneStep: PropTypes.func.isRequired,
    removeStep: PropTypes.func.isRequired,
    moveStep: PropTypes.func.isRequired,
    moveStepInput: PropTypes.func.isRequired,
    configureStepBegin: PropTypes.func.isRequired,
    showStepExecutionDetails: PropTypes.func.isRequired,
    setStepProperty: PropTypes.func.isRequired,

    addStepInput: PropTypes.func.isRequired,
    removeStepInput: PropTypes.func.isRequired,

    addStepDataSource: PropTypes.func.isRequired,
    removeStepDataSource: PropTypes.func.isRequired,
    configureStepDataSourceBegin: PropTypes.func.isRequired,

    setActiveStep: PropTypes.func.isRequired,
    setActiveStepInput: PropTypes.func.isRequired,
    setActiveStepDataSource: PropTypes.func.isRequired,

    // Designer state
    readOnly: PropTypes.bool.isRequired,
  };

  /**
   * Renders a single {@link StepGroup}
   *
   * @param {any} group
   * @returns a {@link StepGroup} component instance
   * @memberof Designer
   */
  renderStepGroup(group) {
    const steps = this.props.steps.filter((step) => group.steps.indexOf(step.key) !== -1);
    const stepExecutions = (this.props.execution ? this.props.execution.steps.filter((e) => !!(steps.find((s) => s.key === e.key))) : []);

    return (
      <StepGroup
        appConfiguration={this.props.appConfiguration}
        key={group.key}
        group={group}
        steps={steps}
        resources={this.props.resources}
        active={this.props.active}
        addStep={this.props.addStep}
        cloneStep={this.props.cloneStep}
        removeStep={this.props.removeStep}
        moveStep={this.props.moveStep}
        moveStepInput={this.props.moveStepInput}
        configureStepBegin={this.props.configureStepBegin}
        showStepExecutionDetails={this.props.showStepExecutionDetails}
        setStepProperty={this.props.setStepProperty}
        addStepInput={this.props.addStepInput}
        removeStepInput={this.props.removeStepInput}
        addStepDataSource={this.props.addStepDataSource}
        removeStepDataSource={this.props.removeStepDataSource}
        configureStepDataSourceBegin={this.props.configureStepDataSourceBegin}
        setActiveStep={this.props.setActiveStep}
        setActiveStepInput={this.props.setActiveStepInput}
        setActiveStepDataSource={this.props.setActiveStepDataSource}
        readOnly={this.props.readOnly}
        stepExecutions={stepExecutions}
        selectOutputPart={this.props.selectOutputPart}
      />
    );
  }

  render() {
    const groups = this.props.groups.map((g, index, array) => {
      if ((this.props.readOnly) && ((array.length - 1) === index)) {
        return null;
      }
      return this.renderStepGroup(g);
    });

    return (
      <div className="slipo-pd-process">
        {groups}
      </div>
    );
  }

}

export default Designer;
