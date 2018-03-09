export const validator = function (values) {
  const errors = {};
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
  if (!values['targetOntology']) {
    errors['targetOntology'] = 'Required';
  }
  if (!values['attrKey']) {
    errors['attrKey'] = 'Required';
  }
  if (!values['featureName']) {
    errors['featureName'] = 'Required for constructing the resource URI';
  }

  if (values && values.inputFormat === 'CSV') {
    if (!values['delimiter']) {
      errors['delimiter'] = 'Required for CSV';
    }
    if (!values['quote']) {
      errors['quote'] = 'Required for CSV';
    }
    if (!values['attrX']) {
      errors['attrX'] = 'Required for CSV';
    }
    if (!values['attrY']) {
      errors['attrY'] = 'Required for CSV';
    }
  }

  if (Object.keys(errors).length) {
    throw errors;
  }
};
