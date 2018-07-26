import React from 'react';
import { toast } from 'react-toastify';

import ToastTemplate from '../../../helpers/toast-template';

import { MultiStep } from '../../../helpers/forms/';
import { EnumErrorLevel } from '../../../../model/error';
import { StaticRoutes } from '../../../../model/routes';
import { defaultValues as defaultTripleGeoValues } from '../../../../model/process-designer/configuration/triplegeo';
import { defaultMetadataValue } from '../../../../model/process-designer/configuration/metadata';

import * as type from './type';
import * as externalUrl from './url-select';
import * as harvester from './harvester-select';
import * as harvesterConfig from './harvester-config';
import * as fileUpload from './file-upload';
import * as filesystem from './filesystem';
import * as confirmation from './confirmation';

import { default as TripleGeoConfiguration } from '../../process/designer/configuration/triplegeo';
import { default as MetadataConfiguration } from '../../process/designer/configuration/metadata';

import {
  validateConfiguration as validateTripleGeo,
} from '../../../../service/toolkit/triplegeo';

import {
  validateConfiguration as validateMetadata,
} from '../../../../service/toolkit/metadata';

import { writeConfiguration as writeConfigurationTripleGeo } from '../../../../service/toolkit/triplegeo';

export default function ResourceWizard(props) {
  return (
    <div className="animated fadeIn">
      <MultiStep
        initialActive={props.initialActive}
        onComplete={(values) => {
          if (values.type.path === 'UPLOAD' || values.type.path === 'FILESYSTEM') {
            const data = {
              settings: values.triplegeo ? writeConfigurationTripleGeo(values.triplegeo) : null,
              metadata: values.metadata,
            };
            switch (values.type.path) {
              case 'FILESYSTEM':
                data.dataSource = {
                  type: values.type.path,
                  path: values.filesystem.resource.path,
                };
                break;
            }
            const file = values.upload && values.upload.file || null;

            toast.dismiss();
            props.createResource(data, file)
              .then(() => {
                toast.success(
                  <ToastTemplate iconClass='fa-book' text='Resource registration has been initialized successfully!' />
                );
                props.goTo(StaticRoutes.ResourceExplorer);
              })
              .catch((err) => {
                switch (err.level) {
                  case EnumErrorLevel.WARN:
                    toast.warn(
                      <ToastTemplate iconClass='fa-warning' text={err.message} />
                    );
                    props.goTo(StaticRoutes.ResourceExplorer);
                    break;
                  default:
                    toast.error(
                      <ToastTemplate iconClass='fa-warning' text={err.message} />
                    );
                    break;
                }
              });
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
        <MetadataConfiguration
          id="metadata"
          title="Resource metadata"
          description=""
          initialValue={props.initialValues.metadata || { ...defaultMetadataValue }}
          validate={validateMetadata}
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

        <TripleGeoConfiguration
          id="triplegeo"
          title="TripleGeo"
          initialValue={props.initialValues.triplegeo || { ...defaultTripleGeoValues, version: props.appConfiguration.tripleGeo.version }}
          validate={validateTripleGeo}
          next={() => 'confirm'}
          appConfiguration={props.appConfiguration}
          filesystem={props.filesystem}
          createFolder={props.createFolder}
          uploadFile={props.uploadFile}
          deletePath={props.deletePath}
          readOnly={false}
        />

        <confirmation.Component
          id="confirm"
          title="Confirm"
          description="Please confirm"
          initialValue={{}}
        />
      </MultiStep>
    </div >
  );
}
