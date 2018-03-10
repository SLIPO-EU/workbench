import * as React from 'react';
import { DropTarget } from 'react-dnd';
import classnames from 'classnames';

import * as processService from '../../../../service/process';

import {
  EnumToolboxItem,
  EnumDragSource,
  EnumInputType,
  EnumResourceType,
  EnumSelection,
} from '../../../../model/process-designer';

import {
  StepDataSource,
} from './';

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
   * @returns true if the item is accepted
   */
  canDrop(props, monitor) {
    const dataSource = monitor.getItem();
    const counters = processService.getStepDataSourceRequirements(props.step);

    if (dataSource.type != EnumToolboxItem.DataSource) {
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
@DropTarget([EnumDragSource.DataSource], containerTarget, (connect, monitor) => ({
  connectDropTarget: connect.dropTarget(),
  isOver: monitor.isOver(),
  canDrop: monitor.canDrop(),
}))
class StepDataSourceContainer extends React.Component {

  /**
   * Renders a single {@link StepDataSource}
   *
   * @param {any} dataSource
   * @returns a {@link StepDataSourceContainer} component instance
   * @memberof StepDataSourceContainer
   */
  renderDataSource(dataSource) {
    return (
      <StepDataSource
        key={dataSource.key}
        active={
          (this.props.active.type === EnumSelection.DataSource) &&
          (this.props.active.step === this.props.step.key) &&
          (this.props.active.item === dataSource.key)
        }
        step={this.props.step}
        dataSource={dataSource}
        removeStepDataSource={this.props.removeStepDataSource}
        configureStepDataSourceBegin={this.props.configureStepDataSourceBegin}
        setActiveStepDataSource={this.props.setActiveStepDataSource}
        readOnly={this.props.readOnly}
      />
    );
  }

  render() {
    const { connectDropTarget, isOver, canDrop } = this.props;

    const counters = processService.getStepDataSourceRequirements(this.props.step);
    const message = (
      <div>
        {counters.source > 0 &&
          <div className="slipo-pd-step-footer pl-2">
            <i className="fa fa-exclamation mr-2"></i>
            {counters.source === 1 &&
              <span>Drop {counters.source} data source or harvester ...</span>
            }
            {counters.source > 1 &&
              <span>Drop {counters.source} data sources or harvesters ...</span>
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
