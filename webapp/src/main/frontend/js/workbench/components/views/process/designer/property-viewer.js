import * as React from 'react';
import PropTypes from 'prop-types';
import * as ReactRedux from 'react-redux';
import { FormattedTime } from 'react-intl';
import {
  EnumSelection,
  EnumInputType,
  EnumResourceType,
} from './constants';

import { TextField, TextAreaField } from '../../../helpers/forms/form-fields/';

function renderEmpty(props) {
  return (
    <div></div>
  );
}

function renderResource(resource, props) {
  if (resource.inputType === EnumInputType.CATALOG) {
    const values = {
      name: resource.name.toString(),
      description: resource.description,
    };

    return (
      <div style={{ marginRight: 28 }}>
        <TextField
          {...props}
          id="name"
          label="Title"
          value={values}
          readonly
        />
        <TextAreaField
          {...props}
          rows={5}
          id="description"
          label="Description"
          value={values}
          readonly
        />
      </div>
    );
  }
  return null;
}

/**
 * A presentational component for displaying selected item properties
 *
 * @class PropertyViewer
 * @extends {React.Component}
 */
class PropertyViewer extends React.Component {

  constructor(props) {
    super(props);
  }

  static propTypes = {
    item: PropTypes.object,
    type: PropTypes.string,
  };

  render() {
    const selectedItem = this.props.item;
    const props = this.props;

    if (!selectedItem) {
      return renderEmpty(props);
    }

    switch (this.props.type) {
      case EnumSelection.Resource:
      case EnumSelection.Input:
        return renderResource(selectedItem, props);
    }
  }
}

export default PropertyViewer;
