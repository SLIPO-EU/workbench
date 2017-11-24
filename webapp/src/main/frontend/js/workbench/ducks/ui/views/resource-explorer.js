// viewport.js

// Actions
const SET_PAGER = 'ui/resource-explorer/SET_PAGER';
const RESET_PAGER = 'ui/resources/RESET_PAGER';
const SET_FILTER = 'ui/resources/SET_FILTER';
const RESET_FILTERS = 'ui/resources/RESET_FILTERS';
const SET_SELECTED = 'ui/resources/SET_SELECTED';
const RESET_SELECTED = 'ui/resources/RESET_SELECTED';

// Reducer
const initialState = {
  pager: {
    index: 0,
    size: 10,
    count: 0,
  },
  filters: {
    name: null,
    description: null,
    format: null,
    bbox: null,
  },
  selected: null,
  version: null,
};

export default (state = initialState, action) => {
  switch (action.type) {
    case SET_PAGER:
      return {
        ...state, 
        pager: {
          ...state.pager,
          ...action.pager,
        },
      };

    case RESET_PAGER:
      return { 
        ...state,
        pager: initialState.pager,
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
        filters: initialState.filters,
      };

    case SET_SELECTED:
      return {
        ...state,
        selected: action.selected,
        version: action.version,
      };

    case RESET_SELECTED:
      return {
        ...state,
        selected: initialState.selected,
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

export const setSelectedResource = (selected, version) => ({
  type: SET_SELECTED,
  selected,
  version,
});

export const resetSelectedResource = () => ({
  type: RESET_SELECTED,
});

