import adminService from '../../../service/admin';
import applicationKeyService from '../../../service/application-key';

// Actions
import { LOGOUT } from '../../user';

const SET_PAGER = 'ui/application-key/SET_PAGER';
const RESET_PAGER = 'ui/application-key/RESET_PAGER';
const SET_FILTER = 'ui/application-key/SET_FILTER';
const RESET_FILTERS = 'ui/application-key/RESET_FILTERS';

const REQUEST_APPLICATION_KEYS = 'ui/application-key/REQUEST_APPLICATION_KEYS';
const RECEIVE_APPLICATION_KEYS = 'ui/application-key/RECEIVE_APPLICATION_KEYS';
const SET_SELECTED_APPLICATION_KEY = 'ui/application-key/SET_SELECTED_APPLICATION_KEY';
const EXPAND_SELECTED_APPLICATION_KEY = 'ui/application-key/EXPAND_SELECTED_APPLICATION_KEY';

const REQUEST_ACCOUNTS = 'ui/application-key/REQUEST_ACCOUNTS';
const RECEIVE_ACCOUNTS = 'ui/application-key/RECEIVE_ACCOUNTS';

const REQUEST_NEW_APPLICATION_KEY = 'ui/application-key/REQUEST_NEW_APPLICATION_KEY';
const RECEIVE_NEW_APPLICATION_KEY = 'ui/application-key/RECEIVE_NEW_APPLICATION_KEY';

const REQUEST_APPLICATION_KEY_REVOKE = 'ui/application-key/REQUEST_NEW_APPLICATION_KEY';
const RECEIVE_APPLICATION_KEY_REVOKE = 'ui/application-key/RECEIVE_NEW_APPLICATION_KEY';

// Reducer
const initialState = {
  accounts: [],
  expanded: {
    index: null,
    reason: null,
  },
  filters: {
    applicationName: null,
    revoked: false,
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
  users: [],
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
      let value = action.value;

      if (action.filter === 'revoked') {
        switch (action.value) {
          case 'REVOKED':
            value = true;
            break;
          case 'ACTIVE':
            value = false;
            break;
          default:
            value = null;
            break;
        }
      }

      return {
        ...state,
        filters: {
          ...state.filters,
          [action.filter]: value,
        }
      };
    }

    case RESET_FILTERS:
      return {
        ...state,
        filters: {
          ...initialState.filters,
        },
      };

    case REQUEST_ACCOUNTS:
      return {
        ...state,
        loading: true,
        accounts: [],
      };

    case RECEIVE_ACCOUNTS:
      return {
        ...state,
        loading: false,
        accounts: action.accounts,
      };

    case REQUEST_APPLICATION_KEYS:
      return {
        ...state,
        loading: true,
        selected: null,
      };

    case RECEIVE_APPLICATION_KEYS:
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

    case SET_SELECTED_APPLICATION_KEY:
      return {
        ...state,
        expanded: {
          index: null,
          reason: null,
        },
        selected: action.id,
      };

    case EXPAND_SELECTED_APPLICATION_KEY: {
      return {
        ...state,
        expanded: {
          index: action.index,
          reason: action.reason,
        }
      };
    }

    case REQUEST_NEW_APPLICATION_KEY:
      return {
        ...state,
        loading: true,
      };

    case RECEIVE_NEW_APPLICATION_KEY:
      return {
        ...state,
        loading: false,
      };

    case REQUEST_APPLICATION_KEY_REVOKE:
      return {
        ...state,
        loading: true,
      };

    case RECEIVE_APPLICATION_KEY_REVOKE:
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
  type: SET_SELECTED_APPLICATION_KEY,
  id,
});

export const setExpanded = (index, reason) => ({
  type: EXPAND_SELECTED_APPLICATION_KEY,
  index,
  reason,
});

// Thunk actions
const requestAccounts = () => ({
  type: REQUEST_ACCOUNTS,
});

const receiveAccounts = (accounts) => ({
  type: RECEIVE_ACCOUNTS,
  accounts,
});

export const getAccounts = () => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(requestAccounts());

  const data = {
    pagingOptions: {
      pageIndex: 0,
      pageSize: 1000,
    },
    query: {
      userName: null,
    },
  };

  return adminService.getAccounts(data, token)
    .then((result) => {
      dispatch(receiveAccounts(result.items));
    })
    .catch((err) => {
      console.error('Failed loading user accounts:', err);
    });
};

const requestApplicationKeys = () => ({
  type: REQUEST_APPLICATION_KEYS,
});

const receiveApplicationKeys = (result) => ({
  type: RECEIVE_APPLICATION_KEYS,
  result,
});

export const query = (query) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(requestApplicationKeys());

  return applicationKeyService.query(query, token)
    .then((result) => {
      dispatch(receiveApplicationKeys(result));
    })
    .catch((err) => {
      console.error('Failed loading application keys:', err);
    });
};

const requestNewApplicationKey = () => ({
  type: REQUEST_NEW_APPLICATION_KEY,
});

const receiveNewApplicationKey = () => ({
  type: RECEIVE_NEW_APPLICATION_KEY,
});

export const create = (data) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(requestNewApplicationKey());

  return applicationKeyService.create(data, token)
    .then((result) => {
      dispatch(receiveNewApplicationKey(result));

      return result;
    });
};

const requestRevokeApplicationKey = () => ({
  type: REQUEST_NEW_APPLICATION_KEY,
});

const receiveRevokeApplicationKey = () => ({
  type: RECEIVE_NEW_APPLICATION_KEY,
});

export const revoke = (id) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(requestRevokeApplicationKey());

  return applicationKeyService.revoke(id, token)
    .then((result) => {
      dispatch(receiveRevokeApplicationKey(result));
    });
};

export const checkApplicationName = (name) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();

  // Do not update state

  return applicationKeyService.checkApplicationName(name, token);
};
