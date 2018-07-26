import React from 'react';

import {
  TextField,
} from '../../../../helpers/forms/form-fields/';

class MetadataConfiguration extends React.Component {

  constructor(props) {
    super(props);
  }

  render() {
    return (
      <div>
        <TextField
          {...this.props}
          id="name"
          label="Resource name"
          help="Resource name"
        />
        <TextField
          {...this.props}
          id="description"
          label="Resource description"
          help="Resource description"
        />
      </div>
    );
  }
}

export default MetadataConfiguration;
