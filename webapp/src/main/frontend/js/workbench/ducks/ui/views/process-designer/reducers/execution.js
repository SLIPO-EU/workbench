import * as Types from '../types';

export function executionReducer(state, action) {
  switch (action.type) {
    case Types.REQUEST_EXECUTION_DATA:
      return {
        ...state.execution,
        data: null,
        selectedKpi: null,
        selectedLog: null,
        selectedRow: null,
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
        selectedKpi: null,
        selectedLog: null,
        selectedRow: action.id,
      };

    case Types.REQUEST_EXECUTION_KPI_DATA:
      return {
        ...state.execution,
        selectedKpi: {
          id: action.id,
          data: null,
          original: null,
        },
        selectedLog: null,
      };

    case Types.RECEIVE_EXECUTION_KPI_DATA:
      return {
        ...state.execution,
        selectedKpi: {
          ...state.selectedKpi,
          data: action.data.values,
          original: action.data.original,
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
        selectedKpi: null,
        selectedLog: null,
        selectedRow: null,
      };

    default:
      return state;
  }
}
