import processService from '../../../service/processExplorer';
// Actions
const REQUEST_PROCESS_DATA = 'ui/processExplorer/REQUEST_PROCESS_DATA';
const RECEIVE_PROCESS_DATA = 'ui/processExplorer/RECEIVE_PROCESS_DATA';
const SET_PAGER = 'ui/processExplorer/SET_PAGER';
const SET_SELECTED = 'ui/processExplorer/SET_SELECTED';
const RESET_SELECTED = 'ui/processExplorer/RESET_SELECTED';
const RECEIVE_EXECUTIONS_DATA = 'ui/processExplorer/RECEIVE_EXECUTIONS_DATA';
const SET_SELECTED_EXECUTION = 'ui/processExplorer/SET_SELECTED_EXECUTION';
const RECEIVE_EXECUTIONS_DETAILS = 'ui/processExplorer/RECEIVE_EXECUTIONS_DETAILS';

// Reducer
const initialState = {
  items:[],
  pagingOptions: {
    count: 0,
    pageIndex: 0,
    pageSize: 10,
  },
  selected: null,
  selectedExecution:null,
  selectedFields: [],
  executionStatus: [],
};

export default (state = initialState, action) => {
  switch (action.type) {
    case RECEIVE_PROCESS_DATA:
      return {
        ...state,
        ...action.data,
      }; 
    case RECEIVE_EXECUTIONS_DATA:
      return {
        ...state,
        selectedFields: action.data,
      }; 
    case SET_PAGER:
      return {
        ...state, 
        pagingOptions: {
          ...state.pagingOptions,
          ...action.pager,
        },
      };  
    case SET_SELECTED:
      return {
        ...state,
        selected: action.selected,
        selectedExecution: initialState.selectedExecution,
        executionStatus: initialState.executionStatus,
      };
    case SET_SELECTED_EXECUTION:
      return {
        ...state,
        selectedExecution: action.selected,
      };
    case RECEIVE_EXECUTIONS_DETAILS:
      return {
        ...state,
        executionStatus: action.data,
      }; 
    case RESET_SELECTED:
      return {
        ...state,
        selected: initialState.selected,
        selectedExecution: initialState.selectedExecution,
        executionStatus: initialState.executionStatus,
      };      
    default:
      return state;
  }
};

// Action creators
const requestProcessData = () => ({
  type: REQUEST_PROCESS_DATA,
});
  
const receiveProcessData = (data) => ({
  type: RECEIVE_PROCESS_DATA,
  data,
});

const receiveExecutionsData = (data) => ({
  type: RECEIVE_EXECUTIONS_DATA,
  data,
});


export const setPager = (pager) => ({
  type: SET_PAGER,
  pager,
});

export const setSelectedEx = (selected) => ({
  type: SET_SELECTED_EXECUTION,
  selected,
});

const receiveExecutionsDetails = (data) => ({
  type: RECEIVE_EXECUTIONS_DETAILS,
  data,
});


export const resetSelectedProcess = () => ({
  type: RESET_SELECTED,
});

export const setSelected = (selected) => ({
  type: SET_SELECTED,
  selected,
});

// Thunk actions
export const fetchProcessData = (options) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(requestProcessData());
  const tt= { 
    name: "Link",
    pagingOptions: {
      pageIndex: 0,
      pageSize: 10,
      ...options
    },
  };
    
  return processService.fetch(tt)
    .then((data) => {
      dispatch(receiveProcessData(data));
    })
    .catch((err) => {
      console.error('Failed loading processes:', err);
    });
};

export const setSelectedProcess = (selected) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch( setSelected(selected));
 
  return processService.fetchExecutions(selected)
    .then((data) => {
      dispatch(receiveExecutionsData(data));
    })
    .catch((err) => {
      console.error('Failed loading executions:', err);
    });
};

export const setSelectedExecution = (selected) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch( setSelectedEx(selected.execution));
 
  return processService.fetchExecutionDetails(selected)
    .then((data) => {
      dispatch(receiveExecutionsDetails(data));
    })
    .catch((err) => {
      console.error('Failed loading execution Details:', err);
    });
};

