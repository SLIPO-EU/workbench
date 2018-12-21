import Style from 'ol/style/style';
import Text from 'ol/style/text';
import Circle from 'ol/style/circle';
import Stroke from 'ol/style/stroke';
import Fill from 'ol/style/fill';
import RegularShape from 'ol/style/regularshape';

import {
  EnumSymbol,
} from '../../../../model/map-viewer';

const opacity = ['00', '1A', '33', '4D', '66', '80', '99', 'B3', 'CC', 'E6', 'FF'];

const opacityToHex = (value = 100) => {
  const index = Math.min(100, Math.max(0, value / 10));
  return opacity[index];
};

export const createStyle = (icon, color, layerStyle, dash = false, scale = 1.0) => {
  const opacity = opacityToHex(layerStyle.opacity);

  const stroke = new Stroke({
    color: layerStyle.stroke.color,
    width: layerStyle.stroke.width,
  });

  const dashStroke = new Stroke({
    color: layerStyle.stroke.color,
    width: layerStyle.stroke.width,
    lineDash: [5],
  });

  const opacityStroke = new Stroke({
    color: layerStyle.stroke.color + opacity,
    width: layerStyle.stroke.width,
    lineCap: 'square',
    lineJoin: 'bevel',
  });

  const fill = new Fill({
    color: layerStyle.fill.color + opacity,
  });

  let image = null;
  switch (layerStyle.symbol) {
    case EnumSymbol.Square:
      image = new RegularShape({
        fill,
        stroke,
        points: 4,
        radius: scale * layerStyle.size / 2,
        angle: Math.PI / 4,
      });
      break;
    case EnumSymbol.Triangle:
      image = new RegularShape({
        fill,
        stroke,
        points: 3,
        radius: scale * layerStyle.size / 2,
        angle: 0,
      });
      break;
    case EnumSymbol.Polygon:
      image = new RegularShape({
        fill,
        stroke,
        points: 8,
        radius: scale * layerStyle.size / 2,
        angle: Math.PI / 8,
      });
      break;
    case EnumSymbol.Cross:
      image = new RegularShape({
        fill,
        stroke: opacityStroke,
        points: 4,
        radius: scale * layerStyle.size / 2,
        radius2: 0,
        angle: 0
      });
      break;
    default:
      image = new Circle({
        radius: scale * layerStyle.size / 2,
        fill,
        stroke,
      });
      break;
  }

  const style = new Style({
    fill,
    stroke: dash ? dashStroke : stroke,
  });

  return {
    'Point': (
      icon ?
        new Style({
          text: new Text({
            text: icon,
            font: 'normal 32px FontAwesome',
            fill: new Fill({
              color,
            }),
          }),
        })
        :
        new Style({
          image,
        })),
    'MultiPoint': (
      icon ?
        new Style({
          text: new Text({
            text: icon,
            font: 'normal 32px FontAwesome',
            fill: new Fill({
              color,
            }),
          }),
        })
        :
        new Style({
          image,
        })),
    'LineString': style,
    'MultiLineString': style,
    'Polygon': style,
    'MultiPolygon': style,
  };

};
