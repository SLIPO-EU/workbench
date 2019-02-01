import * as mapService from '../../../service/map';
import * as provenanceService from '../../../service/provenance';

import {
  EnumPane,
} from '../../../model/map-viewer';

import {
  geometryFromObject,
  processExecutionToLayers,
  provenanceToTable,
} from './map-viewer/util';

/*
 * Action types
 */

import {
  LOGOUT,
} from '../../user';

import {
  EnumErrorLevel,
  ServerError,
} from '../../../model/error';

import {
  FEATURE_ID,
  FEATURE_LAYER_PROPERTY,
  FEATURE_OUTPUT_KEY,
  FEATURE_URI,
} from '../../../components/helpers/map/model/constants';

const Types = {
  LOGOUT,

  RESET: 'ui/map/viewer/RESET',

  REQUEST_EXECUTION_MAP_DATA: 'ui/map/viewer/REQUEST_EXECUTION_MAP_DATA',
  RECEIVE_EXECUTION_MAP_DATA: 'ui/map/viewer/RECEIVE_EXECUTION_MAP_DATA',
  REQUEST_RESOURCE_MAP_DATA: 'ui/map/viewer/REQUEST_RESOURCE_MAP_DATA',
  RECEIVE_RESOURCE_MAP_DATA: 'ui/map/viewer/RECEIVE_RESOURCE_MAP_DATA',
  REQUEST_FEATURE_PROVENANCE: 'ui/map/viewer/REQUEST_FEATURE_PROVENANCE',
  RECEIVE_FEATURE_PROVENANCE: 'ui/map/viewer/RECEIVE_FEATURE_PROVENANCE',

  HIDE_PROVENANCE: 'ui/map/viewer/HIDE_PROVENANCE',
  BRING_PANEL_TO_FRONT: 'ui/map/viewer/BRING_PANEL_TO_FRONT',

  SELECT_LAYER: 'ui/map/viewer/SELECT_LAYER',
  TOGGLE_LAYER: 'ui/map/viewer/TOGGLE_LAYER',
  TOGGLE_LAYER_CONFIG: 'ui/map/viewer/TOGGLE_LAYER_CONFIG',
  SET_BASE_LAYER: 'ui/map/viewer/SET_BASE_LAYER',
  SET_LAYER_COLOR: 'ui/map/viewer/SET_LAYER_COLOR',
  SET_LAYER_STYLE: 'ui/map/viewer/SET_LAYER_STYLE',
  SET_SELECTED_FEATURES: 'ui/map/viewer/SET_SELECTED_FEATURES',
  CLEAR_SELECTED_FEATURES: 'ui/map/viewer/CLEAR_SELECTED_FEATURES',
  SET_CENTER: 'ui/map/viewer/SET_CENTER',
  SET_ITEM_POSITION: 'ui/map/viewer/SET_ITEM_POSITION',

  SELECT_FEATURE: 'ui/map/viewer/SELECT_FEATURE',

  FILTER_TOGGLE: 'ui/map/viewer/FILTER_TOGGLE',
  FILTER_SET: 'ui/map/viewer/FILTER_SET',

  EDIT_TOGGLE: 'ui/map/viewer/EDIT_TOGGLE',
  EDIT_COMMIT_BEGIN: 'ui/map/viewer/EDIT_COMMIT_BEGIN',
  EDIT_COMMIT_COMPLETE: 'ui/map/viewer/EDIT_COMMIT_COMPLETE',
  EDIT_CANCEL: 'ui/map/viewer/EDIT_CANCEL',
  EDIT_UPDATE_PROPERTY: 'ui/map/viewer/EDIT_UPDATE_PROPERTY',
  EDIT_VERTEX: 'ui/map/viewer/EDIT_VERTEX',

  SELECT_GEOMETRY_SNAPSHOT: 'ui/map/viewer/SELECT_GEOMETRY_SNAPSHOT',
};

/*
 * Initial state
 */

const initialEditState = {
  id: null,
  table: null,
  feature: null,
  initial: {
    properties: null,
    geometry: null,
  },
  current: {
    properties: null,
  },
  loading: false,
  active: false,
  updates: 0,
};

