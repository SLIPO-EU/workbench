/**
 * @api {post} action/file-system Browse folder
 * @apiVersion 1.0.0
 * @apiName Browse
 * @apiGroup File System
 * @apiPermission ROLE_USER
 *
 * @apiDescription Enumerates files and folders for the specified path
 *
 * @apiParam (Query String Parameters)    {String}    path              The path for which files and folders will be
 * returned. In order to browse the root folder, the value of parameter <code>path</code> must be set to empty or `/`
 *
 * @apiParamExample {json} Request Example
 * GET action/file-system?path=/
 *
 * @apiSuccess                            {Boolean}   success           Returns <code>true</code> or <code>false</code>
 * indicating success of the operation.
 * @apiSuccess                            {Object[]}  errors            Array of <code>Error</code>
 * @apiSuccess                            {Object}    result            An instance of <code>Folder</code>. If value of
 * <code>success</code> is <code>false</code>,
 * <code>result</code> is <code>undefined</code>
 *
 * @apiSuccess (Folder)                   {String}    name              Folder name
 * @apiSuccess (Folder)                   {String}    path              Relative path to the server file system
 * @apiSuccess (Folder)                   {Number}    count             Total number of folders and files
 * @apiSuccess (Folder)                   {Number}    size              Total size of all files
 * @apiSuccess (Folder)                   {Number}    createdOn         Creation timestamp
 * @apiSuccess (Folder)                   {Object[]}  files             A list of <code>File</code> objects
 * @apiSuccess (Folder)                   {Object[]}  folders           A list of <code>Folder</code> objects. The
 * properties <code>files</code> and <code>folders</code> are empty (no recursion is supported) and the value for
 * properties <code>count</code> and <code>size</code> is set to <code>0</code>
 *
 * @apiSuccess (File)                     {String}    size              Size
 * @apiSuccess (File)                     {String}    name              File name
 * @apiSuccess (File)                     {String}    path              Relative path to the server file system
 * @apiSuccess (File)                     {Number}    createdOn         Creation timestamp
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "success": true,
 *   "result": {
 *     "name": "",
 *     "path": "/",
 *     "files": [{
 *       "size": 229634,
 *       "name": "File 1",
 *       "path": "/File 1",
 *       "createdOn": 1508427308822
 *     }],
 *     "folders": [{
 *       "name": "Folder 1",
 *       "path": "/Folder 1/",
 *       "files": [],
 *       "folders": [],
 *       "count": 0,
 *       "size": 0,
 *       "createdOn": 1508427308822
 *     }],
 *     "count": 1,
 *     "size": 229634,
 *     "createdOn": 1508427308822
 *   }
 * }
 *
 * @apiError                              {Boolean}   success           Always <code>false</code>.
 * @apiError                              {Object[]}  errors            Array of <code>Error</code> objects.
 *
 * @apiError (Error)                      {String} code                 Unique error code.
 * @apiError (Error)                      {String} description          Error message. Application should not present
 * error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 *
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "FileSystemErrorCode.PATH_NOT_FOUND",
 *     description: "Path / was not found."
 *   }],
 *   success: false
 * }
 *
 */
function browse() { return; }
