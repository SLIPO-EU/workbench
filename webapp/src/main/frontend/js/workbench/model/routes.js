/**
 * Libraries
 */
import { pathToRegexp } from 'path-to-regexp';

/**
 * Components
 */
import { ResourceExplorerSidebar } from '../components/views/resource/explorer';
import { ResourceExportSidebar } from '../components/views/resource/export';
import { ProcessDesignerSidebar } from '../components/views/process/designer';
import { MapViewerSideBar, MapViewerToolBar } from '../components/views/map-viewer';

/**
 * Model
 */
import * as Roles from './role';
import {
  EnumTaskType,
} from '../model/process-designer/enum';

/**
 * Routes for utility pages
 */
const Login = '/login';
const Register = '/register';
const ResetPassword = '/reset-password';

export const Pages = {
  Login,
  Register,
  ResetPassword,
};

/**
 * Static routes
 */
const ApiUsage = '/api/usage';

const Dashboard = '/dashboard';
const UserFileSystem = '/user-files';
const Profile = '/profile';
const Settings = '/settings';

const HarvesterDataExplorer = '/harvester/data/explore';

const ResourceExplorer = '/resource/explore';
const ResourceExport = '/resource/export';
const ResourceRegistration = '/resource/register';

const ProcessExplorer = '/process/explore';
const ProcessExecutionExplorer = '/process/execution/explore';

const RecipeExplorer = '/recipe/explore';

const ApplicationKeyViewer = '/admin/application-key-viewer';
const UserManager = '/admin/user-manager';
const EventViewer = '/admin/event-viewer';

const KpiViewer = '/utilities/kpi-viewer';

export const StaticRoutes = {
  ApiUsage,
  Dashboard,
  UserFileSystem,
  HarvesterDataExplorer,
  Profile,
  Settings,
  ResourceExplorer,
  ResourceExport,
  ResourceRegistration,
  ProcessExplorer,
  ProcessExecutionExplorer,
  RecipeExplorer,
  UserManager,
  EventViewer,
  ApplicationKeyViewer,
  KpiViewer,
};

/**
 * Dynamic routes
 */

const ResourceMapViewer = '/resource/view/:id/:version/map';
const ResourceViewer = '/resource/view/:id/:version';

const ProcessDesignerCreate = '/workflow/designer';
const ProcessDesignerEdit = '/workflow/designer/:id';
const ProcessDesignerView = '/workflow/designer/:id/:version';

const ProcessDesignerEditTemplate = '/workflow/template/designer/:id';

const ProcessExecutionMapViewer = '/workflow/designer/:id/:version/execution/:execution/map';
const ProcessExecutionViewer = '/workflow/designer/:id/:version/execution/:execution';

const ApiExecutionViewer = '/api/operation/:processId';

export const DynamicRoutes = {
  ApiExecutionViewer,
  ResourceMapViewer,
  ResourceViewer,
  ProcessDesignerCreate,
  ProcessDesignerEdit,
  ProcessDesignerEditTemplate,
  ProcessDesignerView,
  ProcessExecutionMapViewer,
  ProcessExecutionViewer,
};

/**
 * Routes for error pages
 */

const Forbidden = '/error/403';
const NotFound = '/error/404';

export const ErrorPages = {
  Forbidden,
  NotFound,
};

// Guards
const processRouteGuard = (roles, views) => {
  const { process: { designer: { process = null } } } = views;

  if (!process) {
    return false;
  }
  if (roles.indexOf(Roles.ADMIN) !== -1) {
    // Full access for admins
    return true;
  }
  if ((roles.indexOf(Roles.ADMIN) === -1) && (process.taskType === EnumTaskType.API)) {
    // Only admins can view API execution data
    return false;
  }
  if ((roles.indexOf(Roles.AUTHOR) !== -1) && (process.taskType !== EnumTaskType.REGISTRATION)) {
    // Authors can not edit registration tasks
    return true;
  }
  return false;
};

