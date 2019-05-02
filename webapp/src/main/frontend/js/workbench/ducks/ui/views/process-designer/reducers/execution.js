import * as Types from '../types';

export function executionReducer(state, action) {
  switch (action.type) {
    case Types.REQUEST_EXECUTION_DATA:
      return {
        ...state.execution,
        data: null,
        selectedFile: null,
        selectedKpi: null,
        selectedLog: null,
      };

    case Types.RECEIVE_EXECUTION_DATA:
      return {
        ...state.execution,
        data: { ...action.data },
        lastUpdate: new Date(),
      };

    case Types.SET_SELECTED_FILE:
      return {
        ...state.execution,
        selectedFile: action.id,
        selectedKpi: null,
        selectedLog: null,
      };

    case Types.REQUEST_EXECUTION_KPI_DATA:
      return {
        ...state.execution,
        selectedKpi: {
          id: action.id,
          mode: action.mode,
          data: null,
        },
        selectedLog: null,
      };

    case Types.RECEIVE_EXECUTION_KPI_DATA:
      return {
        ...state.execution,
        selectedKpi: {
          ...state.selectedKpi,
          data: action.data.values,
        },
      };

    case Types.REQUEST_EXECUTION_LOG_DATA:
      return {
        ...state.execution,
        selectedKpi: null,
        selectedLog: {
          id: action.id,
          data: null,
        },
      };

    case Types.RECEIVE_EXECUTION_LOG_DATA:
      return {
        ...state.execution,
        selectedLog: {
          ...state.selectedLog,
          data: action.data,
        },
      };

    case Types.RESET_SELECTED_FILE:
      return {
        ...state.execution,
        selectedFile: null,
        selectedKpi: null,
        selectedLog: null,
      };

    default:
      return state;
  }
}
