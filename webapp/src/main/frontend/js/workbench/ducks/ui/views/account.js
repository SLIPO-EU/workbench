import adminService from '../../../service/admin';

// Actions
import { LOGOUT } from '../../user';

const SET_PAGER = 'ui/accounts/SET_PAGER';
const RESET_PAGER = 'ui/accounts/RESET_PAGER';
const SET_FILTER = 'ui/accounts/SET_FILTER';
const RESET_FILTERS = 'ui/accounts/RESET_FILTERS';

const REQUEST_ACCOUNT_DATA = 'ui/accounts/REQUEST_ACCOUNT_DATA';
const RECEIVE_ACCOUNT_DATA = 'ui/accounts/RECEIVE_ACCOUNT_DATA';
const SET_SELECTED_ACCOUNT = 'ui/accounts/SET_SELECTED_ACCOUNT';
const RESET_SELECTED_ACCOUNT = 'ui/accounts/RESET_SELECTED_ACCOUNT';

const ACCOUNT_CREATE_INIT = 'ui/accounts/ACCOUNT_CREATE_INIT';
const ACCOUNT_CREATE_COMPLETE = 'ui/accounts/ACCOUNT_CREATE_COMPLETE';

const ACCOUNT_UPDATE_INIT = 'ui/accounts/ACCOUNT_UPDATE_INIT';
const ACCOUNT_UPDATE_COMPLETE = 'ui/accounts/ACCOUNT_UPDATE_INIT';

// Reducer
const initialState = {
  filters: {
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

    case REQUEST_ACCOUNT_DATA:
      return {
        ...state,
        loading: true,
        selected: null,
      };

    case RECEIVE_ACCOUNT_DATA:
      return {
        ...state,
        items: action.result.items,
        lastUpdate: new Date(),
        loading: false,
        pager: {
          count: action.result.pagingOptions.count,
          index: action.result.pagingOptions.pageIndex,
          size: action.result.pagingOptions.pageSize,
        },
      };

    case SET_SELECTED_ACCOUNT:
      return {
        ...state,
        selected: {
          id: action.id,
        },
      };

    case RESET_SELECTED_ACCOUNT:
      return {
        ...state,
        selected: null,
      };

    case ACCOUNT_CREATE_INIT:
      return {
        ...state,
        loading: true,
        selected: null,
      };

    case ACCOUNT_CREATE_COMPLETE:
      return {
        ...state,
        loading: false,
      };

    case ACCOUNT_UPDATE_INIT:
      return {
        ...state,
        loading: true,
      };

    case ACCOUNT_UPDATE_COMPLETE:
      return {
        ...state,
        loading: false,
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
  type: SET_SELECTED_ACCOUNT,
  id,
});

export const resetSelected = () => ({
  type: RESET_SELECTED_ACCOUNT,
});

// Thunk actions
const requestAccountData = () => ({
  type: REQUEST_ACCOUNT_DATA,
});

const receiveAccountData = (result) => ({
  type: RECEIVE_ACCOUNT_DATA,
  result,
});

export const fetchAccounts = (query) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(requestAccountData());

  return adminService.getAccounts(query, token)
    .then((result) => {
      dispatch(receiveAccountData(result));
    })
    .catch((err) => {
      console.error('Failed loading accounts:', err);
    });
};

const createAccountInit = () => ({
  type: ACCOUNT_CREATE_INIT,
});

const createAccountComplete = () => ({
  type: ACCOUNT_CREATE_COMPLETE,
});

export const createAccount = (account) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(createAccountInit());

  return adminService.createAccount(account, token)
    .then((result) => {
      dispatch(createAccountComplete(result));
    });
};

const updateAccountInit = () => ({
  type: ACCOUNT_UPDATE_INIT,
});

const updateAccountComplete = () => ({
  type: ACCOUNT_UPDATE_COMPLETE,
});

export const updateAccount = (account) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(updateAccountInit());

  return adminService.updateAccount(account, token)
    .then((result) => {
      dispatch(updateAccountComplete(result));
    });
};
