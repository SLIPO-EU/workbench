import React from 'react';

export default function decorateFormField(Field) {
  return function (props) {
    return (
      <Field
        {...props}
        value={(props.value && props.value[props.id])}
        error={props.errors && props.errors[props.id]}
        onChange={(val) => {
          if (typeof props.onChange === 'function') {
            // Parent component is explicitly handling the onChange event
            // Do not propagate any side effects
            props.onChange(val);
            return;
          }
          const newVal = { ...props.value };
          newVal[props.id] = val;
          props.setValue(newVal);
        }}
      />
    );
  };
}
