import GeoJSON from 'ol/format/GeoJSON';
import WKT from 'ol/format/WKT';

export function compareGeometry(geom1, geom2) {
  if (!geom1 && !geom2) {
    return true;
  }
  if ((geom1 && !geom2) || (!geom1 && geom2)) {
    return false;
  }
  const format = new GeoJSON();

  const geom1AsText = format.writeGeometry(geom1, {
    featureProjection: 'EPSG:3857',
    dataProjection: 'EPSG:4326',
  });
  const geom2AsText = format.writeGeometry(geom2, {
    featureProjection: 'EPSG:3857',
    dataProjection: 'EPSG:4326',
  });

  return geom1AsText === geom2AsText;
}

export function geometryFromObject(geometry) {
  const format = new GeoJSON();
  return format.readGeometry(geometry, {
    featureProjection: 'EPSG:3857',
    dataProjection: 'EPSG:4326',
  });
}

export const fromWKT = (wkt) => {
  const format = new WKT();

  try {
    return format.readGeometry(wkt, {
      dataProjection: 'EPSG:4326',
      featureProjection: 'EPSG:3857'
    });
  } catch (err) {
    return null;
  }
};

export const toWKT = (feature) => {
  if (!feature || !feature.getGeometry()) {
    return null;
  }
  const format = new WKT();

  return format.writeGeometry(feature.getGeometry(), {
    dataProjection: 'EPSG:4326',
    featureProjection: 'EPSG:3857'
  });
};


