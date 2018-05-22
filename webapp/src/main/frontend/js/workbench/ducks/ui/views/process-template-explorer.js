import * as processService from '../../../service/process';

// Actions
const SET_PAGER = 'ui/process/template/explorer/SET_PAGER';
const RESET_PAGER = 'ui/process/template/explorer/RESET_PAGER';
const SET_FILTER = 'ui/process/template/explorer/SET_FILTER';
const RESET_FILTERS = 'ui/process/template/explorer/RESET_FILTERS';

const REQUEST_TEMPLATES = 'ui/process/template/explorer/REQUEST_TEMPLATES';
const RECEIVE_TEMPLATES = 'ui/process/template/explorer/RECEIVE_TEMPLATES';
const SET_SELECTED_TEMPLATE = 'ui/process/template/explorer/SET_SELECTED_TEMPLATE';
const RESET_SELECTED_TEMPLATE = 'ui/process/template/explorer/RESET_SELECTED_TEMPLATE';

// Initial state
const initialState = {
  filters: {
    name: null,
  },
  pager: {
    index: 0,
    size: 10,
    count: 0,
  },
  items: [],
  selected: null,
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
      };

    case RESET_PAGER:
      return {
        ...state,
        pager: {
          ...initialState.pager
        },
        selected: null,
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
      };

    case REQUEST_TEMPLATES:
      return {
        ...state,
        selected: null,
      };

    case RECEIVE_TEMPLATES:
      return {
        ...state,
        items: action.result.items.map((p) => {
          return {
            ...p,
            revisions: p.revisions.sort((v1, v2) => v2.version - v1.version),
          };
        }),
        pager: {
          index: action.result.pagingOptions.pageIndex,
          size: action.result.pagingOptions.pageSize,
          count: action.result.pagingOptions.count,
        },
        lastUpdate: new Date(),
      };

    case SET_SELECTED_TEMPLATE:
      return {
        ...state,
        selected: {
          id: action.id,
          version: action.version,
        },
      };

    case RESET_SELECTED_TEMPLATE:
      return {
        ...state,
        selected: null,
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

export const setSelected = (id, version) => ({
  type: SET_SELECTED_TEMPLATE,
  id,
  version,
});

export const resetFilters = () => ({
  type: RESET_FILTERS,
});

export const resetSelected = () => ({
  type: RESET_SELECTED_TEMPLATE,
});

// Thunk actions
const requestTemplateData = () => ({
  type: REQUEST_TEMPLATES,
});

const receiveTemplateData = (result) => ({
  type: RECEIVE_TEMPLATES,
  result,
});

export const fetchTemplates = (query) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(requestTemplateData());

  return processService.fetchTemplates(query, token)
    .then((result) => {
      dispatch(receiveTemplateData(result));
    })
    .catch((err) => {
      console.error('Failed loading process templates:', err);
    });
};
