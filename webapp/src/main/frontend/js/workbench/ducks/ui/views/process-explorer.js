import * as processService from '../../../service/process';

// Actions
const SET_PAGER = 'ui/process/explorer/SET_PAGER';
const RESET_PAGER = 'ui/process/explorer/RESET_PAGER';
const SET_FILTER = 'ui/process/explorer/SET_FILTER';
const RESET_FILTERS = 'ui/process/explorer/RESET_FILTERS';

const REQUEST_PROCESS_DATA = 'ui/process/explorer/REQUEST_PROCESS_DATA';
const RECEIVE_PROCESS_DATA = 'ui/process/explorer/RECEIVE_PROCESS_DATA';
const SET_SELECTED_PROCESS = 'ui/process/explorer/SET_SELECTED_PROCESS';
const RESET_SELECTED_PROCESS = 'ui/process/explorer/RESET_SELECTED_PROCESS';

const REQUEST_EXECUTION_DATA = 'ui/process/explorer/REQUEST_EXECUTION_DATA';
const RECEIVE_EXECUTION_DATA = 'ui/process/explorer/RECEIVE_EXECUTION_DATA';
const SET_SELECTED_EXECUTION = 'ui/process/explorer/SET_SELECTED_EXECUTION';

const START_PROCESS = 'ui/process/explorer/START_PROCESS';

// Initial state
const initialState = {
  filters: {
    name: null,
  },
  pager: {
    index: 0,
    size: 10,
    count: 0,
  },
  items: [],
  selected: null,
  executions: [],
  lastUpdate: null,
};

// Reducer
export default (state = initialState, action) => {
  switch (action.type) {
    case SET_PAGER:
      return {
        ...state,
        pager: {
          ...state.pager,
          ...action.pager,
        },
        selected: null,
      };

    case RESET_PAGER:
      return {
        ...state,
        pager: {
          ...initialState.pager
        },
        selected: null,
        executions: [],
      };

    case SET_FILTER: {
      const filters = { ...state.filters };
      filters[action.filter] = action.value;
      return {
        ...state,
        filters,
      };
    }

    case RESET_FILTERS:
      return {
        ...state,
        filters: {
          ...initialState.filters
        },
        selected: null,
        executions: [],
      };

    case REQUEST_PROCESS_DATA:
      return {
        ...state,
        selected: null,
        executions: [],
      };

    case RECEIVE_PROCESS_DATA:
      return {
        ...state,
        items: action.result.items.map((p) => {
          return {
            ...p,
            revisions: p.revisions.sort((v1, v2) => v2.version - v1.version),
          };
        }),
        pager: {
          index: action.result.pagingOptions.pageIndex,
          size: action.result.pagingOptions.pageSize,
          count: action.result.pagingOptions.count,
        },
        lastUpdate: new Date(),
      };

    case SET_SELECTED_PROCESS:
      return {
        ...state,
        selected: {
          id: action.id,
          version: action.version,
          execution: null,
        },
      };

    case RESET_SELECTED_PROCESS:
      return {
        ...state,
        selected: null,
        executions: [],
      };

    case RECEIVE_EXECUTION_DATA:
      return {
        ...state,
        executions: action.result,
      };

    case SET_SELECTED_EXECUTION:
      return {
        ...state,
        selected: {
          id: action.id,
          version: action.version,
          execution: action.execution,
        },
      };

    default:
      return state;
  }
};

// Action creators
export const setPager = (pager) => ({
  type: SET_PAGER,
  pager,
});

export const resetPager = () => ({
  type: RESET_PAGER,
});

export const setFilter = (filter, value) => ({
  type: SET_FILTER,
  filter,
  value,
});

export const resetFilters = () => ({
  type: RESET_FILTERS,
});

export const resetSelectedProcess = () => ({
  type: RESET_SELECTED_PROCESS,
});

// Thunk actions
const requestProcessData = () => ({
  type: REQUEST_PROCESS_DATA,
});

const receiveProcessData = (result) => ({
  type: RECEIVE_PROCESS_DATA,
  result,
});

export const fetchProcesses = (query) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(requestProcessData());

  return processService.fetchProcesses(query, token)
    .then((result) => {
      dispatch(receiveProcessData(result));
    })
    .catch((err) => {
      console.error('Failed loading processes:', err);
    });
};

const setSelectedProcess = (id, version) => ({
  type: SET_SELECTED_PROCESS,
  id,
  version,
});

const requestExecutionData = () => ({
  type: REQUEST_EXECUTION_DATA,
});

const receiveExecutionData = (result) => ({
  type: RECEIVE_EXECUTION_DATA,
  result,
});

export const fetchProcessExecutions = (id, version) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(setSelectedProcess(id, version));
  dispatch(requestExecutionData());

  return processService.fetchProcessExecutions(id, version, token)
    .then((data) => {
      dispatch(receiveExecutionData(data));
    })
    .catch((err) => {
      console.error('Failed loading executions:', err);
    });
};

const processExecutionStarted = () => ({
  type: START_PROCESS,
});

export const start = (id) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();

  return processService.start(id, token)
    .then(() => {
      processExecutionStarted();
    });
};
