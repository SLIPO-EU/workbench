/**
 * @api {get} api/v1/file-system Browse
 * @apiHeader {String} X-API-Key Application key
 * @apiVersion 1.0.0
 * @apiName Browse
 * @apiGroup FileSystem
 * @apiPermission ROLE_API
 *
 * @apiDescription Enumerates all files and folders
 *
 * @apiParamExample {json} Request Example
 * GET api/v1/file-system
 *
 * @apiSuccess                            {Boolean}   success           Returns <code>true</code> or <code>false</code>
 * indicating success of the operation.
 * @apiSuccess                            {Error[]}   errors            Array of <code>Error</code> objects.
 * @apiSuccess                            {Folder}    result            An instance of <code>Folder</code>. If value of
 * <code>success</code> is <code>false</code>, <code>result</code> is <code>null</code>
 *
 * @apiSuccess (Folder)                   {Number}    count             Total number of folders and files
 * @apiSuccess (Folder)                   {File[]}    files             A list of <code>File</code> objects
 * @apiSuccess (Folder)                   {Folder[]}  folders           A list of <code>Folder</code> objects.
 * @apiSuccess (Folder)                   {Number}    modified          Timestamp of the most recent update
 * @apiSuccess (Folder)                   {String}    name              Folder name
 * @apiSuccess (Folder)                   {String}    path              Relative path to the server file system
 * @apiSuccess (Folder)                   {Number}    size              Total size of all files in bytes
 *
 * @apiSuccess (File)                     {Number}    modified          Timestamp of the most recent update
 * @apiSuccess (File)                     {String}    name              File name
 * @apiSuccess (File)                     {String}    path              Relative path to the server file system
 * @apiSuccess (File)                     {String}    size              Size in bytes
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "success": true,
 *   "result": {
 *     "count": 1,
 *     "files": [{
 *       "modified": 1508427308822
 *       "name": "File 1",
 *       "path": "/File 1",
 *       "size": 229634,
 *     }],
 *     "folders": [{
 *       "count": 0,
 *       "files": [],
 *       "folders": [],
 *       "modified": 1508427308822
 *       "name": "Folder 1",
 *       "path": "/Folder 1/",
 *       "size": 0,
 *     }],
 *     "modified": 1508427308822
 *     "name": "",
 *     "path": "/",
 *     "size": 229634,
 *   }
 * }
 *
 * @apiError                              {Boolean}   success           Always <code>false</code>.
 * @apiError                              {Error[]}   errors            Array of <code>Error</code> objects.
 *
 * @apiError (Error)                      {String}    code              Unique error code.
 * @apiError (Error)                      {String}    description       Error message. Application should not present
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
function browse() { return; }

/**
 * @api {get} api/v1/file-system Download
 * @apiHeader {String} X-API-Key Application key
 * @apiVersion 1.0.0
 * @apiName DownloadFile
 * @apiGroup FileSystem
 * @apiPermission ROLE_API
 *
 * @apiDescription Downloads a file
 *
 * @apiParam (Query String Parameters)    {String}    path              The relative path of the file
 *
 * @apiParamExample {json} Request Example
 * GET api/v1/file-system?path=folder/data.csv
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
 *     description: "Path folder/data.csv was not found."
 *   }],
 *   success: false
 * }
 */
function download() { return; }

/**
 * @api {get} api/v1/file-system/upload Upload
 * @apiHeader {String} X-API-Key Application key
 * @apiHeader {String} Content-Type multipart/mixed
 * @apiVersion 1.0.0
 * @apiName UploadFile
 * @apiGroup FileSystem
 * @apiPermission ROLE_API
 *
 * @apiDescription Uploads a file
 *
 * @apiParam                              {Part}      file          The file to upload.
 * @apiParam                              {Part}      data          Upload options object serialized as a JSON string.
 *
 * @apiParam (Options)                    {String}    path          Server relative path to upload the file
 * @apiParam (Options)                    {String}    filename      Remote file name.
 * @apiParam (Options)                    {Boolean}   overwrite     <code>true</code> if the operation should overwrite
 * any existing file. If <code>overwrite</code> is set to <code>false</code> and a remote file already exists, the operation
 * will fail.
 *
 * @apiParamExample {json} Request Example
 * POST api/v1/file-system/upload
 *
 * @apiError                              {Boolean}   success       Always <code>false</code>.
 * @apiError                              {Error[]}   errors        Array of <code>Error</code> objects.
 *
 * @apiError (Error)                      {String}    code          Unique error code.
 * @apiError (Error)                      {String}    description   Error message. Application should not present
 * error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 *
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "PATH_IS_DIRECTORY",
 *     description: "File is a directory"
 *   }],
 *   success: false
 * }
 */
function upload() { return; }
