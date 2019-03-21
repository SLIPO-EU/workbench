import * as React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';

import {
  LayerLegend,
} from './';

class Layer extends React.Component {

  constructor(props) {
    super(props);
  }

  static propTypes = {
    layer: PropTypes.object.isRequired,
    toggle: PropTypes.func.isRequired,
    select: PropTypes.func.isRequired,
    selected: PropTypes.bool.isRequired,
  };

  select(e) {
    e.preventDefault();
    e.nativeEvent.stopImmediatePropagation();

    this.props.select(this.props.layer.tableName);
  }

  toggle(e) {
    e.preventDefault();
    e.nativeEvent.stopImmediatePropagation();

    this.select(e);
    this.props.toggle(this.props.layer.tableName);
  }

  config(e) {
    e.preventDefault();
    e.nativeEvent.stopImmediatePropagation();

    this.select(e);
    this.props.toggleLayerConfiguration();
  }

  formatTitle(title, rowCount) {
    return rowCount ? (<span>{title} (<b><span>{rowCount}</span></b>)</span>) : title;
  }

  render() {
    const { disabled, selected, layer: { hidden, title, rowCount = null, style } } = this.props;

    return (
      <div
        className={
          classnames({
            "slipo-ev-layer-disabled": disabled,
            "slipo-ev-layer": true,
            "slipo-ev-layer-active": selected,
          })
        }
        onClick={(e) => this.select(e)}
      >
        <div className="slipo-layer-toggle">
          {hidden &&
            <i className='fa fa-square-o' onClick={(e) => this.toggle(e)}></i>
          }
          {!hidden &&
            <i className='fa fa-check-square-o' onClick={(e) => this.toggle(e)}></i>
          }
        </div>
        <div className="slipo-layer-title">
          {this.formatTitle(title, rowCount)}
        </div>
        <div className="slipo-layer-legend">
          <LayerLegend
            symbol={style.symbol}
            size={24}
            fillColor={style.fill.color}
            strokeWidth={style.stroke.width}
            strokeColor={style.stroke.color}
            opacity={style.opacity}
          />
        </div>
        <div className="slipo-layer-config">
          <i className='fa fa-cog' onClick={(e) => this.config(e)}></i>
        </div>
      </div>
    );
  }

}

export default Layer;
