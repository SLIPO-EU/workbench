import * as React from 'react';

import {
  DropTarget,
} from 'react-dnd';

import {
  MapDraggableItem,
} from '../../../model/map-viewer';

import {
  default as DraggableItem,
} from './draggable-item';

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
    if (!component) {
      return;
    }

    const item = monitor.getItem();
    const delta = monitor.getDifferenceFromInitialOffset();
    const left = Math.round(item.left + delta.x);
    const top = Math.round(item.top + delta.y);

    component.moveBox(item.id, left, top);
  },

};

const styles = {
  marginBottom: '10px',
  position: 'fixed',
  left: '0px',
  top: '0px',
  height: '100%',
  width: '100%',
};

/**
 * Drop target for instances of {@link DraggableItem}
 */
@DropTarget([MapDraggableItem], containerTarget, (connect, monitor) => ({
  connectDropTarget: connect.dropTarget(),
}))
class ItemContainer extends React.Component {

  constructor(props) {
    super(props);

    const { children } = props;
    const items = {};

    React.Children.forEach(children, (child) => {
      const { id, left = 100, top = 100 } = child.props;

      items[id] = {
        left,
        top,
      };
    });

    this.state = {
      items,
    };
  }

  render() {
    const { children, connectDropTarget } = this.props;
    const { items } = this.state;

    return (
      connectDropTarget &&
      connectDropTarget(
        <div style={styles}>
          {React.Children.map(children, (child) => {
            if (child.type === DraggableItem) {
              const { id } = child.props;
              const { left, top, } = items[id];

              return React.cloneElement(child, {
                left,
                top,
              });
            }
            return child;
          })}
        </div>,
      )
    );
  }

  moveBox(id, left, top) {
    this.setState({
      items: {
        ...this.state.items,
        [id]: {
          left,
          top,
        },
      }
    });
  }
}

export default ItemContainer;
