import * as React from 'react';
import PropTypes from 'prop-types';
import { DragSource } from 'react-dnd';
import { Link } from 'react-router-dom';
import classnames from 'classnames';
import {
  DynamicRoutes,
  buildPath,
} from '../../../../model/routes';
import {
  EnumDragSource,
  EnumResourceType,
  EnumInputType,
} from '../../../../model/process-designer';

/**
 * Drag source specification
 */
const resourceSource = {
  /**
   * Specify whether the dragging is currently allowed
   *
   * @param {any} props
   * @param {any} monitor
   */
  canDrag(props, monitor) {
    return true;
  },

  /**
   * Returns a plain JavaScript object describing the data being dragged
   *
   * @param {any} props
   * @returns a plain JavaScript object
   */
  beginDrag(props) {
    return {
      source: EnumDragSource.Resource,
      data: {
        ...props.resource,
      },
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

  select(e) {
    e.nativeEvent.stopImmediatePropagation();

    this.props.setActiveResource(this.props.resource);
  }

  static propTypes = {
    // Resource metadata
    resource: PropTypes.shape({
      // Unique key
      key: PropTypes.number.isRequired,
      // Title
      name: PropTypes.string.isRequired,
      // Icon
      iconClass: PropTypes.string.isRequired,
      // Process input resource type
      inputType: function (props, propName) {
        for (let prop in EnumInputType) {
          if (EnumInputType[prop] === props[propName]) {
            return null;
          }
        }
        return new Error(`Invalid value for property [${propName}].`);
      },
      // Resource type
      resourceType: function (props, propName) {
        for (let prop in EnumResourceType) {
          if (EnumResourceType[prop] === props[propName]) {
            return null;
          }
        }
        return new Error(`Invalid value for property [${propName}].`);
      },
    }).isRequired,

    readOnly: PropTypes.bool,
    setActiveResource: PropTypes.func.isRequired,

    // Injected by React DnD
    connectDragSource: PropTypes.func.isRequired,
    isDragging: PropTypes.bool.isRequired
  };

  static defaultProps = {
    readOnly: false,
  }

  render() {
    const { isDragging, connectDragSource, id } = this.props;

    return connectDragSource(
      <div
        className={
          classnames({
            "slipo-pd-resource": true,
            "slipo-pd-resource-active": this.props.active,
            "slipo-pd-resource-is-dragging": isDragging
          })
        }
        onClick={(e) => this.select(e)}
      >
        <div className="slipo-pd-resource-actions">
          {this.props.resource.inputType != EnumInputType.OUTPUT &&
            <Link to={buildPath(DynamicRoutes.ResourceViewer, [this.props.resource.id, this.props.resource.version])}>
              <i className="slipo-pd-resource-view fa fa-search" title="View resource"></i>
            </Link>
          }
          {this.props.resource.inputType != EnumInputType.OUTPUT && !this.props.readOnly &&
            <i className="slipo-pd-resource-delete fa fa-trash" title="Delete" onClick={() => { this.remove(); }}></i>
          }
        </div>
        <div className="slipo-pd-resource-icon">
          <i className={this.props.resource.iconClass}></i>
        </div>
        <div className="slipo-pd-resource-label">
          {this.props.resource.name}
        </div>
      </div>
    );
  }

}

export default ProcessInput;
