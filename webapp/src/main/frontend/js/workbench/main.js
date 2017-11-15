const store = require('./store');
const { renderRoot } = require('./root');

import { setCsrfToken } from './ducks/meta';
import { changeLocale } from './ducks/i18n';
import { refreshProfile } from './ducks/user';

// TODO : Remove resource initialization
import { EnumProcessInput, EnumResourceType, ResourceTypeIcons } from './components/views/process/designer';
import { addResourceToBag } from './ducks/ui/views/process-designer';

var rootSelector = document.currentScript.getAttribute('data-root') || '#root';

// Bind top-level event handlers

document.addEventListener("DOMContentLoaded", function () {
  var rootEl = document.querySelector(rootSelector);

  // Todo read from non-httponly "language" cookie
  var language = "en";

  var token = document.querySelector("meta[name=_csrf]")
    .getAttribute('content');

  // Chain preliminary actions before initial rendering

  Promise.resolve()
    .then(() => store.dispatch(setCsrfToken(token)))
    .then(() => store.dispatch(changeLocale(language)))
    .then(() => store.dispatch(refreshProfile())
      // recover from an "Unauthorized" error
      .then(undefined, () => console.error('Cannot refresh user profile')))
    // TODO : Remove resource initialization
    .then(() => {
      store.dispatch(addResourceToBag({
        inputType: EnumProcessInput.CATALOG,
        resourceType: EnumResourceType.POI,
        id: 1,
        version: 1,
        title: 'OSM Athens POI Data',
        iconClass: ResourceTypeIcons[EnumResourceType.POI],
        dependencies: [],
      }));
      store.dispatch(addResourceToBag({
        inputType: EnumProcessInput.CATALOG,
        resourceType: EnumResourceType.POI,
        id: 2,
        version: 1,
        title: 'Get Athens POI Data',
        iconClass: ResourceTypeIcons[EnumResourceType.POI],
        dependencies: [],
      }));
      store.dispatch(addResourceToBag({
        inputType: EnumProcessInput.CATALOG,
        resourceType: EnumResourceType.LINKED,
        id: 3,
        version: 2,
        title: 'Athens Restaurants',
        iconClass: ResourceTypeIcons[EnumResourceType.LINKED],
        dependencies: [{
          index: 0,
          inputType: EnumProcessInput.CATALOG,
          resourceType: EnumResourceType.POI,
          id: 1,
          version: 1,
          title: 'OSM Athens POI Data',
          iconClass: ResourceTypeIcons[EnumResourceType.POI],
        }, {
          index: 1,
          inputType: EnumProcessInput.CATALOG,
          resourceType: EnumResourceType.POI,
          id: 2,
          version: 1,
          title: 'Get Athens POI Data',
          iconClass: ResourceTypeIcons[EnumResourceType.POI],
        }],
      }));
    })
    .then(() => renderRoot(rootEl));
});


// Provide development shortcuts

/* global process */
if (process.env.NODE_ENV != 'production') {
  global.$a = {
    store: store,
    api: require('./service/api/index'),
  };
}
