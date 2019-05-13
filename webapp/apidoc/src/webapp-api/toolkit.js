/**
 * @api {get} api/v1/toolkit/profiles Profiles
 * @apiHeader {String} X-API-Key Application key
 * @apiVersion 1.0.0
 * @apiName Profiles
 * @apiGroup Toolkit
 * @apiPermission ROLE_API
 *
 * @apiDescription Enumerates all available profiles for SLIPO Toolkit Components
 *
 * @apiParamExample {json} Request Example
 * GET api/v1/toolkit/profiles
 *
 * @apiSuccess                            {Boolean}   success           Returns <code>true</code> or <code>false</code>
 * indicating success of the operation.
 * @apiSuccess                            {Error[]}  errors            Array of <code>Error</code> objects.
 * @apiSuccess                            {Object}    result            An instance of <code>ProfileCollection</code>. If value of
 * <code>success</code> is <code>false</code>, <code>result</code> is <code>null</code>
 *
 * @apiSuccess (ProfileCollection)        {String[]}  [TRIPLEGEO]           TripleGeo profiles
 * @apiSuccess (ProfileCollection)        {String[]}  [REVERSE_TRIPLEGEO]   Reverse TripleGeo profiles
 * @apiSuccess (ProfileCollection)        {String[]}  [DEER]                DEER profiles
 * @apiSuccess (ProfileCollection)        {String[]}  [FAGI]                FAGI profiles
 * @apiSuccess (ProfileCollection)        {String[]}  [LIMES]               LIMES profiles
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *  "errors":[],
 *  "result":{
 *    "TRIPLEGEO":[
 *      "OSM_Europe",
 *      ...
 *    ],
 *    "REVERSE_TRIPLEGEO":[
 *      "OSM_Europe",
 *      "SLIPO_default"
 *    ],
 *    "DEER":[
 *      ...
 *    ],
 *    "FAGI":[
 *      ...
 *    ],
 *    "LIMES":[
 *      "SLIPO_MatchByName",
 *      ...
 *    ]
 *  },
 *  "success":true
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
 *     code: "IO_ERROR",
 *     description: "An unknown error has occurred"
 *   }],
 *   success: false
 * }
 *
 */
function profiles() { return; }

/**
 * @api {post} api/v1/toolkit/transform Transform
 * @apiHeader {String} X-API-Key Application key
 * @apiVersion 1.0.0
 * @apiName Transform
 * @apiGroup Toolkit
 * @apiPermission ROLE_API
 *
 * @apiDescription Executes a workflow with a single Transform operation.
 *
 * @apiParamExample {json} Request Example
 * POST api/v1/toolkit/transform
 * {
 *  "path":"csv/data.csv",
 *  "configuration":{
 *    "attrCategory": "type",
 *    "attrGeometry": null,
 *    "attrKey": "ID",
 *    "attrName": "name",
 *    "attrX": "lon",
 *    "attrY": "lat",
 *    "defaultLang": "en",
 *    "delimiter": ";",
 *    "encoding": "UTF-8",
 *    "featureSource": "DataFeatureSource",
 *    "inputFormat": "CSV",
 *    "profile": "SLIPO_Default",
 *    "quote": "",
 *    "sourceCRS": "EPSG:4326",
 *    "targetCRS": "EPSG:4326",
 *    "mappingSpec": null,
 *    "classificationSpec": null,
 *  }
 * }
 *
 * @apiParam                              {String}          path
 * A relative path to the input file.
 * @apiParam                              {Configuration}   configuration
 * TripleGeo configuration
 *
 * @apiParam  (Configuration)             {String}          [profile]
 * A profile for setting default configuration values.
 * @apiParam  (Configuration)             {String}          inputFormat
 * The data format that input files conform to.
 * @apiParam  (Configuration)             {String}          [encoding]
 * Optional parameter for the encoding (character set) for strings in the input data.
 * If not specified, UTF-8 encoding is assumed.
 * @apiParam  (Configuration)             {String}          attrKey
 * The name of the field holding a unique identifier for each input record.
 * @apiParam  (Configuration)             {String}          attrName
 * The name of the field from which names will be extracted.
 * @apiParam  (Configuration)             {String}          attrCategory
 * The name of the field from which a category will be extracted (e.g. type of points
 * road classes).
 * @apiParam  (Configuration)             {String}          [attrGeometry]
 * Parameter that specifies the name of the geometry column in the input dataset. Omit
 * this parameter if geometry representation is available with columns specifying X,Y
 * coordinates for points; otherwise, this parameter is MANDATORY.
 * @apiParam  (Configuration)             {String}          [delimiter]
 * A field delimiter for records (meaningful only for CSV input).
 * @apiParam  (Configuration)             {String}          [quote]
 * Mandatory for CSV input only (case-insensitive): specify quote character for string
 * values; Remove for any other types of input data.
 * @apiParam  (Configuration)             {String}          [attrX]
 * Required for CSV input only (case-insensitive): specify attribute holding
 * X-coordinates of point locations.
 * @apiParam  (Configuration)             {String}          [attrY]
 * Required for CSV input only (case-insensitive): specify attribute holding
 * Y-coordinates of point locations.
 * @apiParam  (Configuration)             {String}          featureSource
 * A name for the data source provider of input features.
 * @apiParam  (Configuration)             {String}          sourceCRS
 * The coordinate reference system (CRS) for input data (eg "EPSG:4326").
 * @apiParam  (Configuration)             {String}          targetCRS
 * The coordinate reference system (CRS) for output data (e.g "EPSG:4326").
 * @apiParam  (Configuration)             {String}          [defaultLang]
 * The default language for labels created in output RDF. The default is "en".
 * @apiParam  (Configuration)             {String}          [mappingSpec]
 * A resource location of a YML file containing mappings from input schema to RDF
 * according to a custom ontology.
 * @apiParam  (Configuration)             {String}          [classificationSpec]
 * A resource location of a YML/CSV file describing a classification scheme.
 *
 * @apiSuccess                            {Boolean}   success           Returns <code>true</code> or <code>false</code>
 * indicating success of the operation.
 * @apiSuccess                            {Error[]}   errors            Array of <code>Error</code> objects.
 * @apiSuccess                            {Object}    result            An instance of <code>ProcessExecutionRecord</code>. If value of
 * <code>success</code> is <code>false</code>, <code>result</code> is <code>null</code>
 */
