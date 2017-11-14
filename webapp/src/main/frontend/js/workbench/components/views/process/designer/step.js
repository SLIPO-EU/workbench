import * as React from 'react';
import classnames from 'classnames';

import {
  EnumTool,
  EnumToolboxItem,
} from './constants';

import StepInputContainer from './step-input-container';
import StepDataSourceContainer from './step-data-source-container';

/**
 * A presentational component for rendering a process step.
 *
 * @class Step
 * @extends {React.Component}
 */
class Step extends React.Component {

  /**
   * Resolve step icon class
   *
   * @returns
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
   * @returns
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
   * @returns
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
    return (
      <div
        className={
          classnames({
            "slipo-pd-step": true,
            "slipo-pd-step-active": this.isActive(),
            "slipo-pd-step-invalid": (!this.props.step.configuration),
          })
        }
        onClick={() => this.select()}
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
    );
  }

}

export default Step;
