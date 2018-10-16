import * as Immutable from 'immutable';

// Actions
import { LOGOUT } from '../user';

const TOGGLE_MENU = 'menu/TOGGLE_MENU';

// Reducer
const initialState = {
  expanded: new Immutable.Set(),
};


export default (state = initialState, action) => {
  switch (action.type) {
    case LOGOUT:
      return initialState;

    case TOGGLE_MENU:
      return {
        ...state,
        expanded: state.expanded.has(action.item) ?
          state.expanded.remove(action.item)
          : state.expanded.add(action.item),
      };

    default:
      return state;
  }
};

// Action creators
export const toggleMenu = (item) => ({
  type: TOGGLE_MENU,
  item,
});