function transform() { return; }

/**
 * @api {post} api/v1/toolkit/interlink Interlink
 * @apiHeader {String} X-API-Key Application key
 * @apiVersion 1.0.0
 * @apiName Interlink
 * @apiGroup Toolkit
 * @apiPermission ROLE_API
 *
 * @apiDescription Executes a workflow with a single Interlink operation.
 *
 * @apiParamExample {json} Request Example
 * POST api/v1/toolkit/interlink
 * {
 *  "left": {
 *    "type":"FILESYSTEM",
 *    "path": "data.nt"
 *  },
 *  "right": {
 *    "type":"CATALOG",
 *    "id": 1,
 *    "version: 1
 *  },
 *  "profile": "SLIPO_Default"
 * }
 *
 * @apiParam                              {Object}     left
 * An instance of either <code>FileInput</code>, <code>ResourceInput</code> or <code>StepOutputInput</code>.
 * @apiParam                              {Object}     right
 * An instance of either <code>FileInput</code>, <code>ResourceInput</code> or <code>StepOutputInput</code>.
 * @apiParam                              {String}                      profile
 * Configuration profile.
 *
 * @apiParam  (FileInput)                 {String}    type
 * Input type. Must be set to <code>FILESYSTEM</code>.
 * @apiParam  (FileInput)                 {String}    path
 * A relative path to the input file.
 *
 * @apiParam  (ResourceInput)             {String}    type
 * Input type. Must be set to <code>CATALOG</code>.
 * @apiParam  (ResourceInput)             {Number}    id
 * The resource id.
 * @apiParam  (ResourceInput)             {Number}    version
 * The resource version.
 *
 * @apiParam  (StepOutputInput)           {String}    type
 * Input type. Must be set to <code>OUTPUT</code>.
 * @apiParam  (StepOutputInput)           {Number}    processId
 * The process id.
 * @apiParam  (StepOutputInput)           {Number}    processVersion
 * The process version.
 * @apiParam  (StepOutputInput)           {Number}    fileId
 * The process execution file id. A process execution may generate files of several types i.e. <code>LOG</code>,
 * <code>KPI</code>, <code>OUTPUT</code> etc. The selected file must be of type <code>OUTPUT</code>.
 *
 * Moreover, for <code>LIMES</code> output files, the file format must be <code>CSV</code>. For all other SLIPO
 * Toolkit components, the file format must be <code>N-TRIPLES</code>.
 *
 * @apiSuccess                            {Boolean}   success           Returns <code>true</code> or <code>false</code>
 * indicating success of the operation.
 * @apiSuccess                            {Error[]}   errors            Array of <code>Error</code> objects.
 * @apiSuccess                            {Object}    result            An instance of <code>ProcessExecutionRecord</code>. If value of
 * <code>success</code> is <code>false</code>, <code>result</code> is <code>null</code>
 */
function interlink() { return; }

/**
 * @api {post} api/v1/toolkit/fuse Fusion
 * @apiHeader {String} X-API-Key Application key
 * @apiVersion 1.0.0
 * @apiName Fusion
 * @apiGroup Toolkit
 * @apiPermission ROLE_API
 *
 * @apiDescription Executes a workflow with a single Fusion operation.
 *
 * @apiParamExample {json} Request Example
 * POST api/v1/toolkit/fuse
 * {
 *  "left": {
 *    "type":"FILESYSTEM",
 *    "path": "data-1.nt"
 *  },
 *  "right": {
 *    "type":"FILESYSTEM",
 *    "path": "data-2.nt"
 *  },
 *  "links": {
 *    "type":"FILESYSTEM",
 *    "path": "links.nt"
 *  },
 *  "profile": "SLIPO_Default"
 * }
 *
 * @apiParam                              {Object}     left
 * An instance of either <code>FileInput</code>, <code>ResourceInput</code> or <code>StepOutputInput</code>.
 * @apiParam                              {Object}     right
 * An instance of either <code>FileInput</code>, <code>ResourceInput</code> or <code>StepOutputInput</code>.
 * @apiParam                              {Object}     links
 * An instance of either <code>FileInput</code>, <code>ResourceInput</code> or <code>StepOutputInput</code>.
 * @apiParam                              {String}                      profile
 * Configuration profile.
 *
 * @apiSuccess                            {Boolean}   success           Returns <code>true</code> or <code>false</code>
 * indicating success of the operation.
 * @apiSuccess                            {Error[]}   errors            Array of <code>Error</code> objects.
 * @apiSuccess                            {Object}    result            An instance of <code>ProcessExecutionRecord</code>. If value of
 * <code>success</code> is <code>false</code>, <code>result</code> is <code>null</code>
 */
