This document describes the SLIPO API Interface.

All API requests must set the HTTP header `X-API-Key` to a valid application key value.

The API may return the following error codes:

<table>
  <thead>
    <th>Error Code</th>
    <th>Description</th>
  </thead>
  </tbody>
  <tr>
    <td>QUERY_IS_EMPTY</td>
    <td>Query object is not set</td>
  </tr>
  <tr>
    <td>PATH_IS_EMPTY</td>
    <td>Path parameter is not set</td>
  </tr>
  <tr>
    <td>PATH_NOT_FOUND</td>
    <td>Path was not found</td>
  </tr>
  <tr>
    <td>FILE_NOT_FOUND</td>
    <td>File was not found</td>
  </tr>
  <tr>
    <td>IO_ERROR</td>
    <td>An I/O error has occurred</td>
  </tr>
  <tr>
    <td>PROCESS_NOT_FOUND</td>
    <td>Workflow definition was not found</td>
  </tr>
  <tr>
    <td>EXECUTION_NOT_FOUND</td>
    <td>Workflow execution was not found</td>
  </tr>
  <tr>
    <td>FAILED_TO_START</td>
    <td>Workflow execution failed to start</td>
  </tr>
  <tr>
    <td>FAILED_TO_STOP</td>
    <td>Workflow execution failed to stop</td>
  </tr>
  <tr>
    <td>RPC_SERVER_UNREACHABLE</td>
    <td>The Workbench application failed to connect to the SLIPO Service</td>
  </tr>
  <tr>
    <td>PROFILE_NOT_FOUND</td>
    <td>SLIPO Toolkit component profile was not found</td>
  </tr>
  <tr>
    <td>RESOURCE_NOT_FOUND</td>
    <td>Catalog resource was not found</td>
  </tr>
  <tr>
    <td>EMPTY_PATH</td>
    <td><code>FileInput</code> path is empty</td>
  </tr>
  <tr>
    <td>RELATIVE_PATH_REQUIRED</td>
    <td><code>FileInput</code> path is not relative</td>
  </tr>
  <tr>
    <td>OUTPUT_FILE_NOT_FOUND</td>
    <td><code>StepOutputInput</code> file was not found</td>
  </tr>
  <tr>
    <td>FILE_TYPE_NOT_SUPPORTED</td>
    <td><code>StepOutputInput</code> file type is not supported</td>
  </tr>
  <tr>
    <td>UNKNOWN</td>
    <td>An internal server error has occurred</td>
  </tr>
  </tbody>
</table>
