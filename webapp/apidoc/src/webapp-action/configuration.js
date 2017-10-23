/**
 * @api {get} action/configuration/{locale} Load configuration
 * @apiVersion 1.0.0
 * @apiName Load
 * @apiGroup Configuration
 * @apiPermission ROLE_USER
 *
 * @apiDescription Loads application configuration settings and value lists
 *
 * @apiParam (Path Parameters)            {String}    locale            The locale used for translating resources
 *
 * @apiParamExample {json} Request Example
 * GET action/configuration/en
 *
 * @apiSuccess                            {Boolean}   success           Returns <code>true</code> or <code>false</code>
 * indicating success of the operation.
 * @apiSuccess                            {Object[]}  errors            Array of <code>Error</code> objects
 * @apiSuccess                            {Object}    result            An instance of <code>Configuration</code>. If the value of
 * property <code>success</code> is <code>false</code>, <code>result</code> is <code>undefined</code>
 *
 * @apiSuccess (Configuration)            {Object}    values            An instance of <code>ValueListCollection</code> which contains
 * value list items.
 *
 * @apiSuccess (ValueListCollection)      {Object[]}  dataFormats       An array of <code>ValueListItem</code> objects with all supported
 * data formats
 * @apiSuccess (ValueListCollection)      {Object[]}  dataSources       An array of <code>ValueListItem</code> objects with all supported
 * data sources
 * @apiSuccess (ValueListCollection)      {Object[]}  operations        An array of <code>ValueListItem</code> objects with all supported
 * operations
 * @apiSuccess (ValueListCollection)      {Object[]}  resourceTypes     An array of <code>ValueListItem</code> objects with all supported
 * resource types
 *
 * @apiSuccess (ValueListItem)            {String}    key               Unique key
 * @apiSuccess (ValueListItem)            {String}    name              Localized name for the key value
 *
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "success": true,
 *   "result": {
 *     "values": {
 *       "dataFormats": [{
 *         "key": "UNDEFINED",
 *         "name": "Unknown"
 *       }, {
 *         "key": "CSV",
 *         "name": "Comma-Separated Values"
 *       }, {
 *         "key": "GPX",
 *         "name": "GPS Exchange Format"
 *       }, {
 *         "key": "GEOJSON",
 *         "name": "GeoJSON"
 *       }, {
 *         "key": "OSM",
 *         "name": "Open Street Maps"
 *       }, {
 *         "key": "SHAPEFILE",
 *         "name": "Shape file"
 *       }, {
 *         "key": "RDF_XML",
 *         "name": "RDF/XML"
 *       }, {
 *         "key": "RDF_XML_ABBREV",
 *         "name": "RDF/XML-ABBREV"
 *       }, {
 *         "key": "TURTLE",
 *         "name": "Turtle"
 *       }, {
 *         "key": "N_TRIPLES",
 *         "name": "N-Triples"
 *       }, {
 *         "key": "N3",
 *         "name": "N3"
 *       }],
 *       "dataSources": [{
 *         "key": "UNDEFINED",
 *         "name": "Not Supported"
 *       }, {
 *         "key": "UPLOAD",
 *         "name": "Upload"
 *       }, {
 *         "key": "FILESYSTEM",
 *         "name": "File System"
 *       }, {
 *         "key": "HARVESTER",
 *         "name": "Harvester"
 *       }, {
 *         "key": "EXTERNAL_URL",
 *         "name": "External Url"
 *       }, {
 *         "key": "COMPUTED",
 *         "name": "Computed"
 *       }],
 *       "operations": [{
 *         "key": "UNDEFINED",
 *         "name": "Not Supported"
 *      }, {
 *         "key": "TRANSFORM",
 *         "name": "Transformation  "
 *       }, {
 *         "key": "INTERLINK",
 *         "name": "Interlinking"
 *       }, {
 *         "key": "FUSION",
 *         "name": "Fusion"
 *       }, {
 *         "key": "ENRICHEMENT",
 *         "name": "Enrichment"
 *       }],
 *       "resourceTypes": [{
 *         "key": "UNDEFINED",
 *         "name": "Unknown"
 *       }, {
 *         "key": "POI_DATA",
 *         "name": "POI Data"
 *       }, {
 *         "key": "POI_LINKED_DATA",
 *         "name": "POI Linked Data"
 *       }]
 *     }
 *   }
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
 *     code: "BasicErrorCode.NOT_IMPLEMENTED",
 *     description: "Not implemented"
 *   }],
 *   success: false
 * }
 *
 */
function browse() { return; }
