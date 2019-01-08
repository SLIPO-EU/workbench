import * as mapService from '../../../service/map';
import * as provenanceService from '../../../service/provenance';

import {
  EnumPane,
} from '../../../model/map-viewer';

import {
  processExecutionToLayers,
  provenanceToTable,
} from './map-viewer/util';

/*
 * Action types
 */

import {
  LOGOUT,
} from '../../user';

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
};

/*
 * Initial state
 */

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

const configReducer = (state, action) => {
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
        selectedFeature: null,
        draggableOrder: [...state.draggableOrder.filter(id => id != EnumPane.FeatureCollection), EnumPane.FeatureCollection],
      };

    case Types.CLEAR_SELECTED_FEATURES:
      return {
        ...state,
        selectedFeatures: [],
        selectedFeature: null,
      }

    case Types.HIDE_PROVENANCE:
      return {
        ...state,
        provenance: null,
      };

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
        provenance: provenanceToTable(action.data),
      };

    case Types.SELECT_FEATURE:
      return {
        ...state,
        selectedFeature: {
          outputKey: action.outputKey,
          featureId: action.featureId,
        }
      };

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
      return {
        ...state,
        config: configReducer(state.config, action),
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

const receiveFeatureProvenance = (data) => ({
  type: Types.RECEIVE_FEATURE_PROVENANCE,
  data,
});

const selectFeature = (outputKey, featureId) => ({
  type: Types.SELECT_FEATURE,
  outputKey,
  featureId,
});

export const fetchFeatureProvenance = (processId, processVersion, executionId, outputKey, featureId, featureUri) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();

  dispatch(selectFeature(outputKey, featureId));
  dispatch(requestFeatureProvenance());

  return provenanceService.fetchFeatureProvenance(processId, processVersion, executionId, outputKey, featureId, featureUri, token)
    .then((data) => {
      dispatch(receiveFeatureProvenance(data));
      dispatch(bringToFront(EnumPane.FeatureProvenance));
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

export const hideProvenance = () => ({
  type: Types.HIDE_PROVENANCE,
})

export const bringToFront = (id) => ({
  type: Types.BRING_PANEL_TO_FRONT,
  id,
});
