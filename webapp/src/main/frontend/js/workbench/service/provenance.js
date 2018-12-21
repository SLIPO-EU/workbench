import actions from './api/fetch-actions';

export function fetchFeatureProvenance(processId, processVersion, executionId, outputKey, id, uri, token) {
  const data = {
    processId,
    processVersion,
    executionId,
    outputKey,
    id,
    uri,
  };

  return actions.post('/action/provenance/poi', token, data);
}
