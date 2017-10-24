import React from 'react';
import formatFileSize from '../../../../util/file-size';

export const Component = (props) => {
  const { type, upload, filesystem, external, metadata, harvester, harvesterConfig } = props.values;
  return (
    <div>
      <div>
        <ul>
          <li>Input method: {type.path}</li>
        </ul>
        { 
          type.path === 'UPLOAD' ?
            <ul>
              <li>Name: {metadata.name}</li>
              <li>Description: {metadata.description}</li>
              <li>Format: {metadata.format}</li>
              <li>File: {upload.file.name + ', ' + formatFileSize(upload.file.size)}</li>
              <li>File alias: {upload.name}</li>
            </ul>
            : null
        }
        {
          type.path === 'FILESYSTEM' ?
            <ul>
              <li>Resource: {filesystem.resource.path}</li>
              <li>Name: {metadata.name}</li>
              <li>Description: {metadata.description}</li>
              <li>Format: {metadata.format}</li>
            </ul>
            : null
        }
        {
          type.path === 'EXTERNAL' ?
            <ul>
              <li>Url: {external.url}</li>
            </ul>
            : null
        }
        {
          type.path === 'HARVESTER' ?
            <ul>
              <li>Type: {harvester.type}</li>
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
