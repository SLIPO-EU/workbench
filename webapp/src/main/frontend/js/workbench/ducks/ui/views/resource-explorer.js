import * as resourceService from '../../../service/resources';

// Actions
const SET_PAGER = 'ui/resource/explorer/SET_PAGER';
const RESET_PAGER = 'ui/resource/explorer/RESET_PAGER';
const SET_FILTER = 'ui/resource/explorer/SET_FILTER';
const RESET_FILTERS = 'ui/resource/explorer/RESET_FILTERS';
const REQUEST_RESOURCES = 'ui/resource/explorer/REQUEST_RESOURCES';
const RECEIVE_RESOURCES = 'ui/resource/explorer/RECEIVE_RESOURCES';
const SET_SELECTED = 'ui/resource/explorer/SET_SELECTED';
const RESET_SELECTED = 'ui/resource/explorer/RESET_SELECTED';

// Helpers
function toFeatures(resources) {
  if (!resources) {
    return [];
  }

  return resources
    .filter((r) => {
      return (r !== null);
    })
    .map((r, index) => {
      if (r.boundingBox) {
        return {
          type: 'Feature',
          geometry: {
            ...r.boundingBox,
          },
          properties: {
            id: r.id,
            version: r.version,
            name: r.metadata.name,
            description: r.metadata.description,
            size: r.numberOfEntities,
          }
        };
      }
      return null;
    }).filter((r) => {
      return (r !== null);
    });
}

function toFeatureCollection(features) {
  return {
    type: 'FeatureCollection',
    crs: {
      type: 'name',
      properties: {
        name: 'EPSG:4326'
      }
    },
    features: features || [],
    _lastUpdate: new Date(),
  };
}

function findResource(items, id, version) {
  if (!items) {
    return null;
  }
  return items.find((r) => r.id === id) || null;
}

// Initial state
const initialState = {
  filters: {
    name: null,
    description: null,
    format: null,
    bbox: null,
  },
  pager: {
    index: 0,
    size: 10,
    count: 0,
  },
  items: [],
  features: null,
  selected: null,
  selectedFeatures: null,
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
        selectedFeatures: null,
      };

    case RESET_PAGER:
      return {
        ...state,
        pager: {
          ...initialState.pager
        },
        selected: null,
        selectedFeatures: null,
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
        selectedFeatures: null,
      };

    case REQUEST_RESOURCES:
      return {
        ...state,
        selected: null,
        selectedFeatures: null,
      };

    case RECEIVE_RESOURCES:
      return {
        ...state,
        items: action.result.items.map((r) => {
          return {
            ...r,
            revisions: r.revisions.sort((v1, v2) => v2.version - v1.version),
          };
        }),
        features: toFeatureCollection(toFeatures(action.result.items)),
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
        selected: {
          id: action.id,
          version: action.version,
        },
        selectedFeatures: toFeatureCollection(toFeatures([findResource(state.items, action.id)])),
      };

    case RESET_SELECTED:
      return {
        ...state,
        selected: null,
        selectedFeatures: null,
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

const requestResources = () => ({
  type: REQUEST_RESOURCES,
});

const receiveResources = (result) => ({
  type: RECEIVE_RESOURCES,
  result,
});

export const setSelectedResource = (id, version) => ({
  type: SET_SELECTED,
  id,
  version,
});

export const resetSelectedResource = () => ({
  type: RESET_SELECTED,
});


// Thunk actions
export const fetchResources = (query) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(requestResources());

  return resourceService.fetch(query, token)
    .then((result) => {
      dispatch(receiveResources(result));
    })
    .catch((err) => {
      console.error('Failed loading resources:', err);
    });
};

export const createResource = (data, file = null) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();

  if (file != null) {
    return resourceService.upload(data, file, token);
  } else {
    return resourceService.register(data, token);
  }
};
