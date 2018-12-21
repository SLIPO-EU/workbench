import * as React from 'react';

import {
  EnumSymbol,
} from '../../../model/map-viewer';

class LayerLegend extends React.Component {

  constructor(props) {
    super(props);
  }

  render() {
    const {
      symbol = EnumSymbol.Circle,
      height = 30,
      width = 30,
      fillColor = '#EB9694',
      strokeColor = '#B80000',
      strokeWidth = 2,
      size = 30,
      opacity = 100
    } = this.props;
    const effectSize = Math.max(strokeWidth, size - strokeWidth * 2);

    switch (symbol) {
      case 'polygon': {
        const offset = (width - size) / 2;
        const step = size / 4;

        return (
          <svg height={height} width={width}>
            <polygon
              points={`
                ${offset + step},${offset} ${offset + 3 * step},${offset}
                ${offset + 4 * step},${offset + step} ${offset + 4 * step},${offset + 3 * step}
                ${offset + 3 * step},${offset + 4 * step} ${offset + step},${offset + 4 * step}
                ${offset},${offset + 3 * step} ${offset},${offset + step}
              `}
              style={{ fill: fillColor, stroke: strokeColor, strokeWidth: strokeWidth, fillOpacity: opacity / 100.0 }} />
          </svg>
        );
      }
      case EnumSymbol.Circle:
        return (
          <svg height={height} width={width}>
            <circle
              cx={height / 2}
              cy={width / 2}
              r={Math.min(width, size) / 2 - strokeWidth / 2}
              style={{ fill: fillColor, stroke: strokeColor, strokeWidth: strokeWidth, fillOpacity: opacity / 100.0 }} />
          </svg>
        );
      case EnumSymbol.Triangle:
        return (
          <svg height={height} width={width}>
            <polygon
              points={`${width / 2},${(height - effectSize) / 2} ${(width + effectSize) / 2},${(height + effectSize) / 2} ${(width - effectSize) / 2},${(height + effectSize) / 2}`}
              style={{ fill: fillColor, stroke: strokeColor, strokeWidth: strokeWidth, fillOpacity: opacity / 100.0 }} />
          </svg>
        );
      case EnumSymbol.Cross:
        return (
          <svg height={height} width={width}>
            <line
              x1={width / 2} y1={(height - size) / 2} x2={width / 2} y2={(size + height) / 2}
              style={{ stroke: strokeColor, strokeWidth: strokeWidth, strokeOpacity: opacity / 100.0 }} />
            <line
              x1={(width - size) / 2} y1={height / 2} x2={(size + width) / 2} y2={height / 2}
              style={{ stroke: strokeColor, strokeWidth: strokeWidth, strokeOpacity: opacity / 100.0 }} />
          </svg>
        );
      default:
        return (
          <svg height={height} width={width}>
            <rect
              x={(width - effectSize) / 2}
              y={(width - effectSize) / 2}
              height={effectSize}
              width={effectSize}
              style={{ fill: fillColor, stroke: strokeColor, strokeWidth: strokeWidth, fillOpacity: opacity / 100.0 }} />
          </svg>
        );
    }
  }

}

export default LayerLegend;
