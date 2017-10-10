// viewport.js

import * as Immutable from 'immutable';

// Actions
const TOGGLE = 'menu/TOGGLE';

// Reducer
const initialState = {
  expanded: new Immutable.Set(),
};


export default (state = initialState, action) => {
  switch (action.type) {
    case TOGGLE:
      return {
        expanded: state.expanded.has(action.item) ? 
          state.expanded.remove(action.item) 
          : state.expanded.add(action.item),
      };
    
    default:
      return state;
  }
};

// Action creators
export const toggle = (item) => ({
  type: TOGGLE,
  item,
});

