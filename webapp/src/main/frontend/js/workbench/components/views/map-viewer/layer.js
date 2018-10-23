import * as React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';

import {
  Colors,
} from '../../../model/constants';

import {
  LayerColor,
} from './';

class Layer extends React.Component {

  constructor(props) {
    super(props);

    this.onColorChange = this.onColorChange.bind(this);
  }

  static propTypes = {
    layer: PropTypes.object.isRequired,
    toggle: PropTypes.func.isRequired,
    select: PropTypes.func.isRequired,
    selected: PropTypes.bool.isRequired,
    setLayerColor: PropTypes.func.isRequired,
  };

  select(e) {
    e.nativeEvent.stopImmediatePropagation();

    this.props.select(this.props.layer.tableName);
  }

  toggle(e) {
    e.nativeEvent.stopImmediatePropagation();

    this.props.toggle(this.props.layer.tableName);
  }

  onColorChange(color) {
    this.props.setLayerColor(this.props.layer.tableName, color.hex);
  }

  render() {
    return (
      <div
        className={
          classnames({
            "slipo-ev-layer": true,
            "slipo-ev-layer-active": this.props.selected,
          })
        }
        onClick={(e) => this.select(e)}
      >
        <div style={{ marginTop: '6px', float: 'left' }}>
          {this.props.layer.hidden &&
            <i className='fa fa-square-o slipo-layer-toggle' onClick={(e) => this.toggle(e)}></i>
          }
          {!this.props.layer.hidden &&
            <i className='fa fa-check-square-o slipo-layer-toggle' onClick={(e) => this.toggle(e)}></i>
          }
        </div>
        <div style={{ marginTop: '8px', float: 'left' }}>
          {this.props.layer.title}
        </div>
        <div style={{ float: 'right', padding: '8px' }}>
          <LayerColor
            color={this.props.layer.color}
            colors={Colors}
            onColorChange={this.onColorChange}
          />
        </div>
      </div>
    );
  }

}

export default Layer;
