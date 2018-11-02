import * as React from 'react';

import {
  DragSource,
} from 'react-dnd';

import {
  MapDraggableItem,
} from '../../../model/map-viewer';

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
    const { id, left, top } = props;
    return { id, left, top };
  },
};

const style = {
  position: 'absolute',
  width: 'auto',
};

const previewStyle = {
  position: 'absolute',
  right: 0,
  top: 0,
  width: '100%',
  height: '2.9rem',
  cursor: 'move',
};

/**
 * A component making map items draggable
 */
@DragSource(MapDraggableItem, dataSource, (connect, monitor) => ({
  connectDragSource: connect.dragSource(),
  connectDragPreview: connect.dragPreview(),
  isDragging: monitor.isDragging(),
}))
class DraggableItem extends React.Component {

  render() {
    const { left, top, connectDragSource, connectDragPreview, isDragging, children, } = this.props;

    if (isDragging) {
      return null;
    }

    return (
      connectDragPreview &&
      connectDragSource &&
      connectDragPreview(
        <div style={{ ...style, left, top }}>
          {children}
          {connectDragSource(<div style={previewStyle} />)}
        </div>
      )
    );
  }
}

export default DraggableItem;
