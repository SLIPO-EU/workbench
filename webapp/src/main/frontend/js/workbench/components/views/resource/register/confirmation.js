import React from 'react';
import formatFileSize from '../../../../util/file-size';

export const Component = (props) => {
  const { type, upload, existing, external, metadata, harvester, harvesterConfig } = props.values;
  return (
    <div>
      <div>
        <ul>
          <li>Input method: {type.path.label}</li>
        </ul>
        { 
          type.path.value === 'UPLOAD' ?
            <ul>
              <li>Name: {metadata.name}</li>
              <li>Description: {metadata.description}</li>
              <li>Format: {metadata.format.label}</li>
              <li>File: {upload.file.name + ', ' + formatFileSize(upload.file.size)}</li>
              <li>File alias: {upload.name}</li>
            </ul>
            : null
        }
        {
          type.path.value === 'EXISTING' ?
            <ul>
              <li>Resources: {existing.resources.map(r => r.label).join(', ')}</li>
            </ul>
            : null
        }
        {
          type.path.value === 'EXTERNAL' ?
            <ul>
              <li>Url: {external.url}</li>
            </ul>
            : null
        }
        {
          type.path.value === 'HARVESTER' ?
            <ul>
              <li>Type: {harvester.type.label}</li>
              <li>Url: {harvester.url}</li>
              <li>Option 1: {harvesterConfig.option1}</li>
              <li>Option 2: {harvesterConfig.option2}</li>
            </ul>
            : null
        }

      </div>
    </div>
  );
};
