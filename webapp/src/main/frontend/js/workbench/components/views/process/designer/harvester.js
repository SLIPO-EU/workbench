import * as React from 'react';
import PropTypes from 'prop-types';
import { DragSource } from 'react-dnd';
import classnames from 'classnames';

import {
  EnumToolboxItem,
  EnumDataSource,
  EnumHarvester,
  EnumDragSource
} from './constants';

/**
 * Drag source specification
 */
const harvesterSource = {
  /**
   * Returns a plain JavaScript object describing the data being dragged
   *
   * @param {any} props
   * @returns
   */
  beginDrag(props) {
    return {
      type: EnumToolboxItem.Harvester,
      source: EnumDataSource.HARVESTER,
      harvester: props.harvester,
      title: props.title,
      iconClass: props.iconClass,
    };
  }
};

/**
 * A presentational component for a toolbox item of type {@link EnumToolboxItem.Harvester}.
 * A harvester component can be dropped inside a TripleGeo operation component.
 *
 * @class Harvester
 * @extends {React.Component}
 */
@DragSource(EnumDragSource.Harvester, harvesterSource, (connect, monitor) => ({
  connectDragSource: connect.dragSource(),
  isDragging: monitor.isDragging()
}))
class Harvester extends React.Component {

  constructor(props) {
    super();
  }

  static propTypes = {
    // Harvester description
    title: PropTypes.string.isRequired,

    // Harvester icon
    iconClass: PropTypes.string.isRequired,

    // Harvester type
    harvester: function (props, propName, componentName) {
      for (let prop in EnumHarvester) {
        if (EnumHarvester[prop] === props[propName]) {
          return null;
        }
      }
      return new Error(`Invalid value for property [${propName}].`);
    },

    // Injected by React DnD
    connectDragSource: PropTypes.func.isRequired,
    isDragging: PropTypes.bool.isRequired
  };

  render() {
    const { isDragging, connectDragSource, id } = this.props;

    return connectDragSource(
      <div className={
        classnames({
          "slipo-pd-item": true,
          "slipo-pd-harvester": true,
          "slipo-pd-item-is-dragging": isDragging
        })
      }>
        <div className="slipo-pd-harvester-icon">
          <i className={this.props.iconClass}></i>
        </div>
        <div className="slipo-pd-item-label">
          {this.props.title}
        </div>
      </div>
    );
  }

}

export default Harvester;
