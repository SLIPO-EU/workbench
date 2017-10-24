// config.js
import filesystemService from '../service/filesystem';

// Actions
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
    case REQUEST_CONFIGURATION:
      return state;
    case LOAD_CONFIGURATION:
      return action.config;
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

export const loadConfiguration = (config) => ({
  type: LOAD_CONFIGURATION,
  config,
});

export const requestFilesystem = () => ({
  type: REQUEST_FILESYSTEM,
});

export const receiveFilesystem = (filesystem) => ({
  type: RECEIVE_FILESYSTEM,
  filesystem,
});

// Thunk actions
export const getConfiguration = () => (dispatch) => {
  // Request and load configuration from server
  dispatch(requestConfiguration());
  // Todo fetch and load
};

export const getFilesystem = (path) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();

  dispatch(requestFilesystem());
  return filesystemService.fetch(path, token)
    .then((fs) => {
      dispatch(receiveFilesystem(fs));
    })
    .catch((err) => {
      console.error('Error receiving filesystem', err);
    });
};
