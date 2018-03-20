import * as React from 'react';
import ReactTable from 'react-table';
import moment from 'moment';

import {
  FormattedTime
} from 'react-intl';

import {
  JobStatus,
  Table,
} from '../../../helpers';

const processExecutionsColumns = [{
  Header: 'id',
  accessor: 'id',
  maxWidth: 30,
  show: false,
}, {
  Header: 'Status',
  accessor: 'status',
  maxWidth: 120,
  Cell: row => {
    return (
      <JobStatus status={row.value} />
    );
  },
  headerStyle: { 'textAlign': 'center' },
  style: { 'textAlign': 'center' },
}, {
  Header: 'Actions',
  id: 'actions',
  Cell: props => {
    return (
      <span>
        <i data-action="view" className='fa fa-search slipo-table-row-action p-1'></i>
        <i data-action="map" className='fa fa-map-o slipo-table-row-action p-1'></i>
        {props.row.status === 'RUNNING' &&
          <i data-action="stop" className='fa fa-stop slipo-table-row-action text-danger p-1'></i>
        }
      </span>
    );
  },
  width: 80,
  style: { 'textAlign': 'center' },
}, {
  Header: 'Submitted By',
  id: 'submittedBy',
  accessor: r => (r.submittedBy ? r.submittedBy.name : '-'),
  headerStyle: { 'textAlign': 'center' },
  style: { 'textAlign': 'center' },
}, {
  Header: 'Submitted On',
  id: 'submittedOn',
  accessor: r => <FormattedTime value={r.submittedOn} day='numeric' month='numeric' year='numeric' />,
  headerStyle: { 'textAlign': 'center' },
  style: { 'textAlign': 'center' },
}, {
  Header: 'Started',
  id: 'startedOn',
  accessor: r => (r.startedOn ? <FormattedTime value={r.startedOn} day='numeric' month='numeric' year='numeric' /> : '-'),
  headerStyle: { 'textAlign': 'center' },
  style: { 'textAlign': 'center' },
}, {
  Header: 'End',
  id: 'completedOn',
  accessor: r => (r.completedOn ? <FormattedTime value={r.completedOn} day='numeric' month='numeric' year='numeric' /> : '-'),
  headerStyle: { 'textAlign': 'center' },
  style: { 'textAlign': 'center' },
}, {
  Header: 'Duration',
  id: 'dur',
  accessor: r => (r.startedOn && r.completedOn ? moment.duration(r.startedOn - r.completedOn).humanize() : '-'),
  headerStyle: { 'textAlign': 'center' },
  style: { 'textAlign': 'center' },
}];

export default class ProcessExecutions extends React.Component {

  constructor(props) {
    super(props);
  }

  /**
   * Handles row actions
   *
   * @param {any} rowInfo the rowInfo object for the selected row
   * @param {any} e react synthetic event instance
   * @param {any} handleOriginal the table's original event handler
   * @memberof Resources
   */
  handleRowAction(rowInfo, e, handleOriginal) {
    switch (e.target.getAttribute('data-action')) {
      case 'view':
        this.props.viewExecution(this.props.selected.id, this.props.selected.version, rowInfo.row.id);
        break;
      case 'map':
        this.props.viewMap(this.props.selected.id, this.props.selected.version, rowInfo.row.id);
        break;
      case 'stop':
        this.props.stopExecution(rowInfo.row.id);
        break;
      default:
        if (handleOriginal) {
          handleOriginal();
        }
        break;
    }
  }

  isSelected(rowInfo) {
    if (!rowInfo || !this.props.selected) {
      return false;
    }
    return (this.props.selected.execution === rowInfo.row.id);
  }

  render() {
    if (!this.props.selected) {
      return null;
    }

    return (
      <Table
        name="executions"
        id="executions"
        noDataText="No available Executions for this Process!"
        columns={processExecutionsColumns}
        data={this.props.executions}
        defaultPageSize={10}
        showPageSizeOptions={false}
        manual
        getTrProps={(state, rowInfo) => ({
          className: (this.isSelected(rowInfo) ? 'slipo-react-table-selected' : null),
        })}
        getTdProps={(state, rowInfo, column) => ({
          onClick: this.handleRowAction.bind(this, rowInfo)
        })}
        showPagination={false}
      />
    );
  }
}