/**
 * Default links
 */
const defaultLinks = [Dashboard, ResourceExplorer, ProcessExplorer];

const routes = {
  // Pages
  [Login]: {
    description: 'Login to workbench application',
  },
  [Register]: {
    description: 'Register a new account',
  },
  [ResetPassword]: {
    description: 'Reset user password',
  },
  // Static
  [Dashboard]: {
    description: 'Initial page',
    title: 'links.dashboard',
    defaultTitle: 'Dashboard',
    links: [ResourceExplorer, ProcessExplorer]
  },
  [UserFileSystem]: {
    description: 'My Files',
    title: 'links.user-files',
    defaultTitle: 'My Files',
    links: [ProcessDesignerCreate]
  },
  [HarvesterDataExplorer]: {
    description: 'Explore Harvester Data',
    title: 'links.harvester.data.explore',
    defaultTitle: 'Explore Harvester Data',
    roles: [Roles.ADMIN],
    links: [Dashboard],
  },
  [Profile]: {
    description: 'Profile',
    title: 'links.profile',
    defaultTitle: 'Profile',
    links: defaultLinks
  },
  [Settings]: {
    description: 'Settings',
    title: 'links.settings',
    defaultTitle: 'Settings',
    links: defaultLinks
  },
  [ResourceExplorer]: {
    description: 'Browse registered resources',
    title: 'links.resource.explorer',
    defaultTitle: 'Resource Explorer',
    links: [Dashboard, ProcessExplorer, ResourceRegistration, ProcessDesignerCreate],
    contextComponent: ResourceExplorerSidebar,
  },
  [ResourceRegistration]: {
    description: 'Register a new resource',
    title: 'links.resource.registration',
    defaultTitle: 'Resource Registration',
    roles: [Roles.ADMIN, Roles.AUTHOR],
    links: defaultLinks
  },
  [ResourceExport]: {
    description: 'Export an existing resource',
    title: 'links.resource.export',
    defaultTitle: 'Export Resource',
    roles: [Roles.ADMIN, Roles.AUTHOR],
    links: defaultLinks,
    contextComponent: ResourceExportSidebar,
  },
  [ProcessExplorer]: {
    description: 'Browse system processes',
    title: 'links.process.explorer',
    defaultTitle: 'Process Explorer',
    links: [Dashboard, ProcessDesignerCreate]
  },
  [ApiUsage]: {
    description: 'Browse API usage statistics',
    title: 'links.api.usage.default',
    defaultTitle: 'API Usage',
    links: [Dashboard, ProcessExplorer]
  },
  [ProcessExecutionExplorer]: {
    description: 'Browse workflow executions',
    title: 'links.process.execution.default',
    defaultTitle: 'Execution',
    links: [Dashboard, ProcessExplorer, ProcessDesignerCreate]
  },
  [RecipeExplorer]: {
    description: 'Browse recipes',
    title: 'links.recipe.explorer',
    defaultTitle: 'Recipe Explorer',
    links: defaultLinks
  },
  [UserManager]: {
    description: 'Manage user accounts',
    title: 'links.admin.user-manager',
    defaultTitle: 'User Management',
    roles: [Roles.ADMIN],
    links: [Dashboard, EventViewer],
  },
  [EventViewer]: {
    description: 'Browse event logs',
    title: 'links.admin.event-viewer',
    defaultTitle: 'Event Viewer',
    roles: [Roles.ADMIN],
    links: [Dashboard, UserManager],
  },
  [ApplicationKeyViewer]: {
    description: 'Browse application keys',
    title: 'links.admin.application-key-viewer',
    defaultTitle: 'Application Keys Viewer',
    roles: [Roles.ADMIN],
    links: [UserManager, EventViewer],
  },
  [KpiViewer]: {
    description: 'View KPI files',
    title: 'links.kpi.viewer',
    defaultTitle: 'View KPI files',
    roles: [Roles.ADMIN, Roles.DEVELOPER],
    links: [Dashboard],
  },
  // Dynamic
  [ApiExecutionViewer]: {
    description: 'View SLIPO API execution details',
    title: 'links.api.execution.viewer',
    defaultTitle: 'API Operation',
    roles: processRouteGuard,
    links: defaultLinks,
  },
  [ResourceMapViewer]: {
    description: 'View a resource map data',
    title: 'links.resource.map-viewer',
    defaultTitle: 'Map Viewer',
    links: defaultLinks,
    contextComponent: MapViewerSideBar,
    toolbarComponent: MapViewerToolBar,
  },
  [ResourceViewer]: {
    description: 'View/Update an existing resource',
    title: 'links.resource.viewer',
    defaultTitle: 'Resource Viewer',
    links: defaultLinks
  },
  [ProcessDesignerCreate]: {
    description: 'Create a data integration workflow',
    title: 'links.process.designer.default',
    defaultTitle: 'Workflow Designer',
    roles: [Roles.ADMIN, Roles.AUTHOR],
    links: defaultLinks,
    contextComponent: ProcessDesignerSidebar,
  },
  [ProcessDesignerEdit]: {
    description: 'Update a data integration workflow',
    title: 'links.process.designer.edit',
    defaultTitle: 'Edit',
    roles: processRouteGuard,
    links: defaultLinks,
    contextComponent: ProcessDesignerSidebar,
  },
  [ProcessDesignerEditTemplate]: {
    description: 'Update a template integration workflow',
    title: 'links.process.designer.edit-template',
    defaultTitle: 'Edit Template',
    roles: [Roles.ADMIN, Roles.AUTHOR],
    links: defaultLinks,
    contextComponent: ProcessDesignerSidebar,
  },
  [ProcessDesignerView]: {
    description: 'View a data integration workflow',
    title: 'links.process.designer.view',
    defaultTitle: 'View',
    roles: processRouteGuard,
    links: defaultLinks,
    contextComponent: ProcessDesignerSidebar,
  },
  [ProcessExecutionViewer]: {
    description: 'View information about a workflow execution instance',
    title: 'links.process.execution.view',
    defaultTitle: 'Execution',
    links: defaultLinks,
    contextComponent: ProcessDesignerSidebar,
  },
  [ProcessExecutionMapViewer]: {
    description: 'View a POI dataset',
    title: 'links.process.execution.map-viewer',
    defaultTitle: 'Map Viewer',
    links: defaultLinks,
    contextComponent: MapViewerSideBar,
    toolbarComponent: MapViewerToolBar,
  },
  // Error Pages
  [Forbidden]: {
    description: 'Forbidden',
  },
  [NotFound]: {
    description: 'Not Found',
  },
};

/**
 * Find a route by its path e.g. /Dashboard
 *
 * @export
 * @param {string} path - the route path
 * @returns the route properties
 */
export function getRoute(path) {
  const prop = matchRoute(path);

  if (Object.prototype.hasOwnProperty.call(routes, prop)) {
    return routes[prop];
  }
  return null;
}

/**
 * Matches the given path to an existing route and returns the route or null
 * if no match is found
 *
 * @export
 * @param {any} path - the route path to match
 * @returns the route that matched the given path or null if no match is found
 */
export function matchRoute(path) {
  for (let route in routes) {
    let re = pathToRegexp(route);
    if (re.test(path)) {
      return route;
    }
  }

  return null;
}

/**
 * Build a path given a route and optional parameters
 *
 * @export
 * @param {string} path - The route name
 * @param {string[]|object} params - Optional parameters to bind
 */
export function buildPath(path, params) {
  let result = path || '/';

  if (params) {
    if (Array.isArray(params)) {
      let re = /:\w+/i;
      for (const value of params) {
        result = result.replace(re, value);
      }
    } else {
      let toPath = pathToRegexp.compile(path);
      result = toPath(params);
    }
  }
  return result;
}
