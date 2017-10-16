import React from 'react';

export default function decorateWizardFormField(Field) {
  return function(props) {
    return (
      <Field
        {...props}
        value={(props.value && props.value[props.id])}
        error={props.errors && props.errors[props.id]}
        onChange={(val) => {
          const newVal = { ...props.value };
          newVal[props.id] = val;
          props.setValue(newVal);
        }}
      />
    );
  };
}
