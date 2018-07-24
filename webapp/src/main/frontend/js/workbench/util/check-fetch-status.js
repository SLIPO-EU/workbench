import {
  EnumErrorLevel,
  ServerError,
} from "../model/error";

/**
 * A handler that can be chained to a fetch promise to reject non
 * successful (ie non 2xx) HTTP requests
 *
 * @param {any} res The response object
 * @returns The response if a 2xx HTTP status code is returned; Otherwise the
 * an exception is thrown
 */
export function checkStatus(res) {
  if (res.status >= 200 && res.status < 300) {
    return res;
  } else {
    throw new ServerError([{
      code: 'UNKNOWN',
      level: EnumErrorLevel.ERROR,
      description: `Received: ${res.status} ${res.statusText}`,
    }]);
  }
}
