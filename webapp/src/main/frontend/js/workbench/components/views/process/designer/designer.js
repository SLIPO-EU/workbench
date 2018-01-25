import * as React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';

import {
  EnumTool,
  EnumToolboxItem,
  EnumDragSource,
} from './constants';
import StepGroup from './step-group';

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
    // An array of all groups
    groups: PropTypes.arrayOf(PropTypes.object.isRequired).isRequired,
    // An array of all steps
    steps: PropTypes.arrayOf(PropTypes.object.isRequired).isRequired,
    // An array of all resources
    resources: PropTypes.arrayOf(PropTypes.object.isRequired).isRequired,

    // Action creators
    addStep: PropTypes.func.isRequired,
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
    return (
      <StepGroup
        key={group.key}
        group={group}
        steps={this.props.steps.filter((step) => { return (group.steps.indexOf(step.key) !== -1); })}
        resources={this.props.resources}
        active={this.props.active}
        addStep={this.props.addStep}
        removeStep={this.props.removeStep}
        moveStep={this.props.moveStep}
        configureStepBegin={this.props.configureStepBegin}
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
