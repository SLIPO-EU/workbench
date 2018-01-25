export function toFeatureCollection(features) {
  return {
    type: 'FeatureCollection',
    crs: {
      type: 'name',
      properties: {
        name: 'EPSG:4326'
      }
    },
    features: features || [],
    _lastUpdate: new Date(),
  };
}
