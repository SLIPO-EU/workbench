import processService from '../../../service/processExplorer';
// Actions
const REQUEST_PROCESS_DATA = 'ui/processExplorer/REQUEST_PROCESS_DATA';
const RECEIVE_PROCESS_DATA = 'ui/processExplorer/RECEIVE_PROCESS_DATA';
const SET_PAGER = 'ui/processExplorer/SET_PAGER';
const SET_SELECTED = 'ui/processExplorer/SET_SELECTED';
const RESET_SELECTED = 'ui/processExplorer/RESET_SELECTED';



// Reducer
const initialState = {
  items:[],
  pagingOptions: {
    count: 0,
    pageIndex: 0,
    pageSize: 10,
  },
  selected: null,

};

export default (state = initialState, action) => {
  switch (action.type) {
    case RECEIVE_PROCESS_DATA:
      return {
        ...state,
        ...action.data,
      }; 
    case SET_PAGER:
      return {
        ...state, 
        pagingOptions: {
          ...state.pagingOptions,
          ...action.pager,
        },
      };  
    case SET_SELECTED:
      return {
        ...state,
        selected: action.selected,
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
const requestProcessData = () => ({
  type: REQUEST_PROCESS_DATA,
});
  
const receiveProcessData = (data) => ({
  type: RECEIVE_PROCESS_DATA,
  data,
});

export const setPager = (pager) => ({
  type: SET_PAGER,
  pager,
});

export const setSelectedProcess = (selected) => ({
  type: SET_SELECTED,
  selected,
});

export const resetSelectedProcess = () => ({
  type: RESET_SELECTED,
});


// Thunk actions
export const fetchProcessData = (options) => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(requestProcessData());
  const tt= { 
    name: "Link",
    pagingOptions: {
      pageIndex: 0,
      pageSize: 10,
      ...options
    },
    };
    
  return processService.fetch(tt)
    .then((data) => {
      dispatch(receiveProcessData(data));
    })
    .catch((err) => {
      console.error('Failed loading processes:', err);
    });
};
