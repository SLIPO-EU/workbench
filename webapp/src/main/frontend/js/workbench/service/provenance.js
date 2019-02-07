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


export function fetchFeatureEvolution(processId, processVersion, executionId, id, uri, token) {
  const data = {
    processId,
    processVersion,
    executionId,
    id,
    uri,
  };

  return actions.post('/action/evolution/poi', token, data);
}
