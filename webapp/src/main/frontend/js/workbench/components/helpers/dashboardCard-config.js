

export const DashboardProcessExplorerConfig = {
  name: 'Workflow Explorer',
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
      id:"ALL",
      name:"All",
    },
    {
      id:"ERROR",
      name:"Error",
    },
    {
      id:"WARNING",
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
