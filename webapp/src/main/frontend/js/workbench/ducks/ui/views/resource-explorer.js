import * as resourceService from '../../../service/resources';

// Actions
const SET_PAGER = 'ui/resource/explorer/SET_PAGER';
const RESET_PAGER = 'ui/resource/explorer/RESET_PAGER';
const SET_FILTER = 'ui/resource/explorer/SET_FILTER';
const RESET_FILTERS = 'ui/resource/explorer/RESET_FILTERS';
const FIND_ONE_INIT = 'ui/resource/explorer/FIND_ONE_INIT';
const FIND_ONE_SUCCESS = 'ui/resource/explorer/FIND_ONE_SUCCESS';
const SEARCH_INIT = 'ui/resource/explorer/SEARCH_INIT';
const SEARCH_SUCCESS = 'ui/resource/explorer/SEARCH_SUCCESS';
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

function findResource(items, id) {
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
  resource: null,
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

    case FIND_ONE_INIT:
      return {
        ...state,
        resource: null,
      };

    case FIND_ONE_SUCCESS:
      return {
        ...state,
        resource: action.result.resource || null,
      };

    case SEARCH_INIT:
      return {
        ...state,
        selected: null,
        selectedFeatures: null,
        resource: null,
      };

    case SEARCH_SUCCESS:
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

const findOneInit = () => ({
  type: FIND_ONE_INIT,
});

const findOneSuccess = (result) => ({
  type: FIND_ONE_SUCCESS,
  result,
});

const searchInit = () => ({
  type: SEARCH_INIT,
});

const searchSuccess = (result) => ({
  type: SEARCH_SUCCESS,
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
export const findOne = (id, version) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(findOneInit());

  return resourceService.findOne(id, version, token)
    .then((result) => {
      dispatch(findOneSuccess(result));
    })
    .catch((err) => {
      console.error('Failed loading resources:', err);
    });
};


export const search = (query) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(searchInit());

  return resourceService.find(query, token)
    .then((result) => {
      dispatch(searchSuccess(result));
    })
    .catch((err) => {
      console.error('Failed loading resources:', err);
    });
};

export const create = (data, file = null) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();

  if (file != null) {
    return resourceService.upload(data, file, token);
  } else {
    return resourceService.register(data, token);
  }
};
