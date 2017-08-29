const routes = new Map([
  ['/', {
    name: 'home', title: 'Home',
  }],
  ['/dashboard', {
    name: 'dashboard', title: 'Dashboard'
  }],
  ['/resource/explorer', {
    name: 'resource-explorer', title: 'Resource Explorer'
  }],
  ['/resource/register', {
    name: 'resource-register', title: 'Resource Registration'
  }],
  ['/admin/user-manager', {
    name: 'user-manager', title: 'User Management'
  }],
  ['/admin/event-viewer', {
    name: 'event-viewer', title: 'Event Viewer'
  }],
]);

module.exports = routes;