const initialState = {
  loading: false,
  data: {
    resource: null,
    process: null,
    execution: null,
    version: null,
  },
  config: {
    layerConfigVisible: false,
    layers: [],
    baseLayer: 'BingMaps-Road',
    selectedLayer: null,
    selectedFeatures: [],
    selectedFeature: null,
    provenance: null,
    center: null,
    zoom: null,
    draggableOrder: [EnumPane.FeatureCollection, EnumPane.FeatureProvenance],
    draggable: {
      [EnumPane.FeatureCollection]: {
        left: 220,
        top: 120,
      },
      [EnumPane.FeatureProvenance]: {
        left: 520,
        top: 120,
      },
    },
  },
  edit: initialEditState,
  search: {
    visible: false,
    filters: [],
  },
};

/*
 * Reducers
 */

const dataReducer = (state, action) => {
  switch (action.type) {
    case Types.RECEIVE_EXECUTION_MAP_DATA:
      return {
        ...state,
        // Update all values
        ...action.data,
      };

    case Types.RECEIVE_RESOURCE_MAP_DATA:
      return {
        ...state,
        // Update all values
        ...action.data,
      };

    default:
      return state;
  }
};

const configReducer = (state, action, global) => {
  switch (action.type) {
    case Types.SET_CENTER:
      return {
        ...state,
        center: action.center,
        zoom: action.zoom,
      };

    case Types.SET_ITEM_POSITION:
      return {
        ...state,
        draggable: {
          ...state.draggable,
          [action.id]: {
            left: action.left,
            top: action.top,
          },
        },
      };

    case Types.TOGGLE_LAYER:
      return {
        ...state,
        layers: state.layers.map((l) => {
          if (l.tableName === action.tableName) {
            return {
              ...l,
              hidden: !l.hidden,
            };
          }
          return l;
        }),
      };

    case Types.TOGGLE_LAYER_CONFIG:
      return {
        ...state,
        layerConfigVisible: !state.layerConfigVisible,
      };

    case Types.SELECT_LAYER:
      return {
        ...state,
        selectedLayer: state.layers.find(l => l.tableName === action.tableName) || null,
      };

    case Types.SET_BASE_LAYER:
      return {
        ...state,
        baseLayer: action.layer,
      };

    case Types.SET_LAYER_COLOR:
      return {
        ...state,
        selectedFeatures: [],
        layers: state.layers.map((l) => {
          if (l.tableName === action.tableName) {
            return {
              ...l,
              color: action.color,
            };
          }
          return l;
        }),
      };

    case Types.SET_LAYER_STYLE:
      return {
        ...state,
        selectedFeatures: [],
        layers: state.layers.map((l) => {
          if (l.tableName === action.tableName) {
            return {
              ...l,
              style: action.style,
            };
          }
          return l;
        }),
      };

    case Types.SET_SELECTED_FEATURES:
      return {
        ...state,
        selectedFeatures: action.features || [],
        draggableOrder: [...state.draggableOrder.filter(id => id != EnumPane.FeatureCollection), EnumPane.FeatureCollection],
      };

    case Types.CLEAR_SELECTED_FEATURES:
      return {
        ...state,
        selectedFeatures: [],
      };

    case Types.HIDE_PROVENANCE: {
      // Restore geometry snapshot
      const { updates, dataRows } = state.provenance;
      if (updates.length !== 0) {
        const feature = global.config.selectedFeature.feature;
        // Last row is always the geometry attribute
        const row = dataRows[dataRows.length - 1];
        // Last cell is the most recent value
        const cell = row[row.length - 1];
        const geometry = geometryFromObject(cell.value);
        // Set selected feature geometry. OpenLayers map rendering is not
        // controlled by React.
        feature.setGeometry(geometry);
      }

      return {
        ...state,
        provenance: null,
        selectedFeatures: [],
        selectedFeature: null,
      };
    }

    case Types.BRING_PANEL_TO_FRONT:
      return {
        ...state,
        draggableOrder: [...state.draggableOrder.filter(id => id != action.id), action.id],
      };

    case Types.RECEIVE_EXECUTION_MAP_DATA:
    case Types.RECEIVE_RESOURCE_MAP_DATA:
      return {
        ...state,
        layers: processExecutionToLayers(action.data.process, action.data.execution),
        selectedLayer: null,
        selectedFeatures: [],
        provenance: null,
        selectedFeature: null,
      };

    case Types.RECEIVE_FEATURE_PROVENANCE:
      return {
        ...state,
        provenance: action.provenance,
      };

    case Types.SELECT_GEOMETRY_SNAPSHOT: {
      const feature = global.config.selectedFeature.feature;
      const geometry = geometryFromObject(action.geometry);

      // Set selected feature geometry. OpenLayers map rendering is not
      // controlled by React.
      feature.setGeometry(geometry);

      return {
        ...state,
        provenance: {
          ...state.provenance,
          geometrySnapshotIndex: action.index,
        },
      };
    }

    case Types.SELECT_FEATURE:
      return {
        ...state,
        selectedFeature: {
          feature: action.feature,
          outputKey: action.outputKey,
          featureId: action.featureId,
          featureUri: action.featureUri,
        }
      };

    default:
      return state;
  }
};

