import * as React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';

import {
  stepFileTypeToText,
  ToolIcons,
} from '../../process/designer';

class Layer extends React.Component {

  constructor(props) {
    super(props);
  }

  static propTypes = {
    layer: PropTypes.object.isRequired,
    remove: PropTypes.func.isRequired,
    select: PropTypes.func.isRequired,
    selected: PropTypes.bool.isRequired,
  };

  select(e) {
    e.nativeEvent.stopImmediatePropagation();

    this.props.select(this.props.layer.id);
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
        <div className="slipo-ev-layer-actions">
          <i className="slipo-ev-layer-delete fa fa-trash" onClick={() => { this.props.remove(this.props.layer.id); }}></i>
        </div>
        <div className="slipo-ev-layer-icon">
          <i className={ToolIcons[this.props.layer.tool]}></i>
        </div>
        <div className="slipo-ev-layer-label">
          <span>{`${this.props.layer.step} / ${stepFileTypeToText(this.props.layer.type)}`}</span>
          <br />
          <span>{this.props.layer.file}</span>
        </div>
      </div>
    );
  }

}

export default Layer;
