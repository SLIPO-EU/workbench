import Collection from 'ol/Collection';
import Style from 'ol/style/Style';
import Text from 'ol/style/Text';
import Circle from 'ol/style/Circle';
import Stroke from 'ol/style/Stroke';
import Fill from 'ol/style/Fill';
import RegularShape from 'ol/style/RegularShape';
import Feature from 'ol/Feature';

import { createEmpty, extend, isEmpty } from 'ol/extent';

import {
  EnumSymbol,
} from '../../../../model/map-viewer';

const opacity = ['00', '1A', '33', '4D', '66', '80', '99', 'B3', 'CC', 'E6', 'FF'];

const opacityToHex = (value = 100) => {
  const index = Math.min(100, Math.max(0, value / 10));
  return opacity[index];
};

export const createStyleForSymbol = (symbol, strokeColor, strokeWidth, fillColor, size, opacity = 100, scale = 1.0) => {
  opacity = opacityToHex(opacity);

  const stroke = new Stroke({
    color: strokeColor,
    width: strokeWidth,
  });

  const opacityStroke = new Stroke({
    color: strokeColor + opacity,
    width: strokeWidth,
    lineCap: 'square',
    lineJoin: 'bevel',
  });

  const fill = new Fill({
    color: fillColor + opacity,
  });

  switch (symbol) {
    case EnumSymbol.Square:
      return new RegularShape({
        fill,
        stroke,
        points: 4,
        radius: scale * size / 2,
        angle: Math.PI / 4,
      });
    case EnumSymbol.Triangle:
      return new RegularShape({
        fill,
        stroke,
        points: 3,
        radius: scale * size / 2,
        angle: 0,
      });
    case EnumSymbol.Polygon:
      return new RegularShape({
        fill,
        stroke,
        points: 8,
        radius: scale * size / 2,
        angle: Math.PI / 8,
      });
    case EnumSymbol.Cross:
      return new RegularShape({
        fill,
        stroke: opacityStroke,
        points: 4,
        radius: scale * size / 2,
        radius2: 0,
        angle: 0
      });
    default:
      return new Circle({
        radius: scale * size / 2,
        fill,
        stroke,
      });
  }
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

  const fill = new Fill({
    color: layerStyle.fill.color + opacity,
  });

  const image = createStyleForSymbol(
    layerStyle.symbol,
    layerStyle.stroke.color,
    layerStyle.stroke.width,
    layerStyle.fill.color,
    layerStyle.size,
    layerStyle.opacity,
    scale
  );

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

const toFeatureArray = (features) => {
  if (features instanceof Feature) {
    return [features];
  }
  if (features instanceof Collection) {
    return features.getArray();
  }
  if (Array.isArray(features)) {
    return features;
  }

  return null;
};

export const mergeExtent = (features) => {
  const array = toFeatureArray(features);

  if (array) {
    const extent = createEmpty();

    array.forEach((f) => {
      if (f.getGeometry()) {
        extend(extent, f.getGeometry().getExtent());
      }
    });

    return isEmpty(extent) ? null : extent;
  }
  return null;
};
