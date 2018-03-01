// dashboard.js
import dashboardService from '../../../service/dashboard';
// Actions
const REQUEST_DASHBOARD_DATA = 'ui/dashboard/REQUEST_DASHBOARD_DATA';
const RECEIVE_DASHBOARD_DATA = 'ui/dashboard/RECEIVE_DASHBOARD_DATA';
const CHANGE_CARD_FILTER = 'ui/dashboard/CHANGE_CARD_FILTER';


// Reducer
const initialState = {
  filters:{
    resources: "all",
    events: "ALL",
    processExplorer:"allProcess",

  },
  processes:[],
  resources:[],
  events: [],
  statistics:{
    resources:{
      created: 0,
      total: 0,
      updated: 0,
      updatedOn: null,
    },
    events:{
      error:0,
      information:0,
      warning:0,
      updatedOn: null,
    },
    processes:{
      completed:0,
      running:0,
      failed:0,
      updateOn:null,
    },
    system:{
      usedCores:0,
      totalCores:0,
      usedMemory:0,
      totalMemory:0,
      usedDisk:0,
      totalDisk:0,
      updateOn:null,
    }

  }

};

export default (state = initialState, action) => {
  switch (action.type) {
    case RECEIVE_DASHBOARD_DATA:
      return {
        ...state,
        ...action.data,
        statistics:{
          ...action.data.statistics,
          resources:{
            ...action.data.statistics.resources,
            created:action.data.statistics.resources.created
          }
        }
      };
    case CHANGE_CARD_FILTER:
      return {
        ...state,
        filters:{
          ...state.filters,
          [action.cardName]: action.selection,
        },
      };
    default:
      return state;
  }
};

// Action creators
const requestDashboardData = () => ({
  type: REQUEST_DASHBOARD_DATA,
});

const receiveDashboardData = (data) => ({
  type: RECEIVE_DASHBOARD_DATA,
  data,
});

export const changeDashboardFilter = (cardName, selection) => ({
  type: CHANGE_CARD_FILTER,
  cardName,
  selection,
});


// Thunk actions
export const fetchDashboardData = () => (dispatch, getState) => {
  const { meta: { csrfToken: token } } = getState();
  dispatch(requestDashboardData());
  return dashboardService.fetch(token)
    .then((data) => {
      dispatch(receiveDashboardData(data));
    })
    .catch((err) => {
      console.error('Failed loading resources:', err);
    });
};
