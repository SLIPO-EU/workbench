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
 * @apiSuccess (ProcessSimpleRecord)           {String}                 taskType
 * Workflow task type.
 * @apiSuccess (ProcessSimpleRecord)           {Number}                 updatedOn
 * Last modified timestamp.
 * @apiSuccess (ProcessSimpleRecord)           {Number}                 version
 * Workflow version.
 * @apiSuccess (ProcessSimpleRecord)           {Object[]}               steps
 * An array of <code>Step</code> objects.
 *
 * @apiSuccess (Step)                          {Number}                 key
 * Unique key id.
 * @apiSuccess (Step)                          {Number}                 group
 * Step group.
 * @apiSuccess (Step)                          {String}                 name
 * Unique step name.
 * @apiSuccess (Step)                          {String}                 operation
 * Step operation type.
 * @apiSuccess (Step)                          {String}                 tool
 * SLIPO Toolkit component.
 * @apiSuccess (Step)                          {String[]}               inputKeys
 * An array of input keys.
 * @apiSuccess (Step)                          {String}                 outputKey
 * Step output unique key.
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
 *      "version":1,
 *      "steps": [{
 *        "group": 0,
 *        "inputKeys": [],
 *        "key": 0,
 *        "name": "Transform 1",
 *        "operation": "TRANSFORM",
 *        "outputKey": "3",
 *        "tool": "TRIPLEGEO"
 *        }]
 *    }]
 *  },
 *  "success":true
 * }
 */
function query() { return; }

/**
 * @api {post} api/v1/process/{id}/save Save
 * @apiHeader {String} X-API-Key Application key
 * @apiVersion 1.0.0
 * @apiName Save
 * @apiGroup Workflow
 * @apiPermission ROLE_API
 *
 * @apiDescription Creates a new version for the specified workflow
 *
 * @apiParam (Query String Parameters)    {Number}    id                The workflow unique id
 *
 * @apiParamExample {json} Request Example
 * POST api/v1/process/1/save
 *
 * @apiSuccess                            {Boolean}   success           Returns <code>true</code> or <code>false</code>
 * indicating success of the operation.
 * @apiSuccess                            {Error[]}   errors            Array of <code>Error</code> objects.
 * @apiSuccess                            {Object}    result            An instance of <code>ProcessExecutionRecord</code>.
 * If value of <code>success</code> is <code>false</code>, <code>result</code> is <code>null</code>. Property
 * <code>execution</code> is always <code>null</code> for new workflows.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *  "errors":[],
 *  "execution": null,
 *  "process": {
 *    "createdOn": 1557508338603,
 *    "description": "Demo",
 *    "executedOn": 1558601054676,
 *    "id": 285,
 *    "name": "Demo",
 *    "steps": [{
 *      "group": 0,
 *      "inputKeys": [],
 *      "key": 0,
 *      "name": "Transform 1",
 *      "operation": "TRANSFORM",
 *      "outputKey": "2",
 *      "tool": "TRIPLEGEO"
 *    }],
 *    "taskType": "DATA_INTEGRATION",
 *    "updatedOn": 1558601053135,
 *    "version": 2
 *  },
 *  "success":true
 * }
 */
 */
