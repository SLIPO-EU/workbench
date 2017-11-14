import * as React from 'react';
import PropTypes from 'prop-types';
import { DragSource } from 'react-dnd';
import classnames from 'classnames';

import {
  EnumDragSource,
  EnumResourceType,
  EnumProcessInput,
} from './constants';

/**
 * Drag source specification
 */
const resourceSource = {
  /**
   * Returns a plain JavaScript object describing the data being dragged
   *
   * @param {any} props
   * @returns
   */
  beginDrag(props) {
    return {
      ...props.resource,
      dependencies: props.resource.dependencies.map((d) => {
        return {
          ...d,
        };
      })
    };
  }
};

/**
 * A presentational component for a process input resource.
 *
 * @class ProcessInput
 * @extends {React.Component}
 */
@DragSource(EnumDragSource.Resource, resourceSource, (connect, monitor) => ({
  connectDragSource: connect.dragSource(),
  isDragging: monitor.isDragging()
}))
class ProcessInput extends React.Component {

  constructor(props) {
    super();
  }

  remove() {
    this.props.remove(this.props.resource);
  }

  static propTypes = {
    // Resource metadata
    resource: PropTypes.shape({
      // Unique index
      index: PropTypes.number.isRequired,
      // Title
      title: PropTypes.string.isRequired,
      // Icon
      iconClass: PropTypes.string.isRequired,
      // Process input resource type
      inputType: function (props, propName, componentName) {
        for (let prop in EnumProcessInput) {
          if (EnumProcessInput[prop] === props[propName]) {
            return null;
          }
        }
        return new Error(`Invalid value for property [${propName}].`);
      },
      // Resource type
      resourceType: function (props, propName, componentName) {
        for (let prop in EnumResourceType) {
          if (EnumResourceType[prop] === props[propName]) {
            return null;
          }
        }
        return new Error(`Invalid value for property [${propName}].`);
      },
    }).isRequired,

    // Injected by React DnD
    connectDragSource: PropTypes.func.isRequired,
    isDragging: PropTypes.bool.isRequired
  };

  render() {
    const { isDragging, connectDragSource, id } = this.props;

    return connectDragSource(
      <div className={
        classnames({
          "slipo-pd-resource": true,
          "slipo-pd-resource-is-dragging": isDragging
        })
      }>
        <div className="slipo-pd-resource-actions">
          {this.props.resource.inputType != EnumProcessInput.OUTPUT &&
            <i className="slipo-pd-resource-delete fa fa-trash" onClick={() => { this.remove(); }}></i>
          }
          {this.props.resource.inputType != EnumProcessInput.OUTPUT &&
            <i className="slipo-pd-resource-view fa fa-search"></i>
          }
        </div>
        <div className="slipo-pd-resource-icon">
          <i className={this.props.resource.iconClass}></i>
        </div>
        <div className="slipo-pd-resource-label">
          {this.props.resource.title}
        </div>
      </div>
    );
  }

}

export default ProcessInput;
