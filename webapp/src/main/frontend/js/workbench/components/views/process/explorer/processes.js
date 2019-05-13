import * as React from 'react';

import {
  FormattedTime
} from 'react-intl';

import {
  Roles,
} from '../../../../model';

import {
  EnumTaskType
} from '../../../../model/process-designer';

import {
  SecureContent,
  Table,
} from '../../../helpers';

const processColumns = [{
  expander: true,
  Header: 'Version',
  width: 60,
  Expander: ({ isExpanded, ...rest }) => {
    if (rest.original.revisions.length > 1) {
      return (
        <div>
          {!isExpanded ? rest.original.version : <i className="fa fa-code-fork" ></i>}
        </div>
      );
    }
    else {
      return <div>{rest.original.version} </div>;
    }
  },
  style: { 'textAlign': 'center' },
}, {
  accessor: 'id',
  show: false,
}, {
  accessor: 'version',
  show: false,
}, {
  Header: 'Actions',
  id: 'actions',
  Cell: props => {
    return (
      <span>
        {props.original.taskType !== EnumTaskType.EXPORT_MAP &&
          <i data-action="view" title="View" className='fa fa-search slipo-table-row-action p-1'></i>
        }
        {props.original.taskType !== EnumTaskType.EXPORT_MAP &&
          <SecureContent roles={[Roles.ADMIN, Roles.AUTHOR]}>
            <i data-action="edit" title="Edit" className='fa fa-pencil slipo-table-row-action p-1'></i>
          </SecureContent>
        }
        {!props.original.running &&
          <i data-action="play" title="Start execution" className='fa fa-play slipo-table-row-action text-success p-1'></i>
        }
        {props.original.running &&
          <i data-action="stop" title="Stop execution" className='fa fa-stop slipo-table-row-action text-danger p-1'></i>
        }
      </span>
    );
  },
  maxWidth: 80,
}, {
  Header: 'Name',
  id: 'name',
  accessor: r => r.name,
  headerStyle: { 'textAlign': 'left' },
}, {
  Header: 'Description',
  accessor: 'description',
  headerStyle: { 'textAlign': 'left' },
}, {
  Header: 'Modified By',
  id: 'createdBy',
  accessor: r => r.updatedBy.name,
  headerStyle: { 'textAlign': 'center' },
  style: { 'textAlign': 'center' },
  maxWidth: 160,
}, {
  Header: 'Updated',
  id: 'updatedOn',
  accessor: r => <FormattedTime value={r.updatedOn} day='numeric' month='numeric' year='numeric' />,
  style: { 'textAlign': 'center' },
  maxWidth: 160,
}];

function getProcessHistoryColumns(parent) {
  return [{
    accessor: 'id',
    show: false,
  }, {
    Header: 'Version',
    accessor: 'version',
    maxWidth: 60,
    style: { 'textAlign': 'center' },
  }, {
    Header: 'Actions',
    id: 'actions',
    Cell: props => {
      return (
        <span>
          {
            parent.row.version === props.row.version
              ?
              <SecureContent roles={[Roles.ADMIN, Roles.AUTHOR]}>
                <i data-action="edit" title="Edit" className='fa fa-pencil slipo-table-row-action p-1'></i>
              </SecureContent>
              :
              <i data-action="view" title="View" className='fa fa-search slipo-table-row-action p-1'></i>
          }
          {!props.original.running &&
            <i data-action="play" title="Start execution" className='fa fa-play slipo-table-row-action text-success p-1'></i>
          }
          {props.original.running &&
            <i data-action="stop" title="Stop execution" className='fa fa-stop slipo-table-row-action text-danger p-1'></i>
          }
        </span>
      );
    },
    maxWidth: 80,
  }, {
    Header: 'Description',
    accessor: 'description',
  }, {
    Header: 'Modified By',
    id: 'createdBy',
    accessor: r => r.updatedBy.name,
    headerStyle: { 'textAlign': 'center' },
    style: { 'textAlign': 'center' },
    maxWidth: 160,
  }, {
    Header: 'Updated',
    id: 'updatedOn',
    accessor: r => <FormattedTime value={r.updatedOn} day='numeric' month='numeric' year='numeric' />,
    style: { 'textAlign': 'center' },
    maxWidth: 160,
  }];
}

export default class Processes extends React.Component {

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
      case 'edit':
        this.props.editProcess(rowInfo.row.id);
        break;
      case 'play':
        this.props.startExecution(rowInfo.row.id, rowInfo.row.version);
        break;
      case 'stop':
        this.props.stopExecution(rowInfo.row.id, rowInfo.row.version);
        break;
      case 'view':
        this.props.viewProcess(rowInfo.row.id, rowInfo.row.version);
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
    return this.props.selected.id === rowInfo.row.id && this.props.selected.version === rowInfo.row.version;
  }

  render() {
    const pages = this.props.pager && Math.ceil(this.props.pager.count / this.props.pager.size);

    return (
      <Table
        name="Process explorer"
        id="process-explorer"
        columns={processColumns}
        data={this.props.items}
        defaultPageSize={10}
        showPageSizeOptions={false}
        manual
        onPageChange={(index) => {
          this.props.setPager({ ...this.props.pager, index });
          this.props.fetchProcesses({
            query: { ...this.props.filters },
            pagingOptions: { pageIndex: index, pageSize: this.props.pager.size }
          });
        }}
        onPageSizeChange={(size) => {
          this.props.setPager({ ...this.props.pager, size });
          this.props.fetchProcesses({
            query: { ...this.props.filters },
            pagingOptions: { pageIndex: this.props.pager.index, pageSize: size }
          });
        }}
        getTrProps={(state, rowInfo) => ({
          onClick: () => {
            this.props.fetchProcessExecutions(rowInfo.row.id, rowInfo.row.version);
          },
          className: (this.isSelected(rowInfo) ? 'slipo-react-table-selected' : null),
        })}
        getTdProps={(state, rowInfo, column) => ({
          onClick: this.handleRowAction.bind(this, rowInfo)
        })}
        pages={pages}
        page={this.props.pager.index}
        pageSize={this.props.pager.size}
        showPagination
        SubComponent={
          row => {
            if (row.original.revisions.length > 1) {
              return (
                <div>
                  <Table
                    name="Process-history-explorer"
                    id="process-history-explorer"
                    minRows={1}
                    columns={getProcessHistoryColumns(row)}
                    data={row.original.revisions.filter((v) => v.version !== row.original.version)}
                    noDataText="No other revisions"
                    defaultPageSize={row.original.revisions.length}
                    showPagination={false}
                    getTrProps={(state, rowInfo) => ({
                      onClick: () => {
                        this.props.fetchProcessExecutions(rowInfo.row.id, rowInfo.row.version);
                      },
                      className: (this.isSelected(rowInfo) ? 'slipo-react-table-selected' : 'slipo-react-table-child-row'),
                      style: {
                        lineHeight: 0.8,
                      },
                    })}
                    getTdProps={(state, rowInfo, column) => ({
                      onClick: this.handleRowAction.bind(this, rowInfo)
                    })}
                  />
                </div>
              );
            }
            else {
              return null;
            }
          }
        }
      />
    );
  }
}
