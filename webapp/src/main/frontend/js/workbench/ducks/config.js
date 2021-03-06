import configurationService from '../service/configuration';
import filesystemService from '../service/filesystem';

// Actions
import { LOGOUT } from './user';

const REQUEST_CONFIGURATION = 'config/REQUEST_CONFIGURATION';
const LOAD_CONFIGURATION = 'config/LOAD_CONFIGURATION';

const REQUEST_FILESYSTEM = 'config/REQUEST_FILESYSTEM';
const RECEIVE_FILESYSTEM = 'config/RECEIVE_FILESYSTEM';

const initialState = {
  filesystem: {
    files: [],
  },
};

// Reducer
export default (state = initialState, action) => {
  switch (action.type) {
    case LOGOUT:
      return initialState;

    case LOAD_CONFIGURATION:
      return {
        ...state,
        ...action.configuration,
      };

    case RECEIVE_FILESYSTEM:
      return {
        ...state,
        filesystem: action.filesystem,
      };

    default:
      return state;

  }
};

// Action Creators
export const requestConfiguration = () => ({
  type: REQUEST_CONFIGURATION,
});

export const receiveConfiguration = (configuration) => ({
  type: LOAD_CONFIGURATION,
  configuration,
});

export const requestFilesystem = () => ({
  type: REQUEST_FILESYSTEM,
});

export const receiveFilesystem = (filesystem) => ({
  type: RECEIVE_FILESYSTEM,
  filesystem,
});

// Thunk actions
export const getConfiguration = () => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();

  dispatch(requestConfiguration());
  return configurationService.getConfiguration(token)
    .then((configuration) => {
      dispatch(receiveConfiguration(configuration));
    })
    .catch(() => {

    });
};

export const getFilesystem = () => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();

  dispatch(requestFilesystem());
  return filesystemService.fetch(token)
    .then((fs) => {
      dispatch(receiveFilesystem(fs));
    });
};

export const createFolder = (path) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();

  return filesystemService.createFolder(path, token)
    .then((fs) => {
      dispatch(receiveFilesystem(fs));
    });
};

export const uploadFile = (data, file) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();

  return filesystemService.upload(data, file, token)
    .then((fs) => {
      dispatch(receiveFilesystem(fs));
    });
};

export const deletePath = (path) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();

  return filesystemService.deletePath(path, token)
    .then((fs) => {
      dispatch(receiveFilesystem(fs));
    });
};
