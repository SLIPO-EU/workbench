import GeoJSON from 'ol/format/GeoJSON';

import actions from './api/fetch-actions';

import {
  readProcessResponse,
} from './process';

import {
  Attributes,
} from '../model/map-viewer';

/**
 * Loads data for a resource and the process execution instance that
 * generated the specified resource version
 *
 * @export
 * @param {*} id - the resource id
 * @param {*} version - the resource revision. Process execution data refer to
 * the specific revision
 * @param {*} token the CSRF token
 * @returns the resource, its revisions and the process definition and execution
 * data of the process execution instance that generated the specific resource
 * version
 */
export function fetchResourceMapData(id, version, token) {
  return actions
    .get(`/action/map/resource/${id}/${version}`, token)
    .then((result) => {
      return {
        ...result,
        process: readProcessResponse(result.process),
      };
    });
}

/**
 * Loads data for a process execution instance
 *
 * @export
 * @param {*} id - the id of the parent process
 * @param {*} version - the revision of the specific process
 * @param {*} execution - the id of the process execution instance
 * @param {*} token - the CSRF token
 * @returns the process definition and execution data
 */
export function fetchExecutionMapData(id, version, execution, token) {
  return actions
    .get(`/action/map/process/${id}/${version}/execution/${execution}`, token)
    .then((result) => {
      return {
        ...result,
        process: readProcessResponse(result.process),
      };
    });
}

export function setResourceRevisionStyle(id, version, style, token) {
  return actions
    .post(`/action/map/style/resource/${id}/${version}`, token, style);
}

export function setFileStyle(id, style, token) {
  return actions
    .post(`/action/map/style/file/${id}`, token, style);
}

export function updateFeature(table, id, properties, feature, token) {
  // Feature is an OpenLayers object and must be converted to GeoJSON
  let geometry = null;
  if (feature) {
    const format = new GeoJSON();
    geometry = format.writeGeometryObject(feature.getGeometry(), {
      featureProjection: 'EPSG:3857',
      dataProjection: 'EPSG:4326',
    });
  }

  const data = {
    // Filter out any custom/helper properties
    properties: Attributes.filter(a => a.editable).reduce((result, attr) => {
      result[attr.key] = properties[attr.key];
      return result;
    }, {}),
    geometry,
  };

  return actions
    .post(`/action/map/feature/${table}/${id}`, token, data);
}
