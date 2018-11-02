import * as Types from '../types';

export function executionReducer(state, action) {
  switch (action.type) {
    case Types.REQUEST_EXECUTION_DATA:
      return {
        ...state.execution,
        data: null,
        selectedFile: null,
        selectedKpi: null,
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

      default:
      return state;
  }
}
