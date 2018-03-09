// viewport.js

import * as Immutable from 'immutable';

// Actions
const TOGGLE_SIDEBAR = 'menu/TOGGLE_SIDEBAR';
const TOGGLE_MENU = 'menu/TOGGLE_MENU';

// Reducer
const initialState = {
  expanded: new Immutable.Set(),
  sidebarOpen: true,
};


export default (state = initialState, action) => {
  switch (action.type) {
    case TOGGLE_SIDEBAR:
      return {
        ...state,
        sidebarOpen: !state.sidebarOpen,
      };

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
export const toggleSidebar = () => ({
  type: TOGGLE_SIDEBAR,
});

export const toggleMenu = (item) => ({
  type: TOGGLE_MENU,
  item,
});

