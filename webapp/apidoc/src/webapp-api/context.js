/**
 * @api {get} api/v1/key/validate Validate
 * @apiHeader {String} X-API-Key Application key
 * @apiVersion 1.0.0
 * @apiName Validate
 * @apiGroup Context
 * @apiPermission ROLE_API
 *
 * @apiDescription Validates the request's application key
 *
 * @apiParamExample {json} Request Example
 * GET api/v1/key/validate
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
 * @apiError                              {Boolean}   success           Always <code>false</code>.
 * @apiError                              {Error[]}   errors            Array of <code>Error</code> objects.
 *
 * @apiError (Error)                      {String}    code              Unique error code.
 * @apiError (Error)                      {String}    description       Error message. Application should not present
 * error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 * @apiError (Error)                      {String}    level             Error level.
 *
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "UNREGISTERED_KEY",
 *     description: "Application key is not registered",
 *     level: "ERROR"
 *   }],
 *   success: false
 * }
 *
 */
function profiles() { return; }
