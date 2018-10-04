import React from 'react';

export const Component = (props) => {
  const { catalog, triplegeo } = props.values;
  return (
    <div>
      <div className="float-left pr-2">
        <div className="slipo-pd-item slipo-pd-item-disabled slipo-pd-operation slipo-pd-operation">
          <div className="slipo-pd-operation-icon">
            <i className={'fa fa-cloud-download'}></i>
          </div>
          <div className="slipo-pd-item-label">
            Export Resource
          </div>
        </div>
      </div>
      <div>
        <table>
          <tbody>
            <tr>
              <td className="font-weight-bold">Name</td>
              <td className="pl-2">{catalog.resource.name}</td>
            </tr>
            <tr>
              <td className="font-weight-bold">Description</td>
              <td className="pl-2">{catalog.resource.description}</td>
            </tr>
            <tr>
              <td className="font-weight-bold">Format</td>
              <td className="pl-2">{triplegeo.outputFormat}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  );
};
