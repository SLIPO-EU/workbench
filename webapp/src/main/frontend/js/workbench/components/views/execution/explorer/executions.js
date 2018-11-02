import * as React from 'react';

import moment from '../../../../moment-localized';

import {
  FormattedTime
} from 'react-intl';

import {
  CardBody,
} from 'reactstrap';

import {
  Roles,
} from '../../../../model';

import {
  EnumTaskType,
} from '../../../../model/process-designer/enum';

import {
  JobStatus,
  SecureContent,
  Table,
} from '../../../helpers';

const executionsColumns = (props) => (
  [{
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
    expander: true,
    Header: 'Actions',
    id: 'actions',
    width: 110,
    Expander: (row) => {
      const isAdmin = props.user.roles.indexOf(Roles.ADMIN) !== -1;

      return (
        <span>
          {((isAdmin) || (row.original.taskType !== EnumTaskType.REGISTRATION)) &&
            <SecureContent roles={[Roles.ADMIN, Roles.AUTHOR]}>
              <i data-action="edit" title="Edit" className='fa fa-pencil slipo-table-row-action p-1'></i>
            </SecureContent>
          }
          <i data-action="view" title="View" className='fa fa-search slipo-table-row-action p-1'></i>
          {row.original.status === 'COMPLETED' && row.original.exported &&
            <i data-action="map" title="View Map" className='fa fa-map-o slipo-table-row-action p-1'></i>
          }
          {row.original.status === 'COMPLETED' && !row.original.exported &&
            <i data-action="export-map" title="Export map data" className='fa fa-database slipo-table-row-action p-1'></i>
          }
          {row.original.errorMessage &&
            <i data-action="error" title="View error message" className='fa fa-warning slipo-table-row-action p-1'></i>
          }
          {row.original.status === 'RUNNING' &&
            <i data-action="stop" title="Stop execution" className='fa fa-stop slipo-table-row-action text-danger p-1'></i>
          }
        </span>
      );
    },
  }, {
    Header: 'Name',
    id: 'name',
    accessor: r => r.name,
    headerStyle: { 'textAlign': 'left' },
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
  }]
);


const ExecutionDetails = (props) => {
  return (
    <CardBody>
      <div className="font-weight-bold mb-2">The execution has failed with the following message:</div>
      <div className="font-weight-italic">{props.execution.errorMessage}</div>
    </CardBody>
  );
};

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
    this.props.setSelected(rowInfo.row.id);

    switch (e.target.getAttribute('data-action')) {
      case 'edit':
        this.props.editProcess(rowInfo.original.process.id);
        break;
      case 'view':
        this.props.viewExecution(rowInfo.original.process.id, rowInfo.original.process.version, rowInfo.original.id);
        break;
      case 'stop':
        this.props.stopExecution(rowInfo.original.process.id, rowInfo.original.process.version);
        break;
      case 'export-map':
        this.props.exportMap(rowInfo.original.process.id, rowInfo.original.process.version, rowInfo.original.id);
        break;
      case 'map':
        this.props.viewMap(rowInfo.original.process.id, rowInfo.original.process.version, rowInfo.original.id);
        break;
      case 'error':
        this.props.setExpanded(rowInfo.index, 'error');
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
    return (this.props.selected.id === rowInfo.row.id);
  }

  render() {
    const pages = this.props.pager && Math.ceil(this.props.pager.count / this.props.pager.size);

    return (
      <Table
        name="Workflow execution explorer"
        id="workflow-execution-explorer"
        columns={executionsColumns(this.props)}
        data={this.props.items}
        defaultPageSize={10}
        showPageSizeOptions={false}
        manual
        onPageChange={(index) => {
          this.props.setPager({ ...this.props.pager, index });
          this.props.fetchExecutions({
            query: { ...this.props.filters },
            pagingOptions: { pageIndex: index, pageSize: this.props.pager.size }
          });
        }}
        onPageSizeChange={(size) => {
          this.props.setPager({ ...this.props.pager, size });
          this.props.fetchExecutions({
            query: { ...this.props.filters },
            pagingOptions: { pageIndex: this.props.pager.index, pageSize: size }
          });
        }}
        getTrProps={(state, rowInfo) => ({
          className: (this.isSelected(rowInfo) ? 'slipo-react-table-selected' : null),
        })}
        getTdProps={(state, rowInfo, column) => ({
          onClick: this.handleRowAction.bind(this, rowInfo)
        })}
        pages={pages}
        page={this.props.pager.index}
        pageSize={this.props.pager.size}
        showPagination
        expanded={
          (this.props.selected && this.props.expanded && this.props.expanded.index !== null) ?
            {
              [this.props.expanded.index]: true
            } :
            {}
        }
        SubComponent={
          row => {
            if (this.props.selected && this.props.expanded && this.props.expanded.index === row.index) {
              switch (this.props.expanded.reason) {
                case 'error':
                  return <ExecutionDetails execution={row.original} />;
              }
            }
            return null;
          }
        }
      />
    );
  }
}
