import _ from 'lodash';

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
    .then((result) => {
      dispatch(processLoaded(result.process, true, false, getState().config));
      dispatch(receiveExecutionData(result.execution));

      return result;
    });
};

const requestExecutionKpiData = (id) => ({
  type: Types.REQUEST_EXECUTION_KPI_DATA,
  id,
});

const receiveExecutionKpiData = (data) => ({
  type: Types.RECEIVE_EXECUTION_KPI_DATA,
  data,
});

export const fetchExecutionKpiData = (process, version, execution, file, tool) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(requestExecutionKpiData(file));

  return processService.fetchExecutionKpiData(process, version, execution, file, token, tool)
    .then((data) => {
      dispatch(receiveExecutionKpiData(data));
    });
};

const requestExecutionLogData = (id) => ({
  type: Types.REQUEST_EXECUTION_LOG_DATA,
  id,
});

const receiveExecutionLogData = (data) => ({
  type: Types.RECEIVE_EXECUTION_LOG_DATA,
  data,
});

export const fetchExecutionLogData = (process, version, execution, file) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(requestExecutionLogData(file));

  return processService.fetchExecutionLogData(process, version, execution, file, token)
    .then((data) => {
      dispatch(receiveExecutionLogData(data));
    });
};

const processLoaded = (process, readOnly, clone, appConfiguration) => ({
  type: Types.LOAD_RECEIVE_RESPONSE,
  process,
  readOnly,
  clone,
  appConfiguration,
});

const saveDraftSuccess = (draft) => ({
  type: Types.SAVE_DRAFT_SUCCESS,
  draft,
});

const restoreDraftSuccess = () => ({
  type: Types.RESTORE_DRAFT_SUCCESS,
});

export function fetchProcess(id) {
  return (dispatch, getState) => {
    if (Number.isNaN(id)) {
      return Promise.reject(new Error('Invalid id. Failed to load workflow instance'));
    }

    const { meta: { csrfToken: token } } = getState();
    return processService.fetchProcess(id, token).then((process) => {
      dispatch(processLoaded(process, false, false, getState().config));

      return process;
    });
  };
}

export function fetchDraft() {
  return (dispatch, getState) => {
    const { meta: { csrfToken: token } } = getState();

    return processService.fetchDraft(token);
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

      return process;
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

export function saveDraft() {
  return (dispatch, getState) => {
    const { meta: { csrfToken: token }, ui: { views: { process: { designer } } } } = getState();

    const prevDraft = designer.draft;
    const nextDraft = JSON.stringify(processService.serializeProcess(designer));

    if (prevDraft !== nextDraft) {
      const { process: { id, version } } = designer;

      return processService.saveDraft(id, version, nextDraft, token)
        .then(() => {
          dispatch(saveDraftSuccess(nextDraft));
        });
    }

    return nextDraft;
  };
}

export function restoreDraft(draft) {
  return (dispatch, getState) => {
    const { config, ui: { views: { process: { designer } } } } = getState();

    const { readOnly, process: { id, version, template, clone, taskType } } = designer;

    // Build a valid process response object like the one returned from the server after a successful load request
    const serverProcess = {
      id,
      version,
      taskType,
      template,
      draft: null,
    };

    // Inject missing fields
    serverProcess.definition = JSON.parse(draft);
    serverProcess.id = id;
    serverProcess.version = version;
    serverProcess.taskType = taskType;
    serverProcess.template = template;
    serverProcess.draft = null;

    const clientProcess = processService.readProcessResponse(serverProcess);

    // Since draft is saved as a string, no server normalization is applied; Hence
    // we have to apply it on the client

    // Remove empty groups
    const groups = _.sortedUniq(clientProcess.steps.map(s => s.group));
    clientProcess.steps.forEach(s => {
      s.group = groups.indexOf(s.group) + (groups[0] === 0 ? 0 : 1); // +1 for handling the case that no TripleGeo steps exist
    });


    dispatch(processLoaded(clientProcess, readOnly, clone, config));
    dispatch(restoreDraftSuccess());

    return true;
  };
}

export function rejectDraft() {
  return (dispatch, getState) => {
    dispatch(restoreDraftSuccess());

    return true;
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

const fileDownloaded = function (fileId, fileName) {
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

    dispatch(fileDownloaded(fileId, fileName));

    return Promise.resolve();
  };
};

const logExists = function (fileId, fileName) {
  return {
    type: Types.LOG_EXISTS_RESPONSE,
    fileId,
    fileName,
  };
};

export const checkLog = (id, version, executionId, fileId, fileName) => {
  return (dispatch, getState) => {
    const { meta: { csrfToken: token } } = getState();
    const url = `/action/process/${id}/${version}/execution/${executionId}/log/${fileId}/exists`;

    return file.exists(url, token)
      .then(() => {
        dispatch(logExists(fileId, fileName));
      });
  };
};

const logDownloaded = function (fileId, fileName) {
  return {
    type: Types.LOG_DOWNLOAD_RESPONSE,
    fileId,
    fileName,
  };
};

export const downloadLog = (id, version, executionId, fileId, fileName) => {
  return (dispatch, getState) => {
    const url = `/action/process/${id}/${version}/execution/${executionId}/log/${fileId}/download`;

    dom.downloadUrl(url, fileName);

    dispatch(logDownloaded(fileId, fileName));

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
