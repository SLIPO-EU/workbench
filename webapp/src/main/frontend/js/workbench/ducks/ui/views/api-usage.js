import * as processService from '../../../service/process';

import {
  dom,
  file,
} from '../../../service/api';

// Actions
import { LOGOUT } from '../../user';

const SET_PAGER = 'ui/process/api/usage/explorer/SET_PAGER';
const RESET_PAGER = 'ui/process/api/usage/explorer/RESET_PAGER';
const SET_FILTER = 'ui/process/api/usage/explorer/SET_FILTER';
const RESET_FILTERS = 'ui/process/api/usage/explorer/RESET_FILTERS';

const REQUEST_API_USAGE_DATA = 'ui/process/api/usage/explorer/REQUEST_API_USAGE_DATA';
const RECEIVE_API_USAGE_DATA = 'ui/process/api/usage/explorer/RECEIVE_API_USAGE_DATA';

const SET_SELECTED = 'ui/process/api/usage/explorer/SET_SELECTED';
const RESET_SELECTED = 'ui/process/api/usage/explorer/RESET_SELECTED';
const SET_EXPANDED = 'ui/process/api/usage/explorer/SET_EXPANDED';

const REQUEST_EXECUTION_DATA = 'ui/process/api/usage/explorer/REQUEST_EXECUTION_DATA';
const RECEIVE_EXECUTION_DATA = 'ui/process/api/usage/explorer/RECEIVE_EXECUTION_DATA';

// Initial state
const initialState = {
  expanded: {
    index: null,
    reason: null,
  },
  filters: {
    name: null,
    operation: null,
    status: null,
  },
  pager: {
    index: 0,
    size: 10,
    count: 0,
  },
  items: [],
  selected: null,
  lastUpdate: null,
  execution: null,
};

// Reducer
export default (state = initialState, action) => {
  switch (action.type) {
    case LOGOUT:
      return initialState;

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
      };

    case REQUEST_API_USAGE_DATA:
      return {
        ...state,
        selected: null,
      };

    case RECEIVE_API_USAGE_DATA:
      return {
        ...state,
        items: action.result.items,
        pager: {
          index: action.result.pagingOptions.pageIndex,
          size: action.result.pagingOptions.pageSize,
          count: action.result.pagingOptions.count,
        },
        lastUpdate: new Date(),
      };

    case SET_SELECTED:
      return {
        ...state,
        expanded: {
          index: null,
          reason: null,
        },
        selected: {
          id: action.id,
        },
      };

    case RESET_SELECTED:
      return {
        ...state,
        selected: null,
      };

    case SET_EXPANDED: {
      return {
        ...state,
        expanded: {
          index: action.index,
          reason: action.reason,
        }
      };
    }

    case REQUEST_EXECUTION_DATA:
      return {
        ...state,
        execution: null,
      };

    case RECEIVE_EXECUTION_DATA:
      return {
        ...state,
        execution: action.data,
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

export const setSelected = (id) => ({
  type: SET_SELECTED,
  id,
});

export const resetSelected = () => ({
  type: RESET_SELECTED,
});

export const setExpanded = (index, reason) => ({
  type: SET_EXPANDED,
  index,
  reason,
});

// Thunk actions
const requestApiUsageData = () => ({
  type: REQUEST_API_USAGE_DATA,
});

const receiveApiUsageData = (result) => ({
  type: RECEIVE_API_USAGE_DATA,
  result,
});

export const fetchExecutions = (query) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(requestApiUsageData());

  return processService.fetchApiCalls(query, token)
    .then((result) => {
      dispatch(receiveApiUsageData(result));
    })
    .catch((err) => {
      console.error('Failed loading API usage data:', err);
    });
};

const requestExecutionData = () => ({
  type: REQUEST_EXECUTION_DATA,
});

const receiveExecutionData = (data) => ({
  type: RECEIVE_EXECUTION_DATA,
  data,
});

export const fetchExecution = (processId) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(requestExecutionData());

  return processService.fetchApiExecution(processId, token)
    .then((data) => {
      dispatch(receiveExecutionData(data));
    });
};

export const fetchExecutionKpiData = (processId, processVersion, executionId, fileId, tool) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  return processService.fetchExecutionKpiData(processId, processVersion, executionId, fileId, token, tool);
};

export const fetchExecutionLogData = (processId, processVersion, executionId, fileId) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  return processService.fetchExecutionLogData(processId, processVersion, executionId, fileId, token);
};

export const checkFile = (processId, processVersion, executionId, fileId) => {
  return (dispatch, getState) => {
    const { meta: { csrfToken: token } } = getState();
    const url = `/action/process/${processId}/${processVersion}/execution/${executionId}/file/${fileId}/exists`;

    return file.exists(url, token);
  };
};

export const downloadFile = (processId, processVersion, executionId, fileId, fileName) => {
  return () => {
    const url = `/action/process/${processId}/${processVersion}/execution/${executionId}/file/${fileId}/download`;

    dom.downloadUrl(url, fileName);

    return Promise.resolve();
  };
};

export const checkLog = (processId, processVersion, executionId, fileId) => {
  return (dispatch, getState) => {
    const { meta: { csrfToken: token } } = getState();
    const url = `/action/process/${processId}/${processVersion}/execution/${executionId}/log/${fileId}/exists`;

    return file.exists(url, token);
  };
};

export const downloadLog = (processId, processVersion, executionId, fileId, fileName) => {
  return () => {
    const url = `/action/process/${processId}/${processVersion}/execution/${executionId}/log/${fileId}/download`;

    dom.downloadUrl(url, fileName);

    return Promise.resolve();
  };
};
