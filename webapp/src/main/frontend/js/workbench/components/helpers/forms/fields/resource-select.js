import React from 'react';
import classnames from 'classnames';
import { DropTarget } from 'react-dnd';

import decorateField from './form-field';

import {
  EnumDragSource,
  EnumInputType,
} from '../../../../model/process-designer';

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
          if (typeof props.onChange === 'function') {
            props.onChange(item.data);
          }
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
        return item.data.inputType === EnumInputType.CATALOG;
      default:
        return false;
    }
  }
};


@DropTarget([EnumDragSource.Resource], containerTarget, (connect, monitor) => ({
  connectDropTarget: connect.dropTarget(),
  isOver: monitor.isOver(),
}))
class ResourceSelect extends React.Component {

  constructor(props) {
    super(props);
  }

  renderResource(resource) {
    if (!resource) {
      return (<span>No resource is selected</span>);
    }
    return (
      <div className="slipo-export-resource">
        <div className="slipo-export-resource-actions">
          <i
            className={`slipo-export-resource-action slipo-export-resource-delete slipo-export-resource-0} fa fa-trash`}
            title="Delete"
            onClick={() => { this.onRemove(); }}>
          </i>
        </div>
        <div className="slipo-export-resource-icon">
          <i className="fa fa-book"></i>
        </div>
        <p className="slipo-export-resource-label">
          {resource.name}
        </p>
      </div>
    );
  }

  onRemove() {
    if (typeof this.props.onChange === 'function') {
      this.props.onChange(null);
    }
  }

  render() {
    const { connectDropTarget, isOver, value: resource } = this.props;

    return connectDropTarget(
      <div className="slipo-export-resource-container-wrapper">
        <div
          className={classnames({
            'slipo-export-resource-container': true,
            'slipo-export-resource-container-full': (!!resource)
          })}
          style={{ opacity: (resource || isOver ? 1 : 0.2) }}>
          {this.renderResource(resource)}
        </div>
      </div>
    );
  }
}

export default decorateField(ResourceSelect);
