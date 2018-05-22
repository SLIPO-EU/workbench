import {
  serializations, ontologies
} from "../model/process-designer/configuration/triplegeo";

function fromEnumValue(value, list) {
  const result = list.find((item) => ((item.value === value) || (item.label === value)));
  return (result ? result.value : value);
}

export const validator = function (values) {
  const errors = {};
  if ((!values['mappingSpec']) && (!values['profile'])) {
    errors['mappingSpec'] = 'Required';
  }
  if ((!values['classificationSpec']) && (!values['profile'])) {
    errors['classificationSpec'] = 'Required';
  }
  if (!values['inputFormat']) {
    errors['inputFormat'] = 'Required';
  }
  if (!values['mode']) {
    errors['mode'] = 'Required';
  }
  if (!values['encoding']) {
    errors['encoding'] = 'Required';
  }
  if (!values['serialization']) {
    errors['serialization'] = 'Required';
  }
  if (!values['targetGeoOntology']) {
    errors['targetGeoOntology'] = 'Required';
  }
  if (!values['attrKey']) {
    errors['attrKey'] = 'Required';
  }

  if (values && values.inputFormat !== 'CSV' && !values['attrGeometry']) {
    errors['attrGeometry'] = 'Required';
  }

  if (!values['featureSource']) {
    errors['featureSource'] = 'Required';
  }

  if (values && values.inputFormat === 'CSV') {
    if (!values['delimiter']) {
      errors['delimiter'] = 'Required for CSV';
    }
    if (!values['quote']) {
      errors['quote'] = 'Required for CSV';
    }
    if (!values['attrGeometry']) {
      if (!values['attrX']) {
        errors['attrX'] = 'Required for CSV';
      }
      if (!values['attrY']) {
        errors['attrY'] = 'Required for CSV';
      }
    }
  }

  if (Object.keys(errors).length) {
    throw errors;
  }
};

export function readConfigurationTripleGeo(config) {
  const { prefixes, namespaces, profile = null, serialization, targetGeoOntology, ...rest } = config;
  const prefixArr = prefixes ? prefixes.split(',').map(v => v.trim()) : [];
  const namespaceArr = namespaces ? namespaces.split(',').map(v => v.trim()) : [];


  return {
    ...rest,
    profile,
    serialization: fromEnumValue(serialization, serializations),
    targetGeoOntology: fromEnumValue(targetGeoOntology, ontologies),
    prefixes: prefixArr && namespaceArr && prefixArr.length === namespaceArr.length ?
      prefixArr
        .map((prefix, index) => {
          if (!prefix) {
            return null;
          }
          return { prefix, namespace: namespaceArr[index] };
        })
        .filter((item) => {
          return item !== null;
        }) : [],
  };
}

export function writeConfigurationTripleGeo(config) {
  const { prefixes, mappingSpec, classificationSpec, ...rest } = config;

  return {
    ...rest,
    prefixes: prefixes.map(v => v.prefix).join(','),
    namespaces: prefixes.map(v => v.namespace).join(','),
    mappingSpec: mappingSpec ? typeof mappingSpec === 'object' ? mappingSpec.path : mappingSpec : null,
    classificationSpec: classificationSpec ? typeof classificationSpec === 'object' ? classificationSpec.path : classificationSpec : null,
  };
}

