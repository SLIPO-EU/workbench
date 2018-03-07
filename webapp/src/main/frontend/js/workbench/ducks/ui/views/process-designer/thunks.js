import * as processService from '../../../../service/process';

import {
  EnumDesignerSaveAction,
} from '../../../../model/process-designer';

/*
 * Action types
 */
import * as Types from './types';

/*
 * Thunk actions
 */
const requestExecutionData = () => ({
  type: Types.REQUEST_EXECUTION_DATA,
});

const receiveExecutionData = (data) => ({
  type: Types.RECEIVE_EXECUTION_DATA,
  data,
});

export const fetchExecutionDetails = (process, version, execution) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(requestExecutionData());

  return processService.fetchExecutionDetails(process, version, execution, token)
    .then((data) => {
      dispatch(processLoaded(data.process, true));
      dispatch(receiveExecutionData(data.execution));
    });
};

const requestExecutionKpiData = (id, mode) => ({
  type: Types.REQUEST_EXECUTION_KPI_DATA,
  id,
  mode,
});

const receiveExecutionKpiData = (data) => ({
  type: Types.RECEIVE_EXECUTION_KPI_DATA,
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

const processLoaded = function (process, readOnly) {
  return {
    type: Types.LOAD_RECEIVE_RESPONSE,
    process,
    readOnly,
  };
};

export function fetchProcess(id) {
  return (dispatch, getState) => {
    if (Number.isNaN(id)) {
      return Promise.reject(new Error('Invalid id. Failed to load workflow instance'));
    }

    const { meta: { csrfToken: token } } = getState();
    return processService.fetchProcess(id, token).then((process) => {
      dispatch(processLoaded(process, false));
    });
  };
}

export function fetchProcessRevision(id, version) {
  return (dispatch, getState) => {
    if (Number.isNaN(id)) {
      return Promise.reject(new Error('Invalid id. Failed to load workflow instance'));
    }
    if (Number.isNaN(version)) {
      return Promise.reject(new Error('Invalid version. Failed to load workflow instance'));
    }

    const { meta: { csrfToken: token } } = getState();
    return processService.fetchProcessRevision(id, version, token).then((process) => {
      dispatch(processLoaded(process, true));
    });
  };
}

export function save(action, process) {
  return (dispatch, getState) => {
    if (action === EnumDesignerSaveAction.SaveAsTemplate) {
      return Promise.reject(new Error('Not Implemented!'));
    }

    const errors = processService.validate(action, process);
    if (errors.length !== 0) {
      return Promise.reject(new Error('Validation has failed'));
    }

    const { meta: { csrfToken: token } } = getState();
    return processService.save(action, process, token);
  };
}