const filterReducer = (state, action) => {
  switch (action.type) {
    case Types.FILTER_TOGGLE:
      return {
        ...state,
        visible: action.value != null ? action.value : !state.visible,
      };

    case Types.FILTER_SET:
      return {
        ...state,
        visible: false,
        filters: action.filters || [],
      };

    default:
      return state;
  }
};

const editReducer = (state, action, global) => {
  switch (action.type) {
    case Types.EDIT_TOGGLE: {
      const feature = action.feature;

      if (feature) {
        const { geometry, ...properties } = feature.getProperties();

        return {
          ...state,
          id: feature.getId().split('::')[1],
          table: feature.get(FEATURE_LAYER_PROPERTY),
          feature,
          initial: {
            properties: { ...properties },
            geometry: feature.getGeometry().clone(),
          },
          current: {
            properties: { ...properties },
          },
          active: true,
        };
      }
      return initialEditState;
    }

    case Types.EDIT_COMMIT_BEGIN:
      return {
        ...state,
        loading: true,
      };

    case Types.EDIT_UPDATE_PROPERTY:
      return {
        ...state,
        current: {
          properties: { ...state.current.properties, [action.key]: action.value },
        }
      };

    case Types.EDIT_VERTEX:
      return {
        ...state,
        updates: ++state.updates,
      };

    case Types.EDIT_COMMIT_COMPLETE: {
      const feature = global.config.selectedFeature.feature;

      // Update properties
      const properties = state.current.properties;
      Object.keys(properties).forEach(key => feature.set(key, properties[key]));
      // Restore style if modified by the Modify interaction
      feature.setStyle(null);

      return initialEditState;
    }

    case Types.EDIT_CANCEL: {
      const feature = global.config.selectedFeature.feature;

      // Reset selected feature geometry. Setting the geometry here
      // refreshes OpenLayers map which is not controlled by the React
      // rendering.
      feature.setGeometry(state.initial.geometry);
      // Restore style if modified by the Modify interaction
      feature.setStyle(null);

      return initialEditState;
    }

    default:
      return state;
  }
};

export default (state = initialState, action) => {
  switch (action.type) {
    case Types.LOGOUT:
    case Types.RESET:
      return initialState;

    case Types.REQUEST_EXECUTION_MAP_DATA:
    case Types.REQUEST_RESOURCE_MAP_DATA:
    case Types.REQUEST_FEATURE_PROVENANCE:
      return {
        ...state,
        loading: true,
      };

    case Types.RECEIVE_EXECUTION_MAP_DATA:
    case Types.RECEIVE_RESOURCE_MAP_DATA:
    case Types.RECEIVE_FEATURE_PROVENANCE:
      return {
        ...state,
        loading: false,
        config: configReducer(state.config, action),
        data: dataReducer(state.data, action),
      };

    case Types.SET_BASE_LAYER:
    case Types.SELECT_LAYER:
    case Types.TOGGLE_LAYER:
    case Types.TOGGLE_LAYER_CONFIG:
    case Types.SET_LAYER_COLOR:
    case Types.SET_LAYER_STYLE:
    case Types.SET_SELECTED_FEATURES:
    case Types.CLEAR_SELECTED_FEATURES:
    case Types.HIDE_PROVENANCE:
    case Types.BRING_PANEL_TO_FRONT:
    case Types.SET_CENTER:
    case Types.SET_ITEM_POSITION:
    case Types.SELECT_FEATURE:
    case Types.SELECT_GEOMETRY_SNAPSHOT:
      return {
        ...state,
        config: configReducer(state.config, action, state),
      };

    case Types.FILTER_TOGGLE:
    case Types.FILTER_SET:
      return {
        ...state,
        search: filterReducer(state.search, action),
      };

    case Types.EDIT_TOGGLE:
    case Types.EDIT_COMMIT_BEGIN:
    case Types.EDIT_UPDATE_PROPERTY:
    case Types.EDIT_VERTEX:
    case Types.EDIT_COMMIT_COMPLETE:
    case Types.EDIT_CANCEL:
      return {
        ...state,
        edit: editReducer(state.edit, action, state),
      };

    default:
      return state;
  }
};

