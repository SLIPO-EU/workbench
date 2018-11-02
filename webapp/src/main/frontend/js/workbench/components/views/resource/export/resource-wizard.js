import React from 'react';

import { MultiStep } from '../../../helpers/forms/';
import { EnumErrorLevel } from '../../../../model/error';
import { StaticRoutes } from '../../../../model/routes';
import { defaultReverseValues as defaultTripleGeoValues } from '../../../../model/process-designer/configuration/triplegeo';

import * as resourceSelect from '././resource-select';
import * as confirmation from './confirmation';

import { default as TripleGeoConfiguration } from '../../process/designer/configuration/triplegeo-reverse';

import {
  validateConfiguration as validateConfigurationTripleGeo,
  writeConfiguration as writeConfigurationTripleGeo,
} from '../../../../service/toolkit/triplegeo-reverse';

import {
  message,
} from '../../../../service';

const resourceToInput = (r) => ({
  key: "0",
  inputType: r.inputType,
  resourceType: r.resourceType,
  name: r.name,
  description: r.description,
  resource: {
    id: r.id,
    version: r.version,
  },
});

export default function ResourceWizard(props) {
  return (
    <div className="animated fadeIn">
      <MultiStep
        initialActive={props.initialActive}
        onComplete={(values) => {
          const data = {
            configuration: values.triplegeo ? writeConfigurationTripleGeo(values.triplegeo) : null,
            resource: values.catalog.resource ? resourceToInput(values.catalog.resource) : null,
          };
          props.exportResource(data)
            .then(() => {
              message.success('Resource export task has been initialized successfully!', 'fa-book');
              props.goTo(StaticRoutes.ResourceExplorer);
            })
            .catch((err) => {
              switch (err.level) {
                case EnumErrorLevel.WARN:
                  message.warn(err.message, 'fa-warning');
                  props.goTo(StaticRoutes.ResourceExplorer);
                  break;
                default:
                  message.error(err.message, 'fa-warning');
                  break;
              }
            });
        }}
        childrenProps={{
          saveTemp: props.saveTemp,
          clearTemp: props.clearTemp,
        }}
      >
        <resourceSelect.Component
          id="catalog"
          title="Select resource"
          initialValue={props.initialValues.catalog || resourceSelect.initialValue}
          validate={resourceSelect.validator}
          next={() => 'triplegeo'}
        />

        <TripleGeoConfiguration
          id="triplegeo"
          title="TripleGeo"
          initialValue={props.initialValues.configuration || { ...defaultTripleGeoValues, version: props.appConfiguration.tripleGeo.version }}
          validate={validateConfigurationTripleGeo}
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
