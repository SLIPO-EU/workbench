import React from 'react';
import { DataSourceStep } from '../../../helpers/forms/';
import { StaticRoutes } from '../../../../model/routes';

import * as type from '../../resource/register/type';
import * as externalUrl from '../../resource/register/url-select';
import * as harvester from '../../resource/register/harvester-select';
import * as harvesterConfig from '../../resource/register/harvester-config';
import * as metadata from '../../resource/register/metadata';
import * as triplegeo from '../../resource/register/triplegeo';
import * as filesystem from '../../resource/register/filesystem';

export default function DataSourceConfigWizard(props) {

  switch (props.initialActive) {
    case "FILESYSTEM":
      return (
        <div className="animated fadeIn">
          <DataSourceStep
            initialActive="filesystem"
            onComplete={(values) => { props.configureStepEnd(props.stepId, values); }}
            childrenProps={{
              saveTemp: props.saveTemp,
              clearTemp: props.clearTemp,
            }}
          >
            <filesystem.Component
              id="filesystem"
              title="Select resource"
              initialValue={props.initialValues.filesystem || filesystem.initialValue}
              validate={filesystem.validator}
              next={() => 'metadata'}
              filesystem={props.filesystem}
            />
            <metadata.Component
              id="metadata"
              title="Resource metadata"
              description=""
              initialValue={props.initialValues.metadata || metadata.initialValue}
              validate={metadata.validator}
            />
          </DataSourceStep>
        </div>
      );
    case "external":
      return (
        <div className="animated fadeIn">
          <DataSourceStep
            initialActive={props.initialActive.toLowerCase()}
            onComplete={(values) => { props.configureStepEnd(props.stepId, values); }}
            childrenProps={{
              saveTemp: props.saveTemp,
              clearTemp: props.clearTemp,
            }}
          >
            <externalUrl.Component
              id="external"
              title="Select external url"
              initialValue={props.initialValues.url || externalUrl.initialValue}
              validate={externalUrl.validator}
              next={() => 'metadata'}
            />
            <metadata.Component
              id="metadata"
              title="Resource metadata"
              description=""
              initialValue={props.initialValues.metadata || metadata.initialValue}
              validate={metadata.validator}
            //next={(value) =>  'confirm'}
            />
          </DataSourceStep>
        </div>
      );
    default:
      return (
        <div className="animated fadeIn">
          <DataSourceStep
            initialActive={props.initialActive.toLowerCase()}
            onComplete={(values) => {  props.configureStepEnd(props.stepId, values); }}
            childrenProps={{
              saveTemp: props.saveTemp,
              clearTemp: props.clearTemp,
            }}
          >
            <metadata.Component
              id="metadata"
              title="Resource metadata"
              description=""
              initialValue={props.initialValues.metadata || metadata.initialValue}
              validate={metadata.validator}
            //next={(value) => value.format !== 'RDF' ? 'triplegeo' : 'confirm'}
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
          </DataSourceStep>
        </div>
      );
  }

}

