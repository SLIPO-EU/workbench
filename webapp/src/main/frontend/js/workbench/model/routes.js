import * as Roles from './role';

const Dashboard = '[Dashboard] Initial page';

const ResourceExplorer = '[Resources] Browse registered resources';
const ResourceRegistration = '[Resources] Register resource';

const Scheduler = '[Process] Scheduler';
const ProcessDesigner = '[Process] Data integration process designer';

const RecipeExplorer = '[Recipe] View recipes';
const RecipeDesigner = '[Recipe] Design recipe';

const UserManager = '[Admin] Manage user accounts';
const EventViewer = '[Admin] Browser event logs';

/**
 * Static link identifiers
 */
export const StaticLinks = {
  Dashboard,
  ResourceExplorer,
  ResourceRegistration,
  Scheduler,
  ProcessDesigner,
  RecipeExplorer,
  RecipeDesigner,
  UserManager,
  EventViewer
};

const defaultLinks = [Dashboard, ResourceExplorer, Scheduler];

const routes = [
  {
    name: Dashboard,
    path: '/dashboard',
    title: 'links.dashboard',
    defaultTitle: 'Dashboard',
    links: [ResourceExplorer, Scheduler]
  }, {
    name: ResourceExplorer,
    path: '/resource/explorer',
    title: 'links.resource.explorer',
    defaultTitle: 'Resource Explorer',
    links: [Dashboard, Scheduler, ResourceRegistration]
  }, {
    name: ResourceRegistration,
    path: '/resource/register',
    title: 'links.resource.registration',
    defaultTitle: 'Resource Registration',
    links: defaultLinks
  }, {
    name: Scheduler,
    path: '/process/scheduler',
    title: 'links.process.scheduler',
    defaultTitle: 'Scheduler',
    links: [Dashboard, ResourceExplorer, ProcessDesigner]
  }, {
    name: ProcessDesigner,
    path: '/process/design',
    title: 'links.process.design',
    defaultTitle: 'Process Designer',
    links: defaultLinks
  }, {
    name: RecipeExplorer,
    path: '/recipe/explorer',
    title: 'links.recipe.explorer',
    defaultTitle: 'Recipes',
    links: defaultLinks
  }, {
    name: RecipeDesigner,
    path: '/recipe/design',
    title: 'links.recipe.design',
    defaultTitle: 'Recipe Designer',
    links: defaultLinks
  }, {
    name: UserManager,
    path: '/admin/user-manager',
    title: 'links.admin.user-manager',
    defaultTitle: 'User Management',
    role: Roles.ADMIN,
    links: defaultLinks
  }, {
    name: EventViewer,
    path: '/admin/event-viewer',
    title: 'links.admin.event-viewer',
    defaultTitle: 'Event Viewer',
    role: Roles.MAINTAINER,
    links: defaultLinks
  },
];

function getRouteFromProperty(prop, value) {
  return routes.find(r => r[prop] === value);
}

/**
 * Search and return a route by its path e.g. /Dashboard
 * 
 * @export
 * @param {any} path 
 * @returns 
 */
export function getRouteFromPath(path) {
  return getRouteFromProperty('path', path);
}

/**
 * Search and return a route by its name e.g. {@link StaticLinks.Dashboard}
 * 
 * @export
 * @param {any} name 
 * @returns 
 */
export function getRouteFromName(name) {
  return getRouteFromProperty('name', name);
}
