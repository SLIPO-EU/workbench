/**
 * @api {post} api/v1/process Query
 * @apiHeader {String} X-API-Key Application key
 * @apiVersion 1.0.0
 * @apiName QueryWorkflow
 * @apiGroup Workflow
 * @apiPermission ROLE_API
 *
 * @apiDescription Queries workflows.
 *
 * @apiParamExample {json} Request Example
 * POST api/v1/process
 * {
 *  "pagingOptions":{
 *    "pageIndex":0,
 *    "pageSize":1
 *  },
 *  "query":{
 *    "name":"Demo"
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
 * Query for filtering workflow definitions.
 * @apiParam                              {String}    [query.name]
 * Workflow unique name.
 * @apiParam                              {String}    [query.taskType]
 * Workflow task type:<br/>
 * <code>API</code>: SLIPO API call<br/>
 * <code>REGISTRATION</code>: A workflow registration process created using the workbench workflow wizard<br/>
 * <code>DATA_INTEGRATION</code>: A generic data integration process created using the workbench process designer<br/>
 * <code>EXPORT</code>: A workflow export process created using the workbench export wizard<br/>
 * <code>EXPORT_MAP</code>: A workflow that imports RDF and log data to PostgreSQL for rendering maps<br/>
 *
 * @apiSuccess                            {Boolean}   success           Returns <code>true</code> or <code>false</code>
 * indicating success of the operation.
 * @apiSuccess                            {Error[]}   errors            Array of <code>Error</code> objects.
 * @apiSuccess                            {Object}    result            An instance of <code>QueryResult</code>. If value of
 * <code>success</code> is <code>false</code>, <code>result</code> is <code>null</code>
 *
 * @apiSuccess (QueryResult)              {Object}                  pagingOptions
 * Result data paging options.
 * @apiSuccess (QueryResult)              {Number}                  pagingOptions.pageIndex
 * Data page index.
 * @apiSuccess (QueryResult)              {Number}                  pagingOptions.pageSize
 * Date page size
 * @apiSuccess (QueryResult)              {Number}                  pagingOptions.count
 * Total number of records found.
 *
 * @apiSuccess (QueryResult)              {ProcessSimpleRecord[]}   items
 * Selected workflows.
 *
 * @apiSuccess (ProcessSimpleRecord)           {Number}                 createdOn
 * Creation timestamp.
 * @apiSuccess (ProcessSimpleRecord)           {String}                 description
 * Workflow description.
 * @apiSuccess (ProcessSimpleRecord)           {Number}                 executedOn
 * Execution timestamp.
 * @apiSuccess (ProcessSimpleRecord)           {Number}                 id
 * Workflow unique id.
 * @apiSuccess (ProcessSimpleRecord)           {String}                 name
 * Workflow unique name.
 * @apiSuccess (ProcessSimpleRecord)           {ProcessSimpleRecord[]}  [revisions]
 * A list of all workflow revisions. If no revisions exist, the property is omitted.
 * @apiSuccess (ProcessSimpleRecord)           {Object}                 taskType
 * Workflow task type.
 * @apiSuccess (ProcessSimpleRecord)           {Number}                 updatedOn
 * Last modified timestamp.
 * @apiSuccess (ProcessSimpleRecord)           {Number}                   version
 * Workflow version.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *  "errors":[],
 *  "result":{
 *    "pagingOptions":{
 *      "pageIndex":0,
 *      "pageSize":1,
 *      "count":16
 *    },
 *    "items":[{
 *      "createdOn":1552237103328,
 *      "description":"API Enrich Method",
 *      "executedOn":null,
 *      "id":145,
 *      "name":"API 10-03-2019 18:58:23 0ee87dc8-73c8-4f25-9698-b4f9d7f67f4c",
 *      "taskType":"API",
 *      "updatedOn":1552237103328,
 *      "version":1
 *    }]
 *  },
 *  "success":true
 * }
 */
function query() { return; }

