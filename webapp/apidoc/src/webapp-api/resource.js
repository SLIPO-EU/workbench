/**
 * @api {post} api/v1/resource Query
 * @apiHeader {String} X-API-Key Application key
 * @apiVersion 1.0.0
 * @apiName QueryResource
 * @apiGroup Resources
 * @apiPermission ROLE_API
 *
 * @apiDescription Queries resource catalog for available resources. Resources must be
 * registered using SLIPO workbench application. The API does not support the registration
 * of new resources.
 *
 * @apiParamExample {json} Request Example
 * POST api/v1/resource
 * {
 *  "pagingOptions":{
 *    "pageIndex":0,
 *    "pageSize":1
 *  },
 *  "query":{
 *    "name":"Name"
 *  }
 * }
 *
 * @apiParam                              {Object}    pagingOptions
 * Data paging options
 * @apiParam                              {Number}    pagingOptions.pageIndex
 * Data page index. If a negative value is specified, the page index value is set to 0.
 * @apiParam                              {Number}    pagingOptions.pageSize
 * Data page size. If page size is less or equal to 0, the page size value is set to 10.
 *
 * @apiParam                              {Object}    query
 * Query for filtering catalog resources.
 * @apiParam                              {String}    [query.name]
 * Resource name.
 * @apiParam                              {String}    [query.description]
 * Resource description.
 * @apiParam                              {String}    [query.format]
 * Resource initial format. Valid values are:<br/>
 * <code>CSV</code>: Comma-Separated Values<br/>
 * <code>JSON</code>: Plain JSON (any schema)<br/>
 * <code>XML</code>: Generic XML<br/>
 * <code>GPX</code>: GPS Exchange Format<br/>
 * <code>GEOJSON</code>: JSON-encoded geographic data structures<br/>
 * <code>OSM_XML</code>: OSM XML - Open Street Maps XML format<br/>
 * <code>OSM_PBF</code>: OSM PBF - Open Street Maps protocolbuffer format. See <a href="https://wiki.openstreetmap.org/wiki/PBF_Format"></a><br/>
 * <code>SHAPEFILE</code>: ESRI shape file<br/>
 * @apiParam                              {Object}    [query.boundingBox]
 * Search resources by checking bounding box intersection. The geometry parameter must be
 * formatted as a valid GeoJSON geometry.
 * @apiParam                              {Number}    [query.size]
 * Search for resources with more than <code>size</code> entities.
 *
 * @apiSuccess                            {Boolean}   success           Returns <code>true</code> or <code>false</code>
 * indicating success of the operation.
 * @apiSuccess                            {Object[]}  errors            Array of <code>Error</code> objects.
 * @apiSuccess                            {Object}    result            An instance of <code>QueryResult</code>. If value of
 * <code>success</code> is <code>false</code>, <code>result</code> is <code>null</code>
 *
 * @apiSuccess (QueryResult)              {Object}            pagingOptions
 * Result data paging options.
 * @apiSuccess (QueryResult)              {Number}            pagingOptions.pageIndex
 * Data page index.
 * @apiSuccess (QueryResult)              {Number}            pagingOptions.pageSize
 * Date page size
 * @apiSuccess (QueryResult)              {Number}            pagingOptions.count
 * Total number of records found.
 *
 * @apiSuccess (QueryResult)              {ResourceRecord[]}  items
 * Selected resources.
 *
 * @apiSuccess (ResourceRecord)           {Object}            boundingBox
 * Resource bounding box. The geometry is formatted as a valid GeoJSON geometry.
 * @apiSuccess (ResourceRecord)           {Number}            createdOn
 * Creation timestamp.
 * @apiSuccess (ResourceRecord)           {String}            description
 * Resource description.
 * @apiSuccess (ResourceRecord)           {Number}            id
 * Resource unique id.
 * @apiSuccess (ResourceRecord)           {String}            name
 * Resource name.
 * @apiSuccess (ResourceRecord)           {Number}            numberOfEntities
 * Number of entities in the RDF dataset.
 * @apiSuccess (ResourceRecord)           {ResourceRecord[]}  revisions
 * A list of all resource revisions. If no revisions exist, the property is omitted.
 * @apiSuccess (ResourceRecord)           {Object}            size
 * Resource file size in bytes.
 * @apiSuccess (ResourceRecord)           {String}            tableName
 * Database table name if the resource has been imported to PostGIS or <code>null</code>.
 * @apiSuccess (ResourceRecord)           {Number}            updatedOn
 * Last modified timestamp.
 * @apiSuccess (ResourceRecord)           {Number}            version
 * Resource version.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *  "errors":[],
 *  "result":{
 *    "pagingOptions":{
 *      "pageIndex":0,
 *      "pageSize":1,
 *      "count":4
 *    },
 *    "items":[{
 *      "id":46,
 *      "version":2,
 *      "name":"Name",
 *      "description":"Description",
 *      "createdOn":1551894892815,
 *      "size":749509,
 *      "boundingBox":null,
 *      "numberOfEntities":100,
 *      "tableName":null,
 *      "revisions":[{
 *        "id":46,
 *        "version":1,
 *        "name":"Name",
 *        "description":"Description",
 *        "createdOn":1551695892815,
 *        "size":749509,
 *        "boundingBox":null,
 *        "numberOfEntities":100,
 *        "tableName":null
 *      }]
 *    }],
 *  },"
 *  success":true
 * }
 *
 * @apiError                              {Boolean}   success           Always <code>false</code>.
 * @apiError                              {Error[]}   errors            Array of <code>Error</code> objects.
 *
 * @apiError (Error)                      {String} code                 Unique error code.
 * @apiError (Error)                      {String} description          Error message. Application should not present
 * error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 *
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "PATH_NOT_FOUND",
 *     description: "Path / was not found."
 *   }],
 *   success: false
 * }
 *
 */
function query() { return; }

/**
 * @api {get} api/v1/resource/{id}/{version} Download
 * @apiHeader {String} X-API-Key Application key
 * @apiVersion 1.0.0
 * @apiName DownloadResource
 * @apiGroup Resources
 * @apiPermission ROLE_API
 *
 * @apiDescription Downloads a resource
 *
 * @apiParam (Query String Parameters)    {Number}    id                The resource unique id
 * @apiParam (Query String Parameters)    {Number}    version           The selected resource version
 *
 * @apiParamExample {json} Request Example
 * GET api/v1/resource/46/1
 *
 * @apiError                              {Boolean}   success           Always <code>false</code>.
 * @apiError                              {File[]}    errors            Array of <code>Error</code> objects.
 *
 * @apiError (Error)                      {String} code                 Unique error code.
 * @apiError (Error)                      {String} description          Error message. Application should not present
 * error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 *
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "RESOURCE_NOT_FOUND",
 *     description: "Resource was not found"
 *   }],
 *   success: false
 * }
 *
 */
function download() { return; }
