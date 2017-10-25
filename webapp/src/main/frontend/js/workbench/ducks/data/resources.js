// resources.js
const resourceService = require('../../service/resources');

// Actions
export const REQUEST_RESOURCES = 'resources/REQUEST_RESOURCES';
export const RECEIVE_RESOURCES = 'resources/RECEIVE_RESOURCES';
const ADD_RESOURCE = 'resources/ADD_RESOURCE';
const UPDATE_RESOURCE = 'resources/UPDATE_RESOURCE';
const REMOVE_RESOURCE = 'resources/REMOVE_RESOURCE';


// Action Creators

const requestResources = (index, offset) => ({
  type: REQUEST_RESOURCES,
  index,
  offset,
});

const receiveResources = (resources) => ({
  type: RECEIVE_RESOURCES,
  resources,
});

const addResource = (id, data) => ({
  type: ADD_RESOURCE,
  id,
  data,
});

const updateResource = (id, data) => ({
  type: UPDATE_RESOURCE,
  id,
  data,
});

const removeResource = (id) => ({
  type: REMOVE_RESOURCE,
  id,
});

// Thunk actions
export const fetchResources = (data) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(requestResources());
  return resourceService.fetch(data, token)
    .then((resources) => {
      dispatch(receiveResources(resources));
    })
    .catch((err) => {
      console.error('Failed loading resources:', err);
    });
};

export const createResource = (data, file = null) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();

  if (file != null) {
    return resourceService.upload(data, file, token)
      .then((resource) => {
      })
      .catch((err) => {
        console.error('Failed uploading resource:', err);
      });
  } else {
    return resourceService.register(data, token)
      .then((resource) => {
      })
      .catch((err) => {
        console.error('Failed creating resource:', err);
      });
  }
};

// Reducer
const initialState = [];

export default (state = initialState, action) => {
  switch (action.type) {
    case REQUEST_RESOURCES:
      return state; // no-op  
    case RECEIVE_RESOURCES:
      return action.resources;
    default:
      return state;
  }
};
