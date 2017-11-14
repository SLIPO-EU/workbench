import * as React from 'react';
import { DropTarget } from 'react-dnd';
import classnames from 'classnames';

import {
  EnumToolboxItem,
  EnumDragSource,
  EnumTool,
  EnumProcessInput,
  EnumResourceType,
} from './constants';
import { ToolConfiguration } from './tool-config';
import StepDataSource from './step-data-source';

/**
 * Returns plain JavaScript object with required input counters
 *
 * @param {any} step
 * @returns
 */
function getRequiredDataSources(step) {
  let { source } = ToolConfiguration[step.tool];

  return {
    source: source - step.dataSources.length,
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
      props.addStepDataSource(props.step, {
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
    const dataSource = monitor.getItem();
    const counters = getRequiredDataSources(props.step);

    if ((dataSource.type != EnumToolboxItem.DataSource) && (dataSource.type != EnumToolboxItem.Harvester)) {
      return false;
    }
    return (counters.source > 0);
  }
};

/**
 * A presentational component which acts as a drop target for data source and
 * harvester items inside a process step.
 *
 * @class StepDataSourceContainer
 * @extends {React.Component}
 */
@DropTarget([EnumDragSource.Harvester, EnumDragSource.DataSource], containerTarget, (connect, monitor) => ({
  connectDropTarget: connect.dropTarget(),
  isOver: monitor.isOver(),
  canDrop: monitor.canDrop(),
}))
class StepDataSourceContainer extends React.Component {

  /**
   * Renders a single {@link StepDataSource}
   *
   * @param {any} dataSource
   * @returns
   * @memberof StepDataSourceContainer
   */
  renderDataSource(dataSource) {
    return (
      <StepDataSource
        key={dataSource.index}
        active={this.props.active.step == this.props.step.index && this.props.active.stepDataSource == dataSource.index}
        step={this.props.step}
        dataSource={dataSource}
        removeStepDataSource={this.props.removeStepDataSource}
        configureStepDataSourceBegin={this.props.configureStepDataSourceBegin}
        setActiveStepDataSource={this.props.setActiveStepDataSource}
      />
    );
  }

  render() {
    const { connectDropTarget, isOver, canDrop } = this.props;

    const counters = getRequiredDataSources(this.props.step);
    const message = (
      <div>
        {counters.source > 0 &&
          <div className="slipo-pd-step-footer pl-2">
            <i className="fa fa-exclamation mr-2"></i> <span>Drop {counters.source} data source(s) or harvester(s) ...</span>
          </div>
        }
      </div>
    );

    return connectDropTarget(
      <div className="slipo-pd-step-input-container-wrapper">
        <div
          className={classnames({
            'slipo-pd-step-input-container': true,
            'slipo-pd-step-input-container-full': (counters.source == 0)
          })}
          style={{ opacity: (this.props.step.dataSources.length != 0 || isOver ? 1 : 0.2) }}>
          {this.props.step.dataSources.map((s) => this.renderDataSource(s))}
        </div>
        {message}
      </div>
    );
  }

}

export default StepDataSourceContainer;