/**
 * @api {get} api/v1/process/{id}/{version} Status
 * @apiHeader {String} X-API-Key Application key
 * @apiVersion 1.0.0
 * @apiName Status
 * @apiGroup Workflow
 * @apiPermission ROLE_API
 *
 * @apiDescription Queries workflow execution instance status.
 *
 * @apiParam (Query String Parameters)    {Number}    id                The workflow unique id
 * @apiParam (Query String Parameters)    {Number}    version           The workflow version
 *
 * @apiParamExample {json} Request Example
 * GET api/v1/process/46/2
 *
 * @apiSuccess                            {Boolean}   success           Returns <code>true</code> or <code>false</code>
 * indicating success of the operation.
 * @apiSuccess                            {Error[]}   errors            Array of <code>Error</code> objects.
 * @apiSuccess                            {Object}    result            An instance of <code>ProcessExecutionRecord</code>. If value of
 * <code>success</code> is <code>false</code>, <code>result</code> is <code>null</code>
 *
 * @apiSuccess (ProcessExecutionRecord)           {String}             status
 * Workflow execution instance status:<br/>
 * <code>UNKNOWN</code><br/>
 * <code>COMPLETED</code><br/>
 * <code>FAILED</code><br/>
 * <code>RUNNING</code><br/>
 * <code>STOPPED</code><br/>
 *
 * @apiSuccess (ProcessExecutionRecord)           {String}             taskType
 * Workflow task type.
 * @apiSuccess (ProcessExecutionRecord)           {Number}             executedOn
 * Execution timestamp.
 * @apiSuccess (ProcessExecutionRecord)           {Number}             id
 * Workflow execution instance unique id.
 * @apiSuccess (ProcessExecutionRecord)           {Number}             processId
 * Parent workflow unique id.
 * @apiSuccess (ProcessExecutionRecord)           {Number}             processVersion
 * Parent workflow version.
 * @apiSuccess (ProcessExecutionRecord)           {String}             name
 * Workflow unique name.
 * @apiSuccess (ProcessExecutionRecord)           {Number}             completedOn
 * Execution completion timestamp.
 * @apiSuccess (ProcessExecutionRecord)           {Number}             startedOn
 * Execution start timestamp.
 * @apiSuccess (ProcessExecutionRecord)           {Number}             submittedOn
 * Execution submit timestamp.
 * @apiSuccess (ProcessExecutionRecord)           {Step[]}             steps
 * Workflow steps
 *
 * @apiSuccess (Step)                             {String}             name
 * Unique step name
 * @apiSuccess (Step)                             {String}             status
 * Step execution status.
 * @apiSuccess (Step)                             {String}             tool
 * SLIPO Toolkit Component:<br/>
 * <code>REGISTER</code>: Catalog registration component<br/>
 * <code>TRIPLEGEO</code>: Data transformation component<br/>
 * <code>LIMES</code>: POI RDF dataset interlinking component<br/>
 * <code>FAGI</code>: POI RDF dataset and linked data fusion component<br/>
 * <code>DEER</code>: POI RDF dataset enrichment component<br/>
 * <code>REVERSE_TRIPLEGEO</code>: Data reverse transformation component<br/>
 * <code>IMPORTER</code>: An internal component for importing external data sources into a process<br/>
 * @apiSuccess (Step)                             {String}             operation
 * Operation type:<br/>
 * <code>REGISTER</code>: Register resource to catalog<br/>
 * <code>TRANSFORM</code>: Data transformation<br/>
 * <code>INTERLINK</code>: POI RDF dataset interlinking<br/>
 * <code>FUSION</code>: POI RDF dataset and linked data fusion<br/>
 * <code>ENRICHMENT</code>: POI RDF dataset enrichment<br/>
 * <code>IMPORT_DATA</code>: Import external data sources into a process<br/>
 * @apiSuccess (Step)                             {Number}             startedOn
 * Start timestamp.
 * @apiSuccess (Step)                             {Number}             completedOn
 * Completion timestamp.
 * @apiSuccess (Step)                             {File[]}             files
 * Execution files
 *
 * @apiSuccess (Step)                             {String}             id
 * Execution step file unique id.
 * @apiSuccess (Step)                             {String}             type
 * File type:<br/>
 * <code>CONFIGURATION</code>: Tool configuration<br/>
 * <code>INPUT</code>: Input file<br/>
 * <code>OUTPUT</code>: Output file<br/>
 * <code>SAMPLE</code>: Sample data collected during step execution<br/>
 * <code>KPI</code>: Tool specific or aggregated KPI data<br/>
 * <code>QA</code>: Tool specific QA data<br/>
 * <code>LOG</code>: Logs recorded during step execution  <br/>
 * @apiSuccess (Step)                             {String}             name
 * The file name
 * @apiSuccess (Step)                             {String}             size
 * The file size
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *  "errors":[],
 *  "result":{
 *    "status":"FAILED",
 *    "taskType":"DATA_INTEGRATION",
 *    "id":74,
 *    "processId":46,
 *    "processVersion":2,
 *    "name":"Test FAGI",
 *    "completedOn":1536224888309,
 *    "startedOn":1536224877916,
 *    "submittedOn":1536224877278,
 *    "steps":[{
 *      "name":"Interlink 1",
 *      "status":"COMPLETED",
 *      "tool":"LIMES",
 *      "operation":"INTERLINK",
 *      "startedOn":1536224878195,
 *      "completedOn":1536224887529,
 *      "files":[{
 *        "id":2012,
 *        "type":"INPUT",
 *        "name":"data.nt",
 *        "size":757323,
 *      },{
 *        "id":2016,
 *        "type":"CONFIGURATION",
 *        "name":"config.xml",
 *        "size":1195,
 *      },{
 *        ...
 *      }]
 *    },{
 *      "name":"Fuse 2",
 *      "status":"FAILED",
 *      "tool":"FAGI",
 *      "operation":"FUSION",
 *      "startedOn":1536224887781,
 *      "completedOn":1536224888298,
 *      "files":[{
 *        "id":2017,
 *        "type":"INPUT",
 *        "name":"accepted.nt",
 *        "size":757323
 *      },{
 *        ...
 *      }]
 *    }],
 *  "success":true
 * }
 */
