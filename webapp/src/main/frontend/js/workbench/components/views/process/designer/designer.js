import * as React from 'react';
import PropTypes from 'prop-types';
import { DropTarget } from 'react-dnd';
import classnames from 'classnames';

import {
  EnumToolboxItem,
  EnumDragSource,
} from './constants';
import Step from './step';

/**
 * Drop target specification
 */
const designerTarget = {
  /**
   * Called when a compatible item is dropped on the target
   *
   * @param {any} props
   * @param {any} monitor
   * @param {any} component
   */
  drop(props, monitor, component) {
    if (!monitor.didDrop()) {
      props.addStep({
        ...monitor.getItem(),
      });
    }
  },

  /**
   * Specify whether the drop target is able to accept the item
   *
   * @param {any} props
   * @param {any} monitor
   * @returns
   */
  canDrop(props, monitor) {
    switch (monitor.getItem().type) {
      case EnumDragSource.DataSource:
      case EnumDragSource.Harvester:
      case EnumDragSource.Operation:
        return true;
      default:
        return false;
    }
  }
};

/**
 * A presentational component which acts as a drop target for {@link EnumToolboxItem}
 * items. The component is used for designing a POI data integration process.
 *
 * @class Designer
 * @extends {React.Component}
 */
@DropTarget([EnumDragSource.Operation], designerTarget, (connect, monitor) => ({
  connectDropTarget: connect.dropTarget(),
  isOver: monitor.isOver(),
  canDrop: monitor.canDrop(),
}))
class Designer extends React.Component {

  static propTypes = {
    // An array of existing steps
    steps: PropTypes.arrayOf(PropTypes.object.isRequired).isRequired,

    // Action creators
    addStep: PropTypes.func.isRequired,
    removeStep: PropTypes.func.isRequired,
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
    connectDropTarget: PropTypes.func.isRequired,
    isOver: PropTypes.bool.isRequired,
    canDrop: PropTypes.bool.isRequired,
  };

  /**
   * Renders a single {@link Step}
   *
   * @param {any} step
   * @returns
   * @memberof Designer
   */
  renderStep(step) {
    return (
      <Step
        key={step.index}
        active={this.props.active}
        step={step}
        removeStep={this.props.removeStep}
        configureStepBegin={this.props.configureStepBegin}
        addStepInput={this.props.addStepInput}
        removeStepInput={this.props.removeStepInput}
        addStepDataSource={this.props.addStepDataSource}
        removeStepDataSource={this.props.removeStepDataSource}
        configureStepDataSourceBegin={this.props.configureStepDataSourceBegin}
        setActiveStep={this.props.setActiveStep}
        setActiveStepInput={this.props.setActiveStepInput}
        setActiveStepDataSource={this.props.setActiveStepDataSource}
      />
    );
  }

  render() {
    const { connectDropTarget, isOver, canDrop } = this.props;

    return connectDropTarget(
      <div className={
        classnames({
          'slipo-pd-process': true,
          'slipo-pd-process-can-drop': canDrop,
        })
      }>
        {this.props.steps.length == 0 &&
          <div className="slipo-pd-process-label">
            <i className="fa fa-paint-brush mr-2"></i> Drop a SLIPO Toolkit component ...
          </div>
        }
        {this.props.steps.map((s) => this.renderStep(s))}
      </div>
    );
  }

}

export default Designer;
