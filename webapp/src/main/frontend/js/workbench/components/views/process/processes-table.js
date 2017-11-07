import * as React from 'react';
import ReactTable from 'react-table';

import { FormattedTime } from 'react-intl';


const processColumns = [
  {
    Header: 'id',
    accessor: 'id',
    maxWidth: 30,
  },
  {
    Header: 'Status',
    id: 'status',
    accessor: () => ['idle', 'error', 'running'][Math.floor(Math.random() * 3)|0],
    maxWidth: 90,
    Cell: row => 
      <span>
        <span style={{
          color: row.value === 'error' ? '#ff2e00'
            : row.value === 'idle' ? '#ffbf00'
            : '#57d500',
          transition: 'all .3s ease',
          animation: 'blinking 1s linear infinite'
        }}>
          &#x25cf;
        </span> {
          row.value === 'idle' ? 'Complete'
          : row.value === 'error' ? `Failed`
          : 'Running'
        }
      </span>
  },
  {
    Header: 'Author',
    id: 'author',
    accessor: r => r.createdBy.name,
  },
  {
    Header: 'Name',
    id: 'name',
    accessor: r => r.name,
  },
  {
    Header: 'Description',
    accessor: 'description',
  },
  {
    Header: 'Created',
    id: 'createdOn',
    accessor: r =>  <FormattedTime value={r.createdOn} day='numeric' month='numeric' year='numeric' />,
  },
  {
    Header: 'Last Update',
    id: 'updatedOn',
    accessor: r =>  <div> By {r.updatedBy.name} at <FormattedTime value={r.updatedOn} day='numeric' month='numeric' year='numeric' /> </div>,
  },
];

export default function Processes(props) {
  const pages = props.processes && props.processes.pagingOptions && Math.ceil(props.processes.pagingOptions.count / props.processes.pagingOptions.pageSize);
  return (
    <ReactTable
      name="Process explore"
      id="process-explore"
      columns={processColumns}
      data={props.processes.items}
      defaultPageSize={10}
      showPageSizeOptions={false}
      manual
      onPageChange={(pageIndex) => {
        props.setPager({ ...props.processes.pagingOptions, pageIndex });
        props.fetchProcessData({pageIndex });
      }}
      /*onPageSizeChange={(size) => {
        props.setPager({ ...props.pager, size });
        props.fetchResources({
          pagingOptions: { pageIndex: props.pager.index, pageSize: size }
        });
      }}*/
      getTrProps={(state, rowInfo) => ({
        onClick: (e) => {
          props.setSelectedProcess(rowInfo.row.id);
        },
        style: {
          background: rowInfo && props.selectedProcess === rowInfo.row.id ? '#20a8d8' : null,
        }
      })}
      pages={pages}
      page={props.processes.pagingOptions.pageIndex}
      pageSize={props.processes.pagingOptions.pageSize}
      showPagination
      className= "-striped -highlight"
    />
  );
}

