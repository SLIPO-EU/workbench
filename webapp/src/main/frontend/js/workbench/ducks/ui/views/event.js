import adminService from '../../../service/admin';

// Actions
import { LOGOUT } from '../../user';

const SET_PAGER = 'ui/events/SET_PAGER';
const RESET_PAGER = 'ui/events/RESET_PAGER';
const SET_FILTER = 'ui/events/SET_FILTER';
const RESET_FILTERS = 'ui/events/RESET_FILTERS';

const REQUEST_EVENT_DATA = 'ui/events/REQUEST_EVENT_DATA';
const RECEIVE_EVENT_DATA = 'ui/events/RECEIVE_EVENT_DATA';
const SET_SELECTED_EVENT = 'ui/events/SET_SELECTED_EVENT';
const RESET_SELECTED_EVENT = 'ui/events/RESET_SELECTED_EVENT';

// Reducer
const initialState = {
  filters: {
    level: null,
    maxDate: null,
    minDate: null,
    userName: null,
  },
  items: [],
  lastUpdate: null,
  loading: false,
  pager: {
    count: 0,
    index: 0,
    size: 10,
  },
  selected: null,
};

// Reducer
export default (state = initialState, action) => {
  switch (action.type) {
    case LOGOUT:
      return initialState;

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
        pager: {
          ...initialState.pager
        },
      };

    case SET_FILTER: {
      return {
        ...state,
        filters: {
          ...state.filters,
          [action.filter]: action.value,
        }
      };
    }

    case RESET_FILTERS:
      return {
        ...state,
        filters: {
          ...initialState.filters
        },
      };

    case REQUEST_EVENT_DATA:
      return {
        ...state,
        loading: true,
        selected: null,
      };

    case RECEIVE_EVENT_DATA:
      return {
        ...state,
        items: action.result.items.map((e) => {
          return {
            ...e,
            userName: !e.userName ? '-' : e.userName,
          };
        }),
        lastUpdate: new Date(),
        loading: false,
        pager: {
          count: action.result.pagingOptions.count,
          index: action.result.pagingOptions.pageIndex,
          size: action.result.pagingOptions.pageSize,
        },
      };

    case SET_SELECTED_EVENT:
      return {
        ...state,
        selected: {
          id: action.id,
        },
      };

    case RESET_SELECTED_EVENT:
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

export const resetFilters = () => ({
  type: RESET_FILTERS,
});

export const setSelected = (id) => ({
  type: SET_SELECTED_EVENT,
  id,
});

export const resetSelected = () => ({
  type: RESET_SELECTED_EVENT,
});

// Thunk actions
const requestEventData = () => ({
  type: REQUEST_EVENT_DATA,
});

const receiveEventData = (result) => ({
  type: RECEIVE_EVENT_DATA,
  result,
});

export const fetchEvents = (query) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(requestEventData());

  return adminService.getEvents(query, token)
    .then((result) => {
      dispatch(receiveEventData(result));
    })
    .catch((err) => {
      console.error('Failed loading events:', err);
    });
};
