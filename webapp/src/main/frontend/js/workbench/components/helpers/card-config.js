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

export const ResourceCardConfig = (props, intl) => ({
  title: 'dashboard.card.resources',
  items: [{
    value: props.total,
    label: 'Resources',
  }, {
    value: props.created,
    label: 'New',
  }, {
    value: props.updated,
    label: 'Updated',
  }],
  color: '#ffffff',
  background: '#999999',
  footer: intl.formatRelative(props.updatedOn),
  link: {
    path: StaticRoutes.ResourceExplorer,
    label: 'See more...',
  },
  iconClass: 'fa fa-book',
});

export const SystemCardConfig = {
  title: 'dashboard.card.system',
  items: [{
    value: 12,
    label: ' / 100 Cores',
  }, {
    value: 24,
    label: ' / 128 Memory',
  }, {
    value: 75,
    label: ' / 2048 Disk Space',
  }],
  color: '#ffffff',
  background: '#d9534f',
  iconClass: 'fa fa-cubes',
};

export const EventCardConfig = (props, intl) => ({
  title: 'dashboard.card.events',
  items: [{
    value: props.error,
    label: 'Error',
  }, {
    value: props.warning,
    label: 'Warning',
  }, {
    value: props.information,
    label: 'Information',
  }],
  color: '#ffffff',
  background: '#5cb85c',
  footer: intl.formatRelative(props.updatedOn),
  link: {
    path: StaticRoutes.EventViewer,
    label: 'See more...',
  },
  iconClass: 'fa fa-heartbeat',
});