function status() { return; }

/**
 * @api {post} api/v1/process/{id}/{version}/start Start
 * @apiHeader {String} X-API-Key Application key
 * @apiVersion 1.0.0
 * @apiName Start
 * @apiGroup Workflow
 * @apiPermission ROLE_API
 *
 * @apiDescription Starts workflow execution.
 *
 * @apiParam (Query String Parameters)    {Number}    id                The workflow unique id
 * @apiParam (Query String Parameters)    {Number}    version           The workflow version
 *
 * @apiParamExample {json} Request Example
 * GET api/v1/process/145/1/start
 *
 * @apiSuccess                            {Boolean}   success           Returns <code>true</code> or <code>false</code>
 * indicating success of the operation.
 * @apiSuccess                            {Error[]}   errors            Array of <code>Error</code> objects.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *  "errors":[],
  *  "success":true
 * }
 *
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "RPC_SERVER_UNREACHABLE",
 *     description: "Process execution has failed to start. RPC server is unreachable"
 *   }],
 *   success: false
 * }
 *
 * @apiError                              {Boolean}   success           Always <code>false</code>.
 * @apiError                              {Error[]}   errors            Array of <code>Error</code> objects.
 *
 * @apiError (Error)                      {String} code                 Unique error code.
 * @apiError (Error)                      {String} description          Error message. Application should not present
 * error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 */
function start() { return; }

/**
 * @api {post} api/v1/process/{id}/{version}/stop Stop
 * @apiHeader {String} X-API-Key Application key
 * @apiVersion 1.0.0
 * @apiName Stop
 * @apiGroup Workflow
 * @apiPermission ROLE_API
 *
 * @apiDescription Stops workflow execution.
 *
 * @apiParam (Query String Parameters)    {Number}    id                The workflow unique id
 * @apiParam (Query String Parameters)    {Number}    version           The workflow version
 *
 * @apiParamExample {json} Request Example
 * GET api/v1/process/145/1/stop
 *
 * @apiSuccess                            {Boolean}   success           Returns <code>true</code> or <code>false</code>
 * indicating success of the operation.
 * @apiSuccess                            {Error[]}   errors            Array of <code>Error</code> objects.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *  "errors":[],
  *  "success":true
 * }
 *
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "RPC_SERVER_UNREACHABLE",
 *     description: "Process execution has failed to start. RPC server is unreachable"
 *   }],
 *   success: false
 * }
 *
 * @apiError                              {Boolean}   success           Always <code>false</code>.
 * @apiError                              {Error[]}   errors            Array of <code>Error</code> objects.
 *
 * @apiError (Error)                      {String} code                 Unique error code.
 * @apiError (Error)                      {String} description          Error message. Application should not present
 * error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 */
function stop() { return; }

/**
 * @api {get} api/v1/process/{id}/{version}/file/{fileId} Download
 * @apiHeader {String} X-API-Key Application key
 * @apiVersion 1.0.0
 * @apiName DownloadWorkflowFile
 * @apiGroup Workflow
 * @apiPermission ROLE_API
 *
 * @apiDescription Downloads a file for a workflow execution instance.
 *
 * @apiParam (Query String Parameters)    {Number}    id                The workflow unique id
 * @apiParam (Query String Parameters)    {Number}    version           The selected workflow version
 * @apiParam (Query String Parameters)    {Number}    fileId            The file unique id
 *
 * @apiParamExample {json} Request Example
 * GET api/v1/process/1/1/file/1
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
 *     code: "FILE_NOT_FOUND",
 *     description: "Process execution file was not found"
 *   }],
 *   success: false
 * }
 *
 */
function download() { return; }
