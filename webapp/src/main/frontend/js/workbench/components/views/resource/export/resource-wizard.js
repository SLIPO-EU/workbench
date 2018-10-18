import React from 'react';
import { toast } from 'react-toastify';

import ToastTemplate from '../../../helpers/toast-template';

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
          toast.dismiss();
          props.exportResource(data)
            .then(() => {
              toast.success(
                <ToastTemplate iconClass='fa-book' text='Resource export task has been initialized successfully!' />
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
