import * as React from 'react';

import {
  FormattedTime
} from 'react-intl';

import {
  Roles,
} from '../../../../model';

import {
  SecureContent,
  Table,
} from '../../../helpers';

const templateColumns = [{
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
  Cell: () => {
    return (
      <SecureContent roles={[Roles.ADMIN, Roles.AUTHOR]}>
        <span>
          <i data-action="edit" title="Edit" className='fa fa-pencil slipo-table-row-action mr-1'></i>
          <i data-action="clone" title="Copy" className='fa fa-magic slipo-table-row-action'></i>
        </span>
      </SecureContent>
    );
  },
  style: { 'textAlign': 'center' },
  maxWidth: 60,
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

function getTemplateHistoryColumns() {
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
    Cell: () => {
      return (
        <SecureContent roles={[Roles.ADMIN, Roles.AUTHOR]}>
          <i data-action="clone" className='fa fa-magic slipo-table-row-action'></i>
        </SecureContent>
      );
    },
    style: { 'textAlign': 'center' },
    maxWidth: 60,
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

export default class Templates extends React.Component {

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
    this.props.setSelected(rowInfo.row.id, rowInfo.original.version);

    switch (e.target.getAttribute('data-action')) {
      case 'edit':
        this.props.editTemplate(rowInfo.row.id);
        break;
      case 'clone':
        this.props.cloneTemplate(rowInfo.row.id, rowInfo.original.version);
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
        name="Template explorer"
        id="template-explorer"
        columns={templateColumns}
        data={this.props.items}
        defaultPageSize={10}
        showPageSizeOptions={false}
        manual
        onPageChange={(index) => {
          this.props.setPager({ ...this.props.pager, index });
          this.props.fetchTemplates({
            query: { ...this.props.filters },
            pagingOptions: { pageIndex: index, pageSize: this.props.pager.size }
          });
        }}
        onPageSizeChange={(size) => {
          this.props.setPager({ ...this.props.pager, size });
          this.props.fetchTemplates({
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
        SubComponent={
          row => {
            if (row.original.revisions.length > 1) {
              return (
                <div>
                  <Table
                    name="Template-history-explorer"
                    id="template-history-explorer"
                    minRows={1}
                    columns={getTemplateHistoryColumns(row)}
                    data={row.original.revisions.filter((v) => v.version !== row.original.version)}
                    noDataText="No other revisions"
                    defaultPageSize={row.original.revisions.length}
                    showPagination={false}
                    getTrProps={(state, rowInfo) => ({
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
