import * as processService from '../../../../service/process';

import {
  EnumDesignerSaveAction,
} from '../../../../model/process-designer';

/*
 * Action types
 */
import * as Types from './types';

import {
  EnumErrorLevel,
  ServerError,
} from '../../../../model';

import {
  dom,
  file,
} from '../../../../service/api';

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
      dispatch(processLoaded(data.process, true, false, getState().config));
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

  return processService.fetchExecutionKpiData(process, version, execution, file, token)
    .then((data) => {
      dispatch(receiveExecutionKpiData(data));
    });
};

const processLoaded = function (process, readOnly, clone, appConfiguration) {
  return {
    type: Types.LOAD_RECEIVE_RESPONSE,
    process,
    readOnly,
    clone,
    appConfiguration,
  };
};

export function fetchProcess(id) {
  return (dispatch, getState) => {
    if (Number.isNaN(id)) {
      return Promise.reject(new Error('Invalid id. Failed to load workflow instance'));
    }

    const { meta: { csrfToken: token } } = getState();
    return processService.fetchProcess(id, token).then((process) => {
      dispatch(processLoaded(process, false, false, getState().config));
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
      dispatch(processLoaded(process, true, false, getState().config));
    });
  };
}

export function save(action, process, isTemplate) {
  return (dispatch, getState) => {
    const errors = processService.validate(action, process, isTemplate);
    if (errors.length !== 0) {
      return Promise.reject(new Error('Validation has failed'));
    }

    const { meta: { csrfToken: token } } = getState();
    return processService.save(action, process, token);
  };
}

export function cloneTemplate(id, version) {
  return (dispatch, getState) => {
    if (Number.isNaN(id)) {
      return Promise.reject(new Error('Invalid id. Failed to load template instance'));
    }
    if (Number.isNaN(version)) {
      return Promise.reject(new Error('Invalid version. Failed to load template instance'));
    }

    const { meta: { csrfToken: token } } = getState();
    return processService.fetchProcessRevision(id, version, token).then((process) => {
      dispatch(processLoaded(process, false, true, getState().config));
    });
  };
}

const filedDownloaded = function (fileId, fileName) {
  return {
    type: Types.FILE_DOWNLOAD_REQUEST,
    fileId,
    fileName,
  };
};

export const downloadFile = (id, version, executionId, fileId, fileName) => {
  return (dispatch, getState) => {
    const { meta: { csrfToken: token } } = getState();
    const url = `/action/process/${id}/${version}/execution/${executionId}/file/${fileId}`;

    return file.download(url, token)
      .then(data => {
        dom.downloadLink(data, fileName);

        dispatch(filedDownloaded(fileId, fileName));
      });
  };
};