/*
 * Thunk actions
 */

const requestExecutionMapData = () => ({
  type: Types.REQUEST_EXECUTION_MAP_DATA,
});

const receiveExecutionMapData = (data) => ({
  type: Types.RECEIVE_EXECUTION_MAP_DATA,
  data,
});

export const fetchExecutionMapData = (id, version, execution) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(requestExecutionMapData());

  return mapService.fetchExecutionMapData(id, version, execution, token)
    .then((data) => {
      dispatch(receiveExecutionMapData(data));
    });
};

const requestResourceMapData = () => ({
  type: Types.REQUEST_RESOURCE_MAP_DATA,
});

const receiveResourceMapData = (data) => ({
  type: Types.RECEIVE_RESOURCE_MAP_DATA,
  data,
});

export const fetchResourceMapData = (id, version) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(requestResourceMapData());

  return mapService.fetchResourceMapData(id, version, token)
    .then((data) => {
      dispatch(receiveResourceMapData(data));
    });
};

const requestFeatureProvenance = () => ({
  type: Types.REQUEST_FEATURE_PROVENANCE,
});

const receiveFeatureProvenance = (provenance) => ({
  type: Types.RECEIVE_FEATURE_PROVENANCE,
  provenance,
});

const selectFeature = (feature, outputKey, featureId, featureUri) => ({
  type: Types.SELECT_FEATURE,
  feature,
  outputKey,
  featureId,
  featureUri,
});

export const fetchFeatureProvenance = (processId, processVersion, executionId, feature) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();

  const outputKey = feature.get(FEATURE_OUTPUT_KEY);
  const featureId = feature.get(FEATURE_ID);
  const featureUri = feature.get(FEATURE_URI);

  dispatch(selectFeature(feature, outputKey, featureId, featureUri));
  dispatch(requestFeatureProvenance());

  return provenanceService.fetchFeatureProvenance(processId, processVersion, executionId, outputKey, featureId, featureUri, token)
    .then((data) => {
      const provenance = provenanceToTable(data);

      dispatch(receiveFeatureProvenance(provenance));
      if (provenance) {
        dispatch(bringToFront(EnumPane.FeatureProvenance));
      }

      return provenance;
    });
};

export const refreshFeatureProvenance = () => (dispatch, getState) => {
  // Get parameters from state
  const {
    meta: { csrfToken: token },
    ui: { views: { map: {
      config: { selectedFeature },
      data: { resource, execution },
    } } },
  } = getState();

  // A selected feature must already exists
  if (!selectedFeature) {
    return Promise.reject(new ServerError([{
      code: -1,
      description: 'No feature is selected',
      level: EnumErrorLevel.ERROR,
    }]));
  }

  // A map may be rendered either for a resource or a single execution
  const processId = resource ? resource.execution.id : execution.process.id;
  const processVersion = resource ? resource.execution.version : execution.process.version;
  const executionId = resource ? resource.execution.execution : execution.id;

  const { outputKey, featureId, featureUri } = selectedFeature;

  dispatch(requestFeatureProvenance());

  return provenanceService.fetchFeatureProvenance(processId, processVersion, executionId, outputKey, featureId, featureUri, token)
    .then((data) => {
      const provenance = provenanceToTable(data);

      dispatch(receiveFeatureProvenance(provenance));
      if (provenance) {
        dispatch(bringToFront(EnumPane.FeatureProvenance));
      }

      return provenance;
    });
};


