import * as React from 'react';
import PropTypes from 'prop-types';
import * as ReactRedux from 'react-redux';
import { FormattedTime } from 'react-intl';
import {
  EnumSelection,
  EnumProcessInput,
  EnumResourceType,
} from './constants';

function renderEmpty() {
  return (
    <div></div>
  );
}

function renderResource(resource) {
  if (resource.inputType === EnumProcessInput.CATALOG) {
    return (
      <div>
        <div style={{ borderBottom: '1px solid #cfd8dc', padding: 11 }}>
          Properties
        </div>
        <div className="slipo-pd-resource-details">
          <div className="slipo-pd-resource-details-label text-muted">
            Id / Version
          </div>
          <div className="slipo-pd-resource-details-value mb-3">
            {resource.id} / {resource.version}
          </div>
          <div className="slipo-pd-resource-details-label text-muted">
            Title
          </div>
          <div className="slipo-pd-resource-details-value mb-3">
            {resource.title}
          </div>
          <div className="slipo-pd-resource-details-label text-muted">
            Description
          </div>
          <div className="slipo-pd-resource-details-value mb-3">
            {resource.description}
          </div>
        </div>
      </div>
    );
  }
  return renderEmpty();
}
/**
 * A presentational component for displaying selected item details
 *
 * @class Details
 * @extends {React.Component}
 */
class Details extends React.Component {

  constructor(props) {
    super(props);
  }

  static propTypes = {
    item: PropTypes.object,
    type: PropTypes.string,
  };

  render() {
    const selectedItem = this.props.item;
    if (selectedItem) {
      switch (this.props.type) {
        case EnumSelection.Resource:
        case EnumSelection.Input:
          return renderResource(selectedItem);
      }
    }
    return renderEmpty();
  }
}

export default Details;
