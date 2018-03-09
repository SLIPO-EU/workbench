import * as Types from '../types';

import {
  resourceToLayers,
} from '../util';


export function executionReducer(state, action) {
  switch (action.type) {
    case Types.REQUEST_EXECUTION_DATA:
      return {
        ...state.execution,
        data: null,
        layers: [],
        selectedFile: null,
        selectedKpi: null,
        selectedLayer: null,
        selectedFeatures: [],
      };

    case Types.RECEIVE_EXECUTION_DATA:
      return {
        ...state.execution,
        data: { ...action.data },
        layers: resourceToLayers(state.steps, state.resources, action.data),
        lastUpdate: new Date(),
      };

    case Types.SET_SELECTED_FILE:
      return {
        ...state.execution,
        selectedFile: action.id,
        selectedKpi: null,
      };

    case Types.REQUEST_EXECUTION_KPI_DATA:
      return {
        ...state.execution,
        selectedKpi: {
          id: action.id,
          mode: action.mode,
          data: null,
        },
      };

    case Types.RECEIVE_EXECUTION_KPI_DATA:
      return {
        ...state.execution,
        selectedKpi: {
          ...state.selectedKpi,
          data: action.data.values,
        },
      };

    case Types.RESET_SELECTED_FILE:
      return {
        ...state.execution,
        selectedFile: null,
        selectedKpi: null,
      };

    case Types.RESET_SELECTED_KPI:
      return {
        ...state.execution,
        selectedKpi: null,
      };

    case Types.TOGGLE_LAYER:
      return {
        ...state.execution,
        layers: state.execution.layers.map((l) => {
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
        ...state.execution,
        selectedLayer: action.tableName,
      };

    case Types.SET_BASE_LAYER:
      return {
        ...state.execution,
        baseLayer: action.layer,
      };

    case Types.SET_SELECTED_FEATURES:
      return {
        ...state.execution,
        selectedFeatures: action.features || [],
      };

    default:
      return state;
  }
}
