export const DashboardProcessExplorerConfig = {
  name: 'Executions',
  cardName: 'processExplorer',
  cardFilters: [{
    id: "allProcess",
    name: "All",
  },
  {
    id: "completed",
    name: "Completed",
  },
  {
    id: "running",
    name: "Running",
  },
  {
    id: "failed",
    name: "Failed",
  }],
  footer: 'Workflow executions for the last 7 days'
};

export const DashboardEventsConfig = {
  name: 'Events',
  cardName: 'events',
  cardFilters: [{
    id: "ALL",
    name: "All",
  },
  {
    id: "ERROR",
    name: "Error",
  },
  {
    id: "WARNING",
    name: "Warning",
  },
  {
    id: "INFO",
    name: "Information",
  }],
  footer: '50 most recent system events'
};

export const DashboardResourcesConfig = {
  name: 'Resources',
  cardName: 'resources',
  cardFilters: [{
    id: "all",
    name: "All",
  },
  {
    id: "new",
    name: "New",
  },
  {
    id: "updated",
    name: "Updated",
  }],
  footer: 'New or updated resources for the last 7 days'
};
