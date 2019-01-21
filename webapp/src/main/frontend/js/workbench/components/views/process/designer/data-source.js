import * as React from 'react';
import PropTypes from 'prop-types';
import { DragSource } from 'react-dnd';
import classnames from 'classnames';

import {
  EnumToolboxItem,
  EnumDataSource,
  EnumDragSource,
  EnumHarvester,
} from '../../../../model/process-designer';

/**
 * Drag source specification
 */
const dataSource = {
  /**
   * Returns a plain JavaScript object describing the data being dragged
   *
   * @param {any} props
   * @returns a plain JavaScript object
   */
  beginDrag(props) {
    return {
      type: EnumToolboxItem.DataSource,
      source: props.source,
      harvester: props.harvester,
      name: props.name,
      iconClass: props.iconClass,
    };
  }
};

/**
 * A presentational component for a toolbox item of type {@link EnumToolboxItem.DataSource}.
 * A data source component can be dropped inside a TripleGeo operation component.
 *
 * @class DataSource
 * @extends {React.Component}
 */
@DragSource(EnumDragSource.DataSource, dataSource, (connect, monitor) => ({
  connectDragSource: connect.dragSource(),
  isDragging: monitor.isDragging()
}))
class DataSource extends React.Component {

  constructor(props) {
    super();
  }

  static propTypes = {
    // Data source description
    name: PropTypes.string.isRequired,

    // Data source icon class
    iconClass: PropTypes.string.isRequired,

    // Data source type
    source: function (props, propName, componentName) {
      for (let prop in EnumDataSource) {
        if (EnumDataSource[prop] === props[propName]) {
          return null;
        }
      }
      return new Error(`Invalid value for property [${propName}].`);
    },

    // Harvester type (required only by EnumDataSource.HARVESTER data sources)
    harvester: function (props, propName, componentName) {
      if (props['source'] !== EnumDataSource.HARVESTER) {
        return null;
      }
      for (let prop in EnumHarvester) {
        if (EnumHarvester[prop] === props[propName]) {
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
    const { isDragging, connectDragSource } = this.props;

    return connectDragSource(
      <div className={
        classnames({
          "slipo-pd-item": true,
          "slipo-pd-data-source": (this.props.source !== EnumDataSource.HARVESTER),
          "slipo-pd-harvester": (this.props.source === EnumDataSource.HARVESTER),
          "slipo-pd-item-is-dragging": isDragging,
        })
      }>
        <div className={
          classnames({
            "slipo-pd-data-source-icon": (this.props.source !== EnumDataSource.HARVESTER),
            "slipo-pd-harvester-icon": (this.props.source === EnumDataSource.HARVESTER),
          })
        }>
          <i className={this.props.iconClass}></i>
        </div>
        <div className="slipo-pd-item-label">
          {this.props.name}
        </div>
      </div>
    );
  }

}

export default DataSource;
