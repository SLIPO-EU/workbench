import { StaticRoutes } from '../../model/routes';

export const JobCardConfig = {
  title: 'dashboard.card.jobs',
  items: [{
    value: 2,
    label: 'Completed',
  }, {
    value: 1,
    label: 'Running',
  }, {
    value: 0,
    label: 'Failed',
  }],
  color: '#ffffff',
  background: '#00bcf2',
  footer: 'Since last week',
  link: {
    path: StaticRoutes.ProcessExplorer,
    label: 'See more...',
  },
  iconClass: 'fa fa-cog',
};

export const ResourceCardConfig = {
  title: 'dashboard.card.resources',
  items: [{
    value: 120,
    label: 'Resources',
  }, {
    value: 10,
    label: 'New',
  }, {
    value: 4,
    label: 'Updated',
  }],
  color: '#ffffff',
  background: '#999999',
  footer: 'Since last week',
  link: {
    path: StaticRoutes.ResourceExplorer,
    label: 'See more...',
  },
  iconClass: 'fa fa-book',
};

export const QuotaCardConfig = {
  title: 'dashboard.card.quota',
  items: [{
    value: 100,
    label: 'Available',
  }, {
    value: 25,
    label: 'Used',
  }, {
    value: 75,
    label: 'Remaining',
  }],
  color: '#ffffff',
  background: '#d9534f',
  footer: 'Since last week',
  iconClass: 'fa fa-cubes',
};

export const EventCardConfig = {
  title: 'dashboard.card.events',
  items: [{
    value: 0,
    label: 'Error',
  }, {
    value: 5,
    label: 'Warning',
  }, {
    value: 75,
    label: 'Information',
  }],
  color: '#ffffff',
  background: '#5cb85c',
  footer: 'Since last week',
  link: {
    path: StaticRoutes.EventViewer,
    label: 'See more...',
  },
  iconClass: 'fa fa-heartbeat',
};
