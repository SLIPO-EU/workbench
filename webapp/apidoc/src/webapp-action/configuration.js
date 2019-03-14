/**
 * @api {get} action/configuration Load configuration
 * @apiVersion 1.0.0
 * @apiName Load
 * @apiGroup Configuration
 * @apiPermission ROLE_USER, ROLE_AUTHOR, ROLE_ADMIN
 *
 * @apiDescription Loads application configuration settings
 *
 * @apiParamExample {json} Request Example
 * GET action/configuration
 *
 * @apiSuccess                            {Boolean}   success           Returns <code>true</code> or <code>false</code>
 * indicating success of the operation.
 * @apiSuccess                            {Object[]}  errors
 * Array of <code>Error</code> objects
 * @apiSuccess                            {Object}    result
 * An instance of <code>Configuration</code>. If the value of property <code>success</code> is <code>false</code>,
 * <code>result</code> is <code>null</code>
 *
 * @apiSuccess (Configuration)            {Object}    bingMaps
 * Bing Maps configuration options
 * @apiSuccess (Configuration)            {String}    bingMaps.applicationKey
 * Bing Maps API key. An API key can be created at <a href="https://www.bingmapsportal.com">Bing Maps Dev Center</a>.
 * @apiSuccess (Configuration)            {String}    bingMaps.imagerySet
 * Imagery set used. Valid values are <code>Road</code>, <code>Aerial</code> and <code>AerialWithLabels</code>.
 *
 * @apiSuccess (Configuration)            {Object}    deer
 * An instance of <code>SlipoToolkitConfiguration</code> for DEER.
 * @apiSuccess (Configuration)            {Object}    fagi
 * An instance of <code>SlipoToolkitConfiguration</code> for FAGI.
 * @apiSuccess (Configuration)            {Object}    limes
 * An instance of <code>SlipoToolkitConfiguration</code> for LIMES.
 * @apiSuccess (Configuration)            {Object}    reverseTripleGeo
 * An instance of <code>SlipoToolkitConfiguration</code> for TripleGeo Reverse.
 * @apiSuccess (Configuration)            {Object}    tripleGeo
 * An instance of <code>SlipoToolkitConfiguration</code> for TripleGeo.
 *
 * @apiSuccess (Configuration)            {Object}    mapDefaults
 * Default settings for application maps.
 * @apiSuccess (Configuration)            {Number[]}  mapDefaults.center
 * Default map center coordinates.
 *
 * @apiSuccess (Configuration)            {Object}    osm
 * Open Street Maps configuration options.
 * @apiSuccess (Configuration)            {Object}    osm.url
 * Default OSM url.
 *
 * @apiSuccess (Configuration)            {Object}    profiles
 * Contains a dictionary of SLIPO toolkit component specific profiles.
 *
 * @apiSuccess (SlipoToolkitConfiguration)  {String}    baselineVersion
 * Default version used by the designer when the version for an existing workflow step is not set.
 * @apiSuccess (SlipoToolkitConfiguration)  {String[]}  supportedVersions
 * A list of all supported versions.
 * @apiSuccess (SlipoToolkitConfiguration)  {String}    version
 * The current version.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *  "errors":[],
 *  "result":{
 *    "profiles":{
 *      "REVERSE_TRIPLEGEO":{
 *        "OSM_Europe":{...},
 *        "SLIPO_default":{...},
 *        ...
 *      },
 *      "FAGI":{
 *        "SLIPO_MatchByName":{...},
 *        ...
 *      },
 *      "TRIPLEGEO":{
 *        "OSM_Europe":{...},
 *        ...
 *      },
 *      "LIMES":{
 *        "SLIPO_MatchByName":{...},
 *        ...,
 *      }
 *    },
 *    "osm":{
 *      "url":"https://a.tile.openstreetmap.org/${z}/${x}/${y}.png"},
 *    },
 *    "bingMaps":{
 *      "applicationKey": ...,
 *      "imagerySet":"Road"
 *    },
 *    "tripleGeo":{
 *      "version":"1.6",
 *      "baselineVersion":"1.6",
 *      "supportedVersions":["1.2","1.4","1.5","1.6"]
 *    },
 *    "reverseTripleGeo":{
 *      "version":"1.6",
 *      "baselineVersion":"1.6",
 *      "supportedVersions":["1.5","1.6"]
 *    },
 *    "limes":{
 *      "version":"1.5",
 *      "baselineVersion":"1.5",
 *      "supportedVersions":["1.3","1.5"]
 *    },
 *    "fagi":{
 *      "version":"1.2",
 *      "baselineVersion":"1.2",
 *      "supportedVersions":["1.2"]
 *    },
 *    "deer":{
 *      "version":"1.1",
 *      "baselineVersion":"1.1",
 *      "supportedVersions":["1.1"]
 *    },
 *    "mapDefaults":{
 *      "center":[14.183,48.49623]
 *    }
 *  },
 *  "success":true
 * }
 *
 * @apiError                              {Boolean}   success           Always <code>false</code>.
 * @apiError                              {Object[]}  errors            Array of <code>Error</code> objects.
 *
 * @apiError (Error)                      {String} code                 Unique error code.
 * @apiError (Error)                      {String} description          Error message
 *
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "NOT_IMPLEMENTED",
 *     description: "Not implemented"
 *   }],
 *   success: false
 * }
 *
 */
function browse() { return; }
