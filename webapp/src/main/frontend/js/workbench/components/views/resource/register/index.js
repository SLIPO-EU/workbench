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
import * as resource from './resource-select';
import * as confirmation from './confirmation';


export default function ResourceWizard(props) {
  return (
    <div className="animated fadeIn">
      <MultiStep
        initialActive={props.initialActive}
        onComplete={(values) => { 
          if (values.type.path.value === 'UPLOAD') {
            toast.dismiss();
            toast.success(<span>Resource registration succeeded!</span>);

            props.createResource({ 
              id: values.metadata.name, 
              name: values.metadata.name, 
              description: values.metadata.description, 
              format: values.metadata.format.label, 
            })
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
          next={value => value.path.value.toLowerCase()}
        />
        <externalUrl.Component
          id="external"
          title="Select external url"
          initialValue={props.initialValues.url || externalUrl.initialValue}              
          validate={externalUrl.validator}
          next={() => 'confirm'}
        />
        <resource.Component
          id="existing"
          title="Select resource"
          initialValue={props.initialValues.resource || resource.initialValue}
          validate={resource.validator}
          next={() => 'confirm'}
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
          next={(value) => value.format && value.format.value !== 'RDF' ? 'triplegeo' : 'confirm'}
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
