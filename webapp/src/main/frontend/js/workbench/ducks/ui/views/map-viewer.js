import * as mapService from '../../../service/map';

import {
  processExecutionToLayers,
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
  SELECT_LAYER: 'ui/map/viewer/SELECT_LAYER',
  TOGGLE_LAYER: 'ui/map/viewer/TOGGLE_LAYER',
  SET_BASE_LAYER: 'ui/map/viewer/SET_BASE_LAYER',
  SET_LAYER_COLOR: 'ui/map/viewer/SET_LAYER_COLOR',
  SET_SELECTED_FEATURES: 'ui/map/viewer/SET_SELECTED_FEATURES',
};

/*
 * Initial state
 */

const initialState = {
  data: {
    resource: null,
    process: null,
    execution: null,
    version: null,
  },
  config: {
    layers: [],
    baseLayer: 'BingMaps-Road',
    selectedLayer: null,
    selectedFeatures: [],
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
        // Preserve existing resource and version values
        process: action.data.process,
        execution: action.data.execution,
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

    case Types.SELECT_LAYER:
      return {
        ...state,
        selectedLayer: action.tableName,
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

    case Types.SET_SELECTED_FEATURES:
      return {
        ...state,
        selectedFeatures: action.features || [],
      };

    case Types.RECEIVE_EXECUTION_MAP_DATA:
    case Types.RECEIVE_RESOURCE_MAP_DATA:
      return {
        ...state,
        layers: processExecutionToLayers(action.data.process, action.data.execution),
        selectedLayer: null,
        selectedFeatures: [],
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
    case Types.RECEIVE_EXECUTION_MAP_DATA:
    case Types.REQUEST_RESOURCE_MAP_DATA:
    case Types.RECEIVE_RESOURCE_MAP_DATA:
      return {
        ...state,
        config: configReducer(state.config, action),
        data: dataReducer(state.data, action),
      };

    case Types.SET_BASE_LAYER:
    case Types.SELECT_LAYER:
    case Types.TOGGLE_LAYER:
    case Types.SET_LAYER_COLOR:
    case Types.SET_SELECTED_FEATURES:
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

/*
 * Action creators
 */

export const reset = () => ({
  type: Types.RESET,
});

export const selectLayer = (tableName) => ({
  type: Types.SELECT_LAYER,
  tableName,
});

export const toggleLayer = (tableName) => ({
  type: Types.TOGGLE_LAYER,
  tableName,
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
