import React from 'react';
import { toast } from 'react-toastify';

import { MultiStep } from '../../../helpers/forms/';
import { StaticRoutes } from '../../../../model/routes';

import * as type from './type';
import * as externalUrl from './url-select';
import * as harvester from './harvester-select';
import * as harvesterConfig from './harvester-config';
import * as fileUpload from './file-upload';
import * as metadata from './metadata';
import * as triplegeo from './triplegeo';
import * as filesystem from './filesystem';
import * as confirmation from './confirmation';


export default function ResourceWizard(props) {
  return (
    <div className="animated fadeIn">
      <MultiStep
        initialActive={props.initialActive}
        onComplete={(values) => {
          if (values.type.path === 'UPLOAD' || values.type.path === 'FILESYSTEM') {
            toast.dismiss();
            toast.success(<span>Resource registration succeeded!</span>);

            const data = {
              configuration: values.triplegeo || null,
              metadata: values.metadata,
            };
            switch(values.type.path) {
              case 'UPLOAD':
                data.dataSource = {
                  type: values.type.path,
                  fileIndex: 0,
                };
                break;
              case 'FILESYSTEM':
                data.dataSource = {
                  type: values.type.path,
                  path: values.filesystem.resource.path,
                };
                break;
            }
            const file = values.upload && values.upload.file || null;

            props.createResource(data, file)
              .then(() => props.goTo(StaticRoutes.ResourceExplorer));
          }
        }}
        childrenProps={{
          saveTemp: props.saveTemp,
          clearTemp: props.clearTemp,
        }}
      >
        <type.Component
          id="type"
          title="Input mode"
          initialValue={props.initialValues.type || type.initialValue}
          validate={type.validator}
          next={value => value.path.toLowerCase()}
        />
        <externalUrl.Component
          id="external"
          title="Select external url"
          initialValue={props.initialValues.url || externalUrl.initialValue}
          validate={externalUrl.validator}
          next={() => 'confirm'}
        />
        <filesystem.Component
          id="filesystem"
          title="Select resource"
          initialValue={props.initialValues.filesystem || filesystem.initialValue}
          validate={filesystem.validator}
          next={() => 'metadata'}
          filesystem={props.filesystem}
        />

        <fileUpload.Component
          id="upload"
          title="Upload resource"
          description=""
          initialValue={props.initialValues.upload || fileUpload.initialValue}
          validate={fileUpload.validator}
          next={() => 'metadata'}
        />
        <metadata.Component
          id="metadata"
          title="Resource metadata"
          description=""
          initialValue={props.initialValues.metadata || metadata.initialValue}
          validate={metadata.validator}
          next={(value) => value.format !== 'RDF' ? 'triplegeo' : 'confirm'}
        />
        <harvester.Component
          id="harvester"
          title="Harvester"
          description=""
          initialValue={props.initialValues.harvester || harvester.initialValue}
          validate={harvester.validator}
          next={() => 'harvesterConfig'}
        />
        <harvesterConfig.Component
          id="harvesterConfig"
          title="Harvester Configuration"
          initialValue={props.initialValues.harvesterConfig || harvesterConfig.initialValue}
          validate={harvesterConfig.validator}
          next={() => 'confirm'}
        />

        <triplegeo.Component
          id="triplegeo"
          title="TripleGeo"
          initialValue={props.initialValues.triplegeo || triplegeo.initialValue}
          validate={triplegeo.validator}
          next={() => 'confirm'}
        />

        <confirmation.Component
          id="confirm"
          title="Confirm"
          description="Please confirm"
          initialValue={{}}
        />
      </MultiStep>
    </div>
  );
}
