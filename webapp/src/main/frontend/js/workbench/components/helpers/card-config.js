import { StaticRoutes } from '../../model/routes';

export const JobCardConfig = (props) => ({
  title: 'dashboard.card.jobs',
  items: props ? [{
    value: props.completed,
    label: 'dashboard.card.fields.jobs.completed',
  }, {
    value: props.running,
    label: 'dashboard.card.fields.jobs.running',
  }, {
    value: props.failed,
    label: 'dashboard.card.fields.jobs.failed',
  }] : null,
  color: '#ffffff',
  background: '#00bcf2',
  footer: 'Since last week',
  link: {
    path: StaticRoutes.ProcessExplorer,
    label: 'See more...',
  },
  iconClass: 'fa fa-cog',
});

export const ResourceCardConfig = (props) => ({
  title: 'dashboard.card.resources',
  items: props ? [{
    value: props.total,
    label: 'dashboard.card.fields.resources.total',
  }, {
    value: props.created,
    label: 'dashboard.card.fields.resources.new',
  }, {
    value: props.updated,
    label: 'dashboard.card.fields.resources.updated',
  }] : null,
  color: '#ffffff',
  background: '#999999',
  footer: 'Since last week',
  link: {
    path: StaticRoutes.ResourceExplorer,
    label: 'See more...',
  },
  iconClass: 'fa fa-book',
});

export const SystemCardConfig = (props) => ({
  title: 'dashboard.card.system',
  items: props && props.online ? [{
    value: [props.usedCores || '-', props.totalCores || '-'],
    label: 'dashboard.card.fields.system.cores',
  }, {
    value: [props.usedMemory || '-', props.totalMemory || '-'],
    label: 'dashboard.card.fields.system.memory',
  }, {
    value: [props.usedDisk || '-', props.totalDisk || '-'],
    label: 'dashboard.card.fields.system.disk-space',
  }] : null,
  color: '#ffffff',
  background: '#d9534f',
  iconClass: 'fa fa-server',
});

export const EventCardConfig = (props) => ({
  title: 'dashboard.card.events',
  items: props ? [{
    value: props.error,
    label: 'dashboard.card.fields.events.error',
  }, {
    value: props.warning,
    label: 'dashboard.card.fields.events.warn',
  }, {
    value: props.information,
    label: 'dashboard.card.fields.events.info',
  }] : null,
  color: '#ffffff',
  background: '#5cb85c',
  footer: 'Since last 24 hours',
  link: {
    path: StaticRoutes.EventViewer,
    label: 'See more...',
  },
  iconClass: 'fa fa-heartbeat',
});
