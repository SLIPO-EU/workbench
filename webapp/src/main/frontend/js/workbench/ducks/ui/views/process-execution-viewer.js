import * as processService from '../../../service/process';

// Actions
const REQUEST_EXECUTION_DATA = 'ui/process/execution/viewer/REQUEST_EXECUTION_DATA';
const RECEIVE_EXECUTION_DATA = 'ui/process/execution/viewer/RECEIVE_EXECUTION_DATA';

const SET_SELECTED_STEP = 'ui/process/execution/viewer/SET_SELECTED_STEP';
const SET_SELECTED_FILE = 'ui/process/execution/viewer/SET_SELECTED_FILE';

const ADD_FILE_TO_MAP = 'ui/process/execution/viewer/ADD_FILE_TO_MAP';
const REMOVE_FILE_FROM_MAP = 'ui/process/execution/viewer/REMOVE_FILE_FROM_MAP';

const TOGGLE_MAP_VIEW = 'ui/process/execution/viewer/TOGGLE_MAP_VIEW';

// Initial state
const initialState = {
  execution: null,
  mapFiles: [],
  selectedStep: null,
  selectedFile: null,
  lastUpdate: null,
  displayMapView: false,
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
    case RECEIVE_EXECUTION_DATA:
      return state.map((s) => {
        return {
          ...s,
          files: fileReducer(s.files, action),
        };
      });

    case ADD_FILE_TO_MAP:
    case REMOVE_FILE_FROM_MAP:
      return state.map((s) => {
        if (s.id !== selectedStep) {
          return s;
        }
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
    case REQUEST_EXECUTION_DATA:
      return {
        ...state,
        execution: null,
        mapFiles: [],
        selectedStep: null,
        selectedFile: null,
      };

    case RECEIVE_EXECUTION_DATA:
      return {
        ...state,
        execution: {
          ...action.data,
          mapFiles: [...state.mapFiles, action.id],
          steps: stepReducer(action.data.steps, action, state.selectedStep),
        },
        lastUpdate: new Date(),
      };

    case SET_SELECTED_STEP:
      return {
        ...state,
        selectedStep: action.id,
        selectedFile: null,
      };

    case SET_SELECTED_FILE:
      return {
        ...state,
        selectedFile: action.id,
      };

    case ADD_FILE_TO_MAP:
      if (state.mapFiles.find((f) => f.id === action.id)) {
        return state;
      }
      return {
        ...state,
        mapFiles: [...state.mapFiles, action.id],
        execution: {
          ...state.execution,
          steps: stepReducer(state.execution.steps, action, state.selectedStep),
        }
      };

    case REMOVE_FILE_FROM_MAP:
      return {
        ...state,
        mapFiles: state.mapFiles.filter((f) => f.id !== action.id),
        execution: {
          ...state.execution,
          steps: stepReducer(state.execution.steps, action, state.selectedStep),
        },
      };

    case TOGGLE_MAP_VIEW:
      return {
        ...state,
        displayMapView: !state.displayMapView,

      };

    default:
      return state;
  }
};

// Action creators
export const selectStep = (id) => ({
  type: SET_SELECTED_STEP,
  id,
});

export const selectFile = (id) => ({
  type: SET_SELECTED_FILE,
  id,
});

export const addToMap = (id) => ({
  type: ADD_FILE_TO_MAP,
  id: id,
});

export const removeFromMap = (id) => ({
  type: REMOVE_FILE_FROM_MAP,
  id: id,
});

export const toggleMapView = () => ({
  type: TOGGLE_MAP_VIEW,
});

// Thunk actions
const requestExecutionData = () => ({
  type: REQUEST_EXECUTION_DATA,
});

const receiveExecutionData = (data) => ({
  type: RECEIVE_EXECUTION_DATA,
  data,
});

export const fetchExecutionDetails = (id, version, execution) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(requestExecutionData());

  return processService.fetchExecutionDetails(id, version, execution, token)
    .then((data) => {
      dispatch(receiveExecutionData(data));
    });
};
