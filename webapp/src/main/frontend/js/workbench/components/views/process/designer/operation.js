import * as React from 'react';
import PropTypes from 'prop-types';
import { DragSource } from 'react-dnd';
import classnames from 'classnames';

import {
  EnumToolboxItem,
  EnumTool,
  EnumOperation,
  EnumDragSource,
} from '../../../../model/process-designer';

/**
 * Drag source specification
 */
const operationSource = {
  /**
   * Returns a plain JavaScript object describing the data being dragged
   *
   * @param {any} props
   * @returns a plain JavaScript object
   */
  beginDrag(props) {
    return {
      type: EnumToolboxItem.Operation,
      tool: props.tool,
      operation: props.operation,
      name: props.name,
      iconClass: props.iconClass,
    };
  }
};

/**
 * A presentational component for a toolbox item of type {@link EnumToolboxItem.Operation}.
 * An operation component can be dropped inside a {@link Designer} component.
 *
 * @class Operation
 * @extends {React.Component}
 */
@DragSource(EnumDragSource.Operation, operationSource, (connect, monitor) => ({
  connectDragSource: connect.dragSource(),
  isDragging: monitor.isDragging()
}))
class Operation extends React.Component {

  constructor(props) {
    super();
  }

  static propTypes = {
    // Operation description
    name: PropTypes.string.isRequired,

    // Operation icon
    iconClass: PropTypes.string.isRequired,

    // SLIPO Toolkit component
    tool: function (props, propName, componentName) {
      for (let prop in EnumTool) {
        if (EnumTool[prop] === props[propName]) {
          return null;
        }
      }
      return new Error(`Invalid value for property [${propName}].`);
    },

    // Data integration operation
    operation: function (props, propName, componentName) {
      for (let prop in EnumOperation) {
        if (EnumOperation[prop] === props[propName]) {
          return null;
        }
      }
      return new Error(`Invalid value for property [${propName}].`);
    },

    // Injected by React DnD
    connectDragSource: PropTypes.func,
    isDragging: PropTypes.bool,
  };

  render() {
    const { isDragging, connectDragSource, id } = this.props;

    return connectDragSource(
      <div className={
        classnames({
          "slipo-pd-item": true,
          "slipo-pd-operation": (this.props.tool !== EnumTool.CATALOG),
          "slipo-pd-operation-catalog": (this.props.tool === EnumTool.CATALOG),
          "slipo-pd-item-is-dragging": isDragging
        })
      }>
        <div className="slipo-pd-operation-icon">
          <i className={this.props.iconClass}></i>
        </div>
        <div className="slipo-pd-item-label">
          {this.props.name}
        </div>
      </div>
    );
  }

}

export default Operation;
