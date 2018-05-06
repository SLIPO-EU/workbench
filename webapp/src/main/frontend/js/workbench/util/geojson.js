export function toFeatureCollection(features, crs = 'EPSG:4326') {
  return {
    type: 'FeatureCollection',
    crs: {
      type: 'name',
      properties: {
        name: crs
      }
    },
    features: features || [],
    _lastUpdate: new Date(),
  };
}
