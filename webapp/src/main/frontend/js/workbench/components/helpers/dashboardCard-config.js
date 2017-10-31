

export const DashboardProcessExplorerConfig = {
  name: 'Process Explorer',
  cardname: 'processExplorer',
  changedOn: new Date(),
  cardFilters: [
    {
      id:"allProcess",
      name:"All",
    },
    {
      id:"completed",
      name:"Completed",
    }, 
    {
      id:"running",
      name:"Running",
    },
    {
      id:"failed",
      name:"Failed",
    }],
};


export const DashboardEventsConfig = {  
  name: 'Events',
  cardname: 'events',
  changedOn: new Date(),
  cardFilters: [
    {
      id:"allEvents",
      name:"All",
    },
    {
      id:'error',
      name:"Error",
    }, 
    {
      id:'warning',
      name:"Warning",
    }],
};

export const DashboardResourcesConfig = {    
  name: 'Resources',
  cardname: 'resources',
  changedOn: new Date(),
  cardFilters: [
    {
      id:"all",
      name:"All",
    },
    {
      id:"new",
      name:"New",
    }, 
    {
      id:"updated",
      name:"Updated",
    }],
};