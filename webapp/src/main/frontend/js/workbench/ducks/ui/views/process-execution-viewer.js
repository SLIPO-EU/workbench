import * as processService from '../../../service/process';

import {
  EnumKpiViewMode
} from '../../../model/constants';

// Actions
const RESET = 'ui/process/execution/viewer/RESET';

const REQUEST_EXECUTION_DATA = 'ui/process/execution/viewer/REQUEST_EXECUTION_DATA';
const RECEIVE_EXECUTION_DATA = 'ui/process/execution/viewer/RECEIVE_EXECUTION_DATA';

const REQUEST_EXECUTION_KPI_DATA = 'ui/process/execution/viewer/REQUEST_EXECUTION_KPI_DATA';
const RECEIVE_EXECUTION_KPI_DATA = 'ui/process/execution/viewer/RECEIVE_EXECUTION_KPI_DATA';

const RESET_SELECTED_KPI = 'ui/process/execution/viewer/RESET_SELECTED_KPI';

const SET_SELECTED_STEP = 'ui/process/execution/viewer/SET_SELECTED_STEP';
const SET_SELECTED_FILE = 'ui/process/execution/viewer/SET_SELECTED_FILE';
const SET_SELECTED_LAYER = 'ui/process/execution/viewer/SET_SELECTED_LAYER';
const SET_SELECTED_FEATURES = 'ui/process/execution/viewer/SET_SELECTED_FEATURES';

const ADD_FILE_TO_MAP = 'ui/process/execution/viewer/ADD_FILE_TO_MAP';
const REMOVE_FILE_FROM_MAP = 'ui/process/execution/viewer/REMOVE_FILE_FROM_MAP';

const TOGGLE_MAP = 'ui/process/execution/viewer/TOGGLE_MAP';

// Helper methods
const createLayer = (state, id) => {
  const execution = state.execution;
  const step = execution.steps.find((s) => s.id === state.selectedStep);
  const file = step.files.find((f) => f.id === id);

  return {
    id,
    execution: execution.name,
    tool: step.component,
    step: step.name,
    file: file.fileName,
    table: file.tableName,
    type: file.type,
    visible: true,
    opacity: 100,
  };
};

// Initial state
const initialState = {
  displayMap: false,
  execution: null,
  lastUpdate: null,
  layers: [],
  selectedStep: null,
  selectedFile: null,
  selectedLayer: null,
  selectedFeatures: [],
  selectedKpi: null,
};

// Reducer
const fileReducer = (state, action) => {
  switch (action.type) {
    case RECEIVE_EXECUTION_DATA:
      return state.map((f) => {
        return {
          ...f,
          showInMap: false,
        };
      });

    case ADD_FILE_TO_MAP:
      return state.map((f) => {
        return {
          ...f,
          showInMap: f.showInMap || (f.id === action.id),
        };
      });

    case REMOVE_FILE_FROM_MAP:
      return state.map((f) => {
        return {
          ...f,
          showInMap: f.showInMap && (f.id !== action.id),
        };
      });

    default:
      return state;
  }
};

const stepReducer = (state, action, selectedStep) => {
  switch (action.type) {
    case ADD_FILE_TO_MAP:
    case RECEIVE_EXECUTION_DATA:
    case REMOVE_FILE_FROM_MAP:
      return state.map((s) => {
        return {
          ...s,
          files: fileReducer(s.files, action),
        };
      });

    default:
      return state;
  }
};

export default (state = initialState, action) => {
  switch (action.type) {
    case RESET:
      return {
        ...initialState,
      };

    case REQUEST_EXECUTION_DATA:
      return {
        ...state,
        displayMap: false,
        execution: null,
        layers: [],
        selectedStep: null,
        selectedFile: null,
        selectedLayer: null,
        selectedFeatures: [],
        selectedKpi: null,
      };

    case RECEIVE_EXECUTION_DATA:
      return {
        ...state,
        execution: {
          ...action.data,
          steps: stepReducer(action.data.steps, action, state.selectedStep),
        },
        lastUpdate: new Date(),
      };

    case REQUEST_EXECUTION_KPI_DATA:
      return {
        ...state,
        selectedKpi: {
          id: action.id,
          mode: action.mode,
          data: null,
        }
      };

    case RECEIVE_EXECUTION_KPI_DATA:
      return {
        ...state,
        selectedKpi: {
          ...state.selectedKpi,
          data: action.data.values,
        },
      };

    case SET_SELECTED_STEP:
      return {
        ...state,
        selectedStep: action.id,
        selectedFile: null,
        selectedKpi: null,
      };

    case SET_SELECTED_FILE:
      return {
        ...state,
        selectedFile: action.id,
      };

    case RESET_SELECTED_KPI:
      return {
        ...state,
        selectedKpi: null,
      };

    case SET_SELECTED_LAYER:
      return {
        ...state,
        selectedLayer: action.id
      };

    case SET_SELECTED_FEATURES:
      return {
        ...state,
        selectedFeatures: action.features || [],
      };

    case ADD_FILE_TO_MAP:
      if (state.layers.find((l) => l.id === action.id)) {
        return state;
      }
      return {
        ...state,
        execution: {
          ...state.execution,
          steps: stepReducer(state.execution.steps, action, state.selectedStep),
        },
        layers: [...state.layers, createLayer(state, action.id)],
      };

    case REMOVE_FILE_FROM_MAP:
      return {
        ...state,
        execution: {
          ...state.execution,
          steps: stepReducer(state.execution.steps, action, state.selectedStep),
        },
        layers: state.layers.filter((l) => l.id !== action.id),
        displayMap: state.displayMap && (state.layers.length > 1),
      };

    case TOGGLE_MAP:
      return {
        ...state,
        displayMap: !state.displayMap,
      };

    default:
      return state;
  }
};

// Action creators
export const reset = () => ({
  type: RESET,
});

export const selectStep = (id) => ({
  type: SET_SELECTED_STEP,
  id,
});

export const selectFeatures = (features) => ({
  type: SET_SELECTED_FEATURES,
  features,
});

export const selectFile = (id) => ({
  type: SET_SELECTED_FILE,
  id,
});

export const addToMap = (id) => ({
  type: ADD_FILE_TO_MAP,
  id,
});

export const removeFromMap = (id) => ({
  type: REMOVE_FILE_FROM_MAP,
  id,
});

export const resetKpi = () => ({
  type: RESET_SELECTED_KPI,
});

export const selectLayer = (id) => ({
  type: SET_SELECTED_LAYER,
  id,
});

export const toggleMapView = () => ({
  type: TOGGLE_MAP,
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
