import * as processService from '../../../service/process';

import {
  EnumKpiViewMode
} from '../../../model';

// Actions
const RESET = 'ui/process/execution/viewer/RESET';

const REQUEST_EXECUTION_DATA = 'ui/process/execution/viewer/REQUEST_EXECUTION_DATA';
const RECEIVE_EXECUTION_DATA = 'ui/process/execution/viewer/RECEIVE_EXECUTION_DATA';

const SET_SELECTED_FILE = 'ui/process/execution/viewer/SET_SELECTED_FILE';
const RESET_SELECTED_FILE = 'ui/process/execution/viewer/RESET_SELECTED_FILE';

const REQUEST_EXECUTION_KPI_DATA = 'ui/process/execution/viewer/REQUEST_EXECUTION_KPI_DATA';
const RECEIVE_EXECUTION_KPI_DATA = 'ui/process/execution/viewer/RECEIVE_EXECUTION_KPI_DATA';

const RESET_SELECTED_KPI = 'ui/process/execution/viewer/RESET_SELECTED_KPI';


// Initial state
const initialState = {
  execution: null,
  lastUpdate: null,
  selectedFile: null,
  selectedKpi: null,
};

// Reducer
export default (state = initialState, action) => {
  switch (action.type) {
    case RESET:
      return {
        ...initialState,
      };

    case REQUEST_EXECUTION_DATA:
      return {
        ...state,
        execution: null,
        selectedFile: null,
        selectedKpi: null,
      };

    case RECEIVE_EXECUTION_DATA:
      return {
        ...state,
        execution: {
          ...action.data,
        },
        lastUpdate: new Date(),
      };

    case SET_SELECTED_FILE:
      return {
        ...state,
        selectedFile: action.id,
      };

    case REQUEST_EXECUTION_KPI_DATA:
      return {
        ...state,
        selectedKpi: {
          id: action.id,
          mode: action.mode,
          data: null,
        },
      };

    case RECEIVE_EXECUTION_KPI_DATA:
      return {
        ...state,
        selectedKpi: {
          ...state.selectedKpi,
          data: action.data.values,
        },
      };

    case RESET_SELECTED_FILE:
      return {
        ...state,
        selectedFile: null,
        selectedKpi: null,
      };

    case RESET_SELECTED_KPI:
      return {
        ...state,
        selectedKpi: null,
      };

    default:
      return state;
  }
};

// Action creators
export const reset = () => ({
  type: RESET,
});

export const selectFile = (id) => ({
  type: SET_SELECTED_FILE,
  id,
});

export const resetSelectedFile = () => ({
  type: RESET_SELECTED_FILE,
});

export const resetSelectedKpi = () => ({
  type: RESET_SELECTED_KPI,
});

// Thunk actions
const requestExecutionData = () => ({
  type: REQUEST_EXECUTION_DATA,
});

const receiveExecutionData = (data) => ({
  type: RECEIVE_EXECUTION_DATA,
  data,
});

export const fetchExecutionDetails = (process, version, execution) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(requestExecutionData());

  return processService.fetchExecutionDetails(process, version, execution, token)
    .then((data) => {
      dispatch(receiveExecutionData(data));
    });
};

const requestExecutionKpiData = (id, mode) => ({
  type: REQUEST_EXECUTION_KPI_DATA,
  id,
  mode,
});

const receiveExecutionKpiData = (data) => ({
  type: RECEIVE_EXECUTION_KPI_DATA,
  data,
});

export const fetchExecutionKpiData = (process, version, execution, file, mode) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(requestExecutionKpiData(file, mode));

  return processService.fetchExecutionKpiData(process, version, execution, file, mode, token)
    .then((data) => {
      dispatch(receiveExecutionKpiData(data));
    });
};
