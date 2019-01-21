import * as React from 'react';
import PropTypes from 'prop-types';

import ReactSelect from 'react-select';

import {
  Button, FormGroup, Input, Label, Modal, ModalHeader, ModalBody, ModalFooter,
} from 'reactstrap';

import {
  Colors,
} from '../../../model/constants';

import {
  EnumSymbol,
  Symbols,
} from '../../../model/map-viewer';

import {
  LayerColor,
  LayerLegend,
} from './';

const selectStyle = {
  control: (base) => {
    return {
      ...base,
      borderRadius: '0px',
      borderColor: '#cccccc',
      ':focus': {
        borderColor: '#8ad4ee',
      },
      boxShadow: 'none',
    };
  },
  container: (base) => ({
    ...base,
    zIndex: '1002',
  }),
  menu: (base) => ({
    ...base,
    borderRadius: '0px',
    boxShadow: 'none',
    marginTop: '-1px',
    border: '1px solid #cccccc',
  }),
};

class LayerConfig extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      symbol: EnumSymbol.Square,
      fill: {
        color: '#EB9694',
      },
      stroke: {
        color: '#B80000',
        width: 2,
      },
      size: 15,
      opacity: 100,
    };
  }

  static propTypes = {
    layer: PropTypes.object,
  }

  componentWillReceiveProps(next) {
    const { layer } = next;

    if (layer) {
      const { style: { symbol, fill, stroke, size, opacity } } = layer;

      this.setState({
        symbol,
        fill: {
          ...fill
        },
        stroke: {
          ...stroke
        },
        size,
        opacity,
      });
    }
  }

  toggle(apply) {
    if (apply) {
      const { symbol, stroke, fill, size, opacity } = this.state;
      const { layer } = this.props;

      this.props.setLayerStyle(layer.tableName, {
        symbol,
        stroke: {
          ...stroke,
        },
        fill: {
          ...fill,
        },
        size,
        opacity,
      });
    }

    this.props.toggle();
  }

  render() {
    const { layer } = this.props;

    if (!layer) {
      return null;
    }

    const { symbol, stroke: { color: strokeColor, width }, fill: { color: fillColor }, size, opacity } = this.state;

    return (
      <Modal
        centered={true}
        isOpen={this.props.visible}
        toggle={() => this.toggle(false)}
        className={this.props.className}
      >
        <ModalHeader toggle={() => this.toggle(false)}>{layer.title}</ModalHeader>
        <ModalBody>
          <div className="layer-config-container">
            <div className="layer-config-options">
              <FormGroup>
                <Label for="strokeColor">Symbol</Label>
                <ReactSelect
                  name="symbol"
                  id="symbol"
                  value={Symbols.find(opt => opt.value === symbol)}
                  onChange={(option) => this.setState({ symbol: option.value, })}
                  options={Symbols}
                  styles={selectStyle}
                />
              </FormGroup>
              <FormGroup>
                <Label for="strokeColor">Border Color</Label>
                <LayerColor
                  color={strokeColor}
                  colors={Colors}
                  onColorChange={(e) => this.setState({ stroke: { color: e.hex, width } })}
                />
              </FormGroup>
              <FormGroup>
                <Label for="strokeColor">Fill Color</Label>
                <LayerColor
                  color={fillColor}
                  colors={Colors}
                  onColorChange={(e) => this.setState({ fill: { color: e.hex } })}
                />
              </FormGroup>
            </div>
            <div className="layer-config-options">
              <FormGroup>
                <Label for="size">Size {size}px</Label>
                <Input
                  type="range"
                  min={10}
                  max={30}
                  step={1}
                  name="size"
                  id="size"
                  value={size || 15}
                  onChange={e => this.setState({ size: Number(e.target.value) })}
                />
              </FormGroup>
              <FormGroup>
                <Label for="size">Border Width {width}px</Label>
                <Input
                  type="range"
                  min={1}
                  max={5}
                  step={1}
                  name="width"
                  id="width"
                  value={width || 2}
                  onChange={e => this.setState({ stroke: { color: strokeColor, width: Number(e.target.value) } })}
                />
              </FormGroup>
              <FormGroup>
                <Label for="size">Opacity {opacity}%</Label>
                <Input
                  type="range"
                  min={0}
                  max={100}
                  step={10}
                  name="opacity"
                  id="opacity"
                  value={opacity || 100}
                  onChange={e => this.setState({ opacity: Number(e.target.value) })}
                />
              </FormGroup>
            </div>
          </div>
        </ModalBody>
        <ModalFooter style={{ justifyContent: 'center' }}>
          <LayerLegend
            height={35}
            width={35}
            symbol={symbol}
            size={size}
            fillColor={fillColor}
            strokeWidth={width}
            strokeColor={strokeColor}
            opacity={opacity}
          />
        </ModalFooter>
        <ModalFooter>
          <Button color="primary" onClick={() => this.toggle(true)}>Apply</Button>
        </ModalFooter>
      </Modal>
    );
  }

}

export default LayerConfig;
