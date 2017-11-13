import * as React from 'react';
import ReactTable from 'react-table';
import { FormattedTime } from 'react-intl';
import moment from 'moment';

const processExecutionsColumns = [
  {
    Header: 'id',
    accessor: 'id',
    maxWidth: 30,
  },
  {
    Header: 'Status',
    accessorid: 'status',
    maxWidth: 90,
    Cell: row => 
      <span>
        <span style={{
          color: row.value === 'error' ? '#ff2e00'
            : row.value === 'idle' ? '#ffbf00'
              : '#57d500',
          transition: 'all .3s ease',
        }}>
          &#x25cf;
        </span> {
          row.value === 'idle' ? 'Complete'
            : row.value === 'error' ? `Failed`
              : 'COMPLETED'
        }
      </span>
  },
  {
    Header: 'Started',
    id: 'startedOn',
    accessor: r =>  <FormattedTime value={r.startedOn} day='numeric' month='numeric' year='numeric' />,
  },
  {
    Header: 'End',
    id: 'completedOn',
    accessor: r =>  <FormattedTime value={r.completedOn} day='numeric' month='numeric' year='numeric' />,
  },
  {
    Header: 'Runned for',
    id: 'dur',
    accessor: r => moment.duration(r.startedOn-r.completedOn).humanize()
  },
];


export default function ProcessExecutions(props) {
  if (!props.detailed) return <div>Tip: Select a Process to see its executions here!</div>;
  return (
    <ReactTable
      name="executions"
      id="executions"
      noDataText="No avaliable Executions for this Process!"
      columns={processExecutionsColumns}
      data={props.selectedFields}
      defaultPageSize={20}
      style={{
        height: "400px" // This will force the table body to overflow and scroll, since there is not enough room
      }}
      getTrProps={(state, rowInfo) => ({
        onClick: (e) => {
          props.setSelectedExecution({process: props.selectedProcess, execution: rowInfo.row.id});
        },
        style: {
          background: rowInfo && props.selectedExecution === rowInfo.row.id ? '#20a8d8' : null,
        }
      })}
      showPagination={false}
      className="-striped -highlight"
    />
  );
}
