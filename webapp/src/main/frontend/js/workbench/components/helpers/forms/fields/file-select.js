import React from 'react';
import PropTypes from 'prop-types';
import ReactTable from 'react-table';
import formatFileSize from '../../../../util/file-size'; 

import decorateField from './formfield';

const fileColumns = [
  {
    Header: 'File Name',
    accessor: 'name',
  },
  {
    Header: 'File Path',
    accessor: 'path',
  },
  {
    Header: 'File Size',
    id: 'size',
    accessor: r => formatFileSize(r.size),
  },
  {
    Header: 'Created',
    id: 'createdOn',
    accessor: r => new Date(r.createdOn).toString(),
  },
];

export function FileSelect(props) {
  return (
    <ReactTable
      name={props.id} 
      id={props.id} 
      getTrProps={(state, rowInfo) => ({
        onClick: (e) => { props.onChange(rowInfo.row); },
        style: {
          background: props.value && (rowInfo.row.path === props.value.path) ? '#20a8d8' : 'white',
        }    
      })}
      defaultPageSize={props.data.length}
      showPagination={false}
      columns={fileColumns}
      data={props.data}
    />
  );
}

export default decorateField(FileSelect);


FileSelect.propTypes = {
  id: PropTypes.string.isRequired,
  data: PropTypes.array.isRequired,
  value: PropTypes.any,
  onChange: PropTypes.func.isRequired,
};

