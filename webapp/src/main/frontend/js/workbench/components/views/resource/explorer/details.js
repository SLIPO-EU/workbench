import * as React from 'react';
import ReactTable from 'react-table';
import { FormattedTime } from 'react-intl';

const resourceDetailsColumns = [
  {
    Header: 'Field',
    accessor: 'field',
    maxWidth: 180,
  },
  {
    Header: 'Value',
    accessor: 'value',
    Cell: props => props.value && typeof props.value === 'object' ?
      <ReactTable
        name="Resource explore"
        id="resource-explore"
        columns={resourceDetailsColumns}
        data={Object.keys(props.value).map((key) => ({
          field: key,
          value: props.value[key],
        }))}
        defaultPageSize={Object.keys(props.value).length}
        showPagination={false}
        className="-striped -highlight"
      />
      : <b>
        {
          (props.original.field === 'createdOn' || props.original.field === 'updatedOn') ?
            <FormattedTime value={props.value} day='numeric' month='numeric' year='numeric' />
            :
            props.value
        }
      </b>
  },
];

export default function ResourceDetails(props) {
  const selectedResource = props.resources && props.resources.find(r => r.id === props.detailed);
  if (!selectedResource) return <div>-</div>;

  if (selectedResource.version !== props.selectedResourceVersion){
    const selectedResource2 = selectedResource.versions.find(r => r.version === props.selectedResourceVersion);
    const selectedResourceFields = Object.keys(selectedResource2).map((key) => ({
      field: key,
      value: selectedResource2[key],
    }));
    return (
      <ReactTable
        name="Resource explore"
        id="resource-explore"
        columns={resourceDetailsColumns}
        data={selectedResourceFields}
        defaultPageSize={selectedResourceFields.length}
        showPagination={false}
        className="-striped -highlight"
      />
    );
  }

  const selectedResourceFields = Object.keys(selectedResource).map((key) => ({
    field: key,
    value: selectedResource[key],
  }));
  return (
    <ReactTable
      name="Resource explore"
      id="resource-explore"
      columns={resourceDetailsColumns}
      data={selectedResourceFields}
      defaultPageSize={selectedResourceFields.length}
      showPagination={false}
      className="-striped -highlight"
    />
  );

}