function save() { return; }

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
 * @apiSuccess (ProcessExecutionRecord)           {Object}             process
 * A <code>Process</code> object.
 * @apiSuccess (ProcessExecutionRecord)           {Object}             execution
 * An <code>Execution</code> object. If process execution has not started, this property is set to <code>null</code>.
 *
 * @apiSuccess (Process)                          {Number}             createdOn
 * Creation timestamp.
 * @apiSuccess (Process)                          {String}             description
 * Process description.
 * @apiSuccess (Process)                          {Number}             executedOn
 * Execution timestamp.
 * @apiSuccess (Process)                          {Number}             id
 * Process unique id.
 * @apiSuccess (Process)                          {String}             name
 * Process unique name.
 * @apiSuccess (Process)                          {String}             taskType
 * Process task type.
 * @apiSuccess (Process)                          {Number}             updatedOn
 * Modified timestamp.
 * @apiSuccess (Process)                          {Number}             version
 * Process version number.
 * @apiSuccess (Process)                          {Object[]}           steps
 * An array of <code>ProcessStep</code> objects.
 *
 * @apiSuccess (ProcessStep)                      {Number}             key
 * Unique key id.
 * @apiSuccess (ProcessStep)                      {Number}             group
 * Step group.
 * @apiSuccess (ProcessStep)                      {String}             name
 * Unique step name.
 * @apiSuccess (ProcessStep)                      {String}             operation
 * Step operation type.
 * @apiSuccess (ProcessStep)                      {String}             tool
 * SLIPO Toolkit component.
 * @apiSuccess (ProcessStep)                      {String[]}           inputKeys
 * An array of input keys.
 * @apiSuccess (ProcessStep)                      {String}             outputKey
 * Step output unique key.
 *
 * @apiSuccess (Execution)                        {String}             status
 * Process execution instance status:<br/>
 * <code>UNKNOWN</code><br/>
 * <code>COMPLETED</code><br/>
 * <code>FAILED</code><br/>
 * <code>RUNNING</code><br/>
 * <code>STOPPED</code><br/>
 *
 * @apiSuccess (Execution)                        {String}             taskType
 * Process task type.
 * @apiSuccess (Execution)                        {Number}             id
 * Process execution instance unique id.
 * @apiSuccess (Execution)                        {Number}             processId
 * Parent process unique id.
 * @apiSuccess (Execution)                        {Number}             processVersion
 * Parent process version.
 * @apiSuccess (Execution)                        {String}             name
 * Process unique name.
 * @apiSuccess (Execution)                        {Number}             completedOn
 * Execution completion timestamp.
 * @apiSuccess (Execution)                        {Number}             startedOn
 * Execution start timestamp.
 * @apiSuccess (Execution)                        {Number}             submittedOn
 * Execution submit timestamp.
 * @apiSuccess (Execution)                        {Object[]}           steps
 * An array of <code>ExecutionStep</code> objects.
 *
 * @apiSuccess (ExecutionStep)                    {Number}             key
 * Unique step id.
 * @apiSuccess (ExecutionStep)                    {String}             name
 * Unique step name.
 * @apiSuccess (ExecutionStep)                    {String}             status
 * Step execution status.
 * @apiSuccess (ExecutionStep)                    {String}             tool
 * SLIPO Toolkit Component:<br/>
 * <code>REGISTER</code>: Catalog registration component<br/>
 * <code>TRIPLEGEO</code>: Data transformation component<br/>
 * <code>LIMES</code>: POI RDF dataset interlinking component<br/>
 * <code>FAGI</code>: POI RDF dataset and linked data fusion component<br/>
 * <code>DEER</code>: POI RDF dataset enrichment component<br/>
 * <code>REVERSE_TRIPLEGEO</code>: Data reverse transformation component<br/>
 * <code>IMPORTER</code>: An internal component for importing external data sources into a process<br/>
 * @apiSuccess (ExecutionStep)                    {String}             operation
 * Operation type:<br/>
 * <code>REGISTER</code>: Register resource to catalog<br/>
 * <code>TRANSFORM</code>: Data transformation<br/>
 * <code>INTERLINK</code>: POI RDF dataset interlinking<br/>
 * <code>FUSION</code>: POI RDF dataset and linked data fusion<br/>
 * <code>ENRICHMENT</code>: POI RDF dataset enrichment<br/>
 * <code>IMPORT_DATA</code>: Import external data sources into a process<br/>
 * @apiSuccess (ExecutionStep)                    {Number}             startedOn
 * Start timestamp.
 * @apiSuccess (ExecutionStep)                    {Number}             completedOn
 * Completion timestamp.
 * @apiSuccess (ExecutionStep)                    {Object[]}           files
 * An array of <code>ExecutionStepFile</code> objects.
 *
 * @apiSuccess (ExecutionStepFile)                {Number}             id
 * Execution step file unique id.
 * @apiSuccess (ExecutionStepFile)                {String}             name
 * The step file name.
 * @apiSuccess (ExecutionStepFile)                {String}             outputPartKey
 * The type of output file depending on the operation e.g. for <codes>LIMES</code> the output
 * part key may be any of <code>accepted</code> or <code>review</code>. If no applicable value
 * exists, this property is set to <code>null</code>.
 * @apiSuccess (ExecutionStepFile)                {String}             size
 * The file size.
 * @apiSuccess (ExecutionStepFile)                {String}             type
 * File type:<br/>
 * <code>CONFIGURATION</code>: Tool configuration<br/>
 * <code>INPUT</code>: Input file<br/>
 * <code>OUTPUT</code>: Output file<br/>
 * <code>SAMPLE</code>: Sample data collected during step execution<br/>
 * <code>KPI</code>: Tool specific or aggregated KPI data<br/>
 * <code>QA</code>: Tool specific QA data<br/>
 * <code>LOG</code>: Logs recorded during step execution  <br/>
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *  "errors":[],
 *  "execution": {
 *    "completedOn": 1558601105127,
 *    "id": 544,
 *    "name": "Demo",
 *    "processId": 285,
 *    "processVersion": 26,
 *    "startedOn": 1558601054676,
 *    "status": "COMPLETED",
 *    "steps": [{
 *      "completedOn": 1558601068543,
 *      "files": [{
 *        "id": 8095,
 *        "name": "classification.csv",
 *        "outputPartKey": null,
 *        "size": 4412,
 *        "type": "CONFIGURATION"
 *      }, {
 *        "id": 8094,
 *        "name": "mappings.yml",
 *        "outputPartKey": null,
 *        "size": 822,
 *        "type": "CONFIGURATION"
 *      }, {
 *        "id": 8093,
 *        "name": "options.conf",
 *        "outputPartKey": null,
 *        "size": 1260,
 *        "type": "CONFIGURATION"
 *      }, {
 *        "id": 8089,
 *        "name": "classification_metadata.json",
 *        "outputPartKey": "classification-metadata",
 *        "size": 382,
 *        "type": "KPI"
 *      }, {
 *        "id": 8090,
 *        "name": "classification.nt",
 *        "outputPartKey": "classification",
 *        "size": 89766,
 *        "type": "OUTPUT"
 *      }, {
 *        "id": 8083,
 *        "name": "DKV_Berlin.csv",
 *        "outputPartKey": "registration-request",
 *        "size": 36219,
 *        "type": "OUTPUT"
 *      }, {
 *        "id": 8085,
 *        "name": "DKV_Berlin_metadata.json",
 *        "outputPartKey": "transformed-metadata",
 *        "size": 532,
 *        "type": "KPI"
 *      }, {
 *        "id": 8087,
 *        "name": "DKV_Berlin.nt",
 *        "outputPartKey": "transformed",
 *        "size": 747829,
 *        "type": "OUTPUT"
 *      }, {
 *        "id": 8081,
 *        "name": "DKV_Berlin.csv",
 *        "outputPartKey": null,
 *        "size": 28669,
 *        "type": "INPUT"
 *      }],
 *      "key": 0,
 *      "name": "Transform 1",
 *      "operation": "TRANSFORM",
 *      "startedOn": 1558601054670,
 *      "status": "COMPLETED",
 *      "tool": "TRIPLEGEO"
 *    }],
 *    "submittedOn": 1558601054294,
 *    "taskType": "DATA_INTEGRATION"
 *  },
 *  "process": {
 *    "createdOn": 1557508338603,
 *    "description": "Demo",
 *    "executedOn": 1558601054676,
 *    "id": 285,
 *    "name": "Demo",
 *    "steps": [{
 *      "group": 0,
 *      "inputKeys": [],
 *      "key": 0,
 *      "name": "Transform 1",
 *      "operation": "TRANSFORM",
 *      "outputKey": "2",
 *      "tool": "TRIPLEGEO"
 *    }],
 *    "taskType": "DATA_INTEGRATION",
 *    "updatedOn": 1558601053135,
 *    "version": 26
 *  },
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
