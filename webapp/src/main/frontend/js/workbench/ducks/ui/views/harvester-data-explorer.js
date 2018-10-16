import actions from '../../../service/api/fetch-actions';

// Actions
import { LOGOUT } from '../../user';

const SIMPLE_ACTION = 'ui/harvester/data/explorer/SIMPLE_ACTION';
const THUNK_BEFORE = 'ui/harvester/data/explorer/THUNK_BEFORE';
const THUNK_AFTER = 'ui/harvester/data/explorer/THUNK_AFTER';

// Reducer
const initialState = {
  data: {
    string: 'Test',
    number: '1',
    series: [{
      label: 'a',
      value: '1',
    }, {
      label: 'b',
      value: '2',
    }],
  },
};

export default (state = initialState, action) => {
  switch (action.type) {
    case LOGOUT:
      return initialState;

    case SIMPLE_ACTION:
      return {
        ...state,
        data: action.data,
      };

    default:
      return state;
  }
};

// Action creators
export const action = (data) => ({
  type: SIMPLE_ACTION,
  data,
});

// Thunk actions
const beforeThunk = () => ({
  type: THUNK_BEFORE,
});

const afterThunk = (data) => ({
  type: THUNK_AFTER,
  data,
});

export const thunk = () => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();

  dispatch(beforeThunk());
  return actions.get('/url/to/my/data.json', token)
    .then((data) => {
      dispatch(afterThunk(data));
    })
    .catch((err) => {
      console.error('Error message: ', err);
    });
};
