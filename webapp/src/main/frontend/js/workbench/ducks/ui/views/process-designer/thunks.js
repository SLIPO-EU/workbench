import * as processService from '../../../../service/process';
import * as triplegeoService from '../../../../service/toolkit/triplegeo';
/*
 * Action types
 */
import * as Types from './types';

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

const fileExists = function (fileId, fileName) {
  return {
    type: Types.FILE_EXISTS_RESPONSE,
    fileId,
    fileName,
  };
};

export const checkFile = (id, version, executionId, fileId, fileName) => {
  return (dispatch, getState) => {
    const { meta: { csrfToken: token } } = getState();
    const url = `/action/process/${id}/${version}/execution/${executionId}/file/${fileId}/exists`;

    return file.exists(url, token)
      .then(() => {
        dispatch(fileExists(fileId, fileName));
      });
  };
};

const filedDownloaded = function (fileId, fileName) {
  return {
    type: Types.FILE_DOWNLOAD_RESPONSE,
    fileId,
    fileName,
  };
};

export const downloadFile = (id, version, executionId, fileId, fileName) => {
  return (dispatch, getState) => {
    const url = `/action/process/${id}/${version}/execution/${executionId}/file/${fileId}/download`;

    dom.downloadUrl(url, fileName);

    dispatch(filedDownloaded(fileId, fileName));

    return Promise.resolve();
  };
};

export const getTripleGeoMappings = (path) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();

  return triplegeoService.getMappings(path, token)
    .then((mappings) => {
      return mappings;
    });
};

export const getTripleGeoMappingFileAsText = (mappings) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();

  return triplegeoService.getMappingsFileAsText(mappings, token)
    .then((text) => {
      return text;
    });
};