function fuse() { return; }

/**
 * @api {post} api/v1/toolkit/enrich Enrichment
 * @apiHeader {String} X-API-Key Application key
 * @apiVersion 1.0.0
 * @apiName Enrich
 * @apiGroup Toolkit
 * @apiPermission ROLE_API
 *
 * @apiDescription Executes a workflow with a single Enrichment operation.
 *
 * @apiParamExample {json} Request Example
 * POST api/v1/toolkit/enrich
 * {
 *  "input": {
 *    "type":"FILESYSTEM",
 *    "path": "data.nt"
 *  },
 *  "profile": "SLIPO_Default"
 * }
 *
 * @apiParam                              {Object}     input
 * An instance of either <code>FileInput</code>, <code>ResourceInput</code> or <code>StepOutputInput</code>.
 * @apiParam                              {String}                      profile
 * Configuration profile.
 *
 * @apiSuccess                            {Boolean}   success           Returns <code>true</code> or <code>false</code>
 * indicating success of the operation.
 * @apiSuccess                            {Error[]}   errors            Array of <code>Error</code> objects.
 * @apiSuccess                            {Object}    result            An instance of <code>ProcessExecutionRecord</code>. If value of
 * <code>success</code> is <code>false</code>, <code>result</code> is <code>null</code>
 */
function enrich() { return; }

/**
 * @api {post} api/v1/toolkit/export Export
 * @apiHeader {String} X-API-Key Application key
 * @apiVersion 1.0.0
 * @apiName Export
 * @apiGroup Toolkit
 * @apiPermission ROLE_API
 *
 * @apiDescription Executes a workflow with a single Export (reverse TripleGeo) operation.
 *
 * @apiParamExample {json} Request Example
 * POST api/v1/toolkit/export
 * {
 *  "input": {
 *    "type":"FILESYSTEM",
 *    "path": "data.nt"
 *  },
 *  "configuration":{
 *    "defaultLang": "en",
 *    "delimiter": "|",
 *    "encoding": "UTF-8",
 *    "outputFormat": "CSV",
 *    "profile": "SLIPO_default",
 *    "quote": "",
 *    "sourceCRS": "EPSG:4326",
 *    "sparqlFile": null,
 *    "targetCRS": "EPSG:4326",
 *  }
 * }
 *
 * @apiParam                              {Object}          path
 * An instance of either <code>FileInput</code>, <code>ResourceInput</code> or <code>StepOutputInput</code>.
 * @apiParam                              {Configuration}   configuration
 * Reverse TripleGeo configuration
 *
 * @apiParam  (Configuration)             {String}          profile
 * A profile for setting default configuration values
 * @apiParam  (Configuration)             {String}          [sparqlFile]
 * A file containing a user-specified SELECT query (in SPARQL) that will retrieve
 * results from the input RDF triples. This query should conform with the underlying
 * ontology of the input RDF triples.
 * @apiParam  (Configuration)             {String}          [delimiter]
 * A field delimiter for records (meaningful only for CSV output).
 * @apiParam  (Configuration)             {String}          [quote]
 * A quote character for records (meaningful only for CSV output)
 * @apiParam  (Configuration)             {String}          sourceCRS
 * The coordinate reference system (CRS) for input data (eg "EPSG:4326").
 * @apiParam  (Configuration)             {String}          targetCRS
 * The coordinate reference system (CRS) for output data (e.g "EPSG:4326").
 * @apiParam  (Configuration)             {String}          [defaultLang]
 * The default language for labels created in output RDF. The default is "en".
 * @apiParam  (Configuration)             {String}          [encoding]
 * Optional parameter for the encoding (character set) for strings in the input data.
 *
 * @apiParam  (Configuration)             {String}          outputFormat
 * The expected data format for output. The value can be either <code>CSV</code> or
 * <code>SHAPEFILE</code>
 *
 * @apiSuccess                            {Boolean}   success           Returns <code>true</code> or <code>false</code>
 * indicating success of the operation.
 * @apiSuccess                            {Error[]}   errors            Array of <code>Error</code> objects.
 * @apiSuccess                            {Object}    result            An instance of <code>ProcessExecutionRecord</code>. If value of
 * <code>success</code> is <code>false</code>, <code>result</code> is <code>null</code>
 */
function export_to_file() { return; }
