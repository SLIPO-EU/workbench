import React from 'react';

import formatFileSize from '../../../../util/file-size';

function renderUpload(metadata, triplegeo, upload) {
  return (
    <div>
      <div className="float-left pr-2">
        <div className="slipo-pd-item slipo-pd-item-disabled slipo-pd-operation slipo-pd-operation">
          <div className="slipo-pd-operation-icon">
            <i className={'fa fa-cloud-upload'}></i>
          </div>
          <div className="slipo-pd-item-label">
            Upload
          </div>
        </div>
      </div>
      <div>
        <table>
          <tbody>
            <tr>
              <td className="font-weight-bold">Name</td>
              <td className="pl-2">{metadata.name}</td>
            </tr>
            <tr>
              <td className="font-weight-bold">Description</td>
              <td className="pl-2">{metadata.description}</td>
            </tr>
            <tr>
              <td className="font-weight-bold">Format</td>
              <td className="pl-2">{triplegeo.inputFormat}</td>
            </tr>
            <tr>
              <td className="font-weight-bold">File</td>
              <td className="pl-2">{upload.file.name + ', ' + formatFileSize(upload.file.size)}</td>
            </tr>
            <tr>
              <td className="font-weight-bold">File alias</td>
              <td className="pl-2">{upload.name}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  );
}

function renderFileSystem(metadata, triplegeo, filesystem) {
  return (
    <div>
      <div className="float-left pr-2">
        <div className="slipo-pd-item slipo-pd-item-disabled slipo-pd-operation slipo-pd-operation">
          <div className="slipo-pd-operation-icon">
            <i className={'fa fa-folder'}></i>
          </div>
          <div className="slipo-pd-item-label">
            File System
          </div>
        </div>
      </div>
      <div>
        <table>
          <tbody>
            <tr>
              <td className="font-weight-bold">Resource</td>
              <td className="pl-2">{filesystem.resource.path}</td>
            </tr>
            <tr>
              <td className="font-weight-bold">Name</td>
              <td className="pl-2">{metadata.name}</td>
            </tr>
            <tr>
              <td className="font-weight-bold">Description</td>
              <td className="pl-2">{metadata.description}</td>
            </tr>
            <tr>
              <td className="font-weight-bold">Format</td>
              <td className="pl-2">{triplegeo.inputFormat}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div >
  );
}

export const Component = (props) => {
  const { type, triplegeo, upload, filesystem, external, metadata } = props.values;
  return (
    <div>
      <div>

        {
          type.path === 'UPLOAD' ?
            renderUpload(metadata, triplegeo, upload)
            : null
        }

        {
          type.path === 'FILESYSTEM' ?
            renderFileSystem(metadata, triplegeo, filesystem)
            : null
        }

      </div>
    </div>
  );
};