const updateStyle = (tableName, style) => ({
  type: Types.SET_LAYER_STYLE,
  tableName,
  style,
});

export const setLayerStyle = (tableName, style) => (dispatch, getState) => {
  const { meta: { csrfToken: token }, ui: { views: { map: { config: { layers = [] } } } } } = getState();
  const layer = layers.find(l => l.tableName === tableName);

  if (!layer) {
    return Promise.resolve();
  }

  dispatch(updateStyle(tableName, style));

  return layer.file ?
    mapService.setFileStyle(layer.file, style, token) :
    mapService.setResourceRevisionStyle(layer.resource.id, layer.resource.version, style, token);
};

const updateFeatureBegin = () => ({
  type: Types.EDIT_COMMIT_BEGIN,
});

const updateFeatureComplete = () => ({
  type: Types.EDIT_COMMIT_COMPLETE,
});

export const updateFeature = () => (dispatch, getState) => {
  const {
    meta: { csrfToken: token },
    ui: { views: { map: {
      edit: { id, table, current: { properties: current }, initial: { properties: initial }, updates },
      config: { selectedFeature: { feature } },
    } } },
  } = getState();

  if (!feature) {
    return Promise.reject(new ServerError([{
      code: -1,
      description: 'No feature is selected',
      level: EnumErrorLevel.ERROR,
    }]));
  }

  const properties = Object.keys(initial).reduce((result, key) => {
    if (initial[key] !== current[key]) {
      result[key] = current[key];
    }
    return result;
  }, {});

  dispatch(updateFeatureBegin());

  return mapService.updateFeature(table, id, properties, updates === 0 ? null : feature, token)
    .then(() => {
      dispatch(updateFeatureComplete());
    });
};

/*
 * Action creators
 */

export const reset = () => ({
  type: Types.RESET,
});

export const setItemPosition = (id, left, top) => ({
  type: Types.SET_ITEM_POSITION,
  id,
  left,
  top,
});

export const setCenter = (center, zoom) => ({
  type: Types.SET_CENTER,
  center,
  zoom,
});

export const selectLayer = (tableName) => ({
  type: Types.SELECT_LAYER,
  tableName,
});

export const toggleLayer = (tableName) => ({
  type: Types.TOGGLE_LAYER,
  tableName,
});

export const toggleLayerConfiguration = () => ({
  type: Types.TOGGLE_LAYER_CONFIG,
});

export const setBaseLayer = (layer) => ({
  type: Types.SET_BASE_LAYER,
  layer,
});

export const setLayerColor = (tableName, color) => ({
  type: Types.SET_LAYER_COLOR,
  tableName,
  color,
});

export const selectFeatures = (features) => ({
  type: Types.SET_SELECTED_FEATURES,
  features,
});

export const clearSelectedFeatures = () => ({
  type: Types.CLEAR_SELECTED_FEATURES,
});

export const selectGeometrySnapshot = (index, geometry) => ({
  type: Types.SELECT_GEOMETRY_SNAPSHOT,
  index,
  geometry,
});

export const hideProvenance = () => ({
  type: Types.HIDE_PROVENANCE,
});

export const bringToFront = (id) => ({
  type: Types.BRING_PANEL_TO_FRONT,
  id,
});

// Search

export const toggleFilter = (value = null) => ({
  type: Types.FILTER_TOGGLE,
  value,
});

export const setFilter = (filters = []) => ({
  type: Types.FILTER_SET,
  filters,
});

// Edit

export const toggleEditor = (feature = null) => ({
  type: Types.EDIT_TOGGLE,
  feature,
});

export const updateFeatureProperty = (key, value) => ({
  type: Types.EDIT_UPDATE_PROPERTY,
  key,
  value,
});

export const updateFeatureVertex = () => ({
  type: Types.EDIT_VERTEX,
});

export const cancelEdit = () => ({
  type: Types.EDIT_CANCEL,
});
