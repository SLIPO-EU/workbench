import * as React from 'react';

import {
  FormattedTime
} from 'react-intl';

import {
  Link
} from 'react-router-dom';

import {
  formatFileSize
} from '../../../../util';

import {
  DynamicRoutes,
  buildPath
} from '../../../../model/routes';

import {
  EnumInputType,
  EnumMapExportStatus,
  ResourceTypeIcons,
} from '../../../../model/process-designer';

import {
  Table
} from '../../../helpers';

/**
 * Creates a plain JavaScript object representing a catalog resource
 *
 * @param {any} rowInfo the rowInfo object for the selected row
 * @returns a plain JavaScript object
 */
function createResource(rowInfo) {
  return {
    inputType: EnumInputType.CATALOG,
    resourceType: rowInfo.original.type,
    id: rowInfo.original.id,
    version: rowInfo.original.version,
    name: `${rowInfo.original.metadata.name} (ver. ${rowInfo.original.version})`,
    description: rowInfo.original.metadata.description,
    iconClass: ResourceTypeIcons[rowInfo.original.type],
  };
}

const resourceColumns = [{
  Header: '',
  id: 'selection',
  Cell: props => {
    if (props.original.selected) {
      return (
        <i data-action="remove-from-bag" title="Remove from selected resources" className='fa fa-check-square-o slipo-table-row-action'></i>
      );
    } else {
      return (
        <i data-action="add-to-bag" title="Add to selected resources" className='fa fa-square-o slipo-table-row-action'></i>
      );
    }
  },
  style: { 'textAlign': 'center' },
  maxWidth: 60
}, {
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
  show: false
}, {
  accessor: 'version',
  show: false,
}, {
  Header: 'Actions',
  id: 'actions',
  className: 'd-flex',
  Cell: props => {
    const record = props.original;

    let action = null;
    switch (record.exportStatus) {
      case EnumMapExportStatus.NONE:
        if (record.tableName) {
          // Resource has been exported during the export of a process execution
          action = <i data-action="view-map" title="View Map" className='fa fa-map-o slipo-table-row-action'></i>;
        } else {
          action = <i data-action="export-map" title="Export map data" className='fa fa-database slipo-table-row-action'></i>;
        }
        break;
      case EnumMapExportStatus.FAILED:
        action = (
          <i data-action="export-map" title="Export map data. Last execution has failed" className='fa fa-database slipo-table-row-action invalid-feedback'></i>
        );
        break;
      case EnumMapExportStatus.COMPLETED:
        action = <i data-action="view-map" title="View Map" className='fa fa-map-o slipo-table-row-action'></i>;
        break;
      default:
        action = <i title="Export operation in progress ..." className='fa fa-cogs'></i>;
        break;
    }

    return (
      <React.Fragment>
        <i data-action="delete" className='fa fa-trash slipo-table-row-action mr-2' title="Delete"></i>
        <i data-action="export-to-file" className='fa fa-archive slipo-table-row-action mr-2' title="Export to file"></i>
        {action}
      </React.Fragment>
    );
  },
  style: { 'justifyContent': 'center' },
  maxWidth: 90,
}, {
  Header: 'Name',
  id: 'name',
  accessor: r => r.metadata.name,
  Cell: props => {
    return (
      <Link to={buildPath(DynamicRoutes.ResourceViewer, [props.row.id, props.row.version])}>
        {props.value}
      </Link>
    );
  },
  headerStyle: { 'textAlign': 'left' },
}, {
  Header: 'Description',
  id: 'description',
  accessor: r => r.metadata.description,
  headerStyle: { 'textAlign': 'left' },
}, {
  Header: 'Last Update',
  id: 'updatedOn',
  accessor: r => <FormattedTime value={r.updatedOn} day='numeric' month='numeric' year='numeric' />,
  style: { 'textAlign': 'center' },
}];

const resourceHistoryColumns = [{
  Header: '',
  id: 'selection',
  Cell: props => {
    if (props.original.selected) {
      return (
        <i data-action="remove-from-bag" title="Add to selected resources" className='fa fa-check-square-o slipo-table-row-action'></i>
      );
    } else {
      return (
        <i data-action="add-to-bag" title="Remove from selected resources" className='fa fa-square-o slipo-table-row-action'></i>
      );
    }
  },
  style: { 'textAlign': 'center' },
  maxWidth: 60
}, {
  Header: 'Version',
  accessor: 'version',
  maxWidth: 60,
  style: { 'textAlign': 'center' },
}, {
  Header: 'id',
  accessor: 'id',
  show: false
}, {
  Header: 'Actions',
  id: 'actions',
  className: 'd-flex',
  Cell: props => {
    const record = props.original;

    let action = null;
    switch (record.exportStatus) {
      case EnumMapExportStatus.NONE:
        action = <i data-action="export-map" title="Export map data" className='fa fa-database slipo-table-row-action p-1'></i>;
        break;
      case EnumMapExportStatus.FAILED:
        action = (
          <i data-action="export-map" title="Export map data. Last execution has failed" className='fa fa-database slipo-table-row-action invalid-feedback p-1'></i>
        );
        break;
      case EnumMapExportStatus.COMPLETED:
        action = <i data-action="view-map" title="View Map" className='fa fa-map-o slipo-table-row-action p-1'></i>;
        break;
      default:
        action = <i title="Export operation in progress ..." className='fa fa-cogs p-1'></i>;
        break;
    }

    return (
      <React.Fragment>
        <i data-action="delete" className='fa fa-trash slipo-table-row-action mr-2' title="Delete"></i>
        <i data-action="export-to-file" className='fa fa-archive slipo-table-row-action' title="Export"></i>
        {action}
      </React.Fragment>
    );
  },
  style: { 'justifyContent': 'center' },
  maxWidth: 60,
}, {
  Header: 'Size',
  id: 'fileSize',
  Cell: props => {
    return <span>{formatFileSize(props.original.fileSize)}</span>;
  },
  maxWidth: 70,
  headerStyle: { 'textAlign': 'right' },
  style: { 'textAlign': 'right' },
}, {
  Header: 'Last Update',
  id: 'updatedOn',
  accessor: r => <FormattedTime value={r.updatedOn} day='numeric' month='numeric' year='numeric' />,
  style: { 'textAlign': 'center' },
}];


export default class Resources extends React.Component {

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
      case 'add-to-bag':
        this.props.addResourceToBag(createResource(rowInfo));
        break;
      case 'remove-from-bag':
        this.props.removeResourceFromBag(createResource(rowInfo));
        break;
      case 'delete':
        this.props.deleteResource(rowInfo.original.id, rowInfo.original.version);
        break;
      case 'export-to-file':
        this.props.exportResource(createResource(rowInfo));
        break;
      case 'export-map':
        this.props.exportMap(
          rowInfo.original.execution.id,
          rowInfo.original.execution.version,
          rowInfo.original.execution.execution,
        );
        break;
      case 'view-map':
        this.props.viewMap(
          rowInfo.original.id,
          rowInfo.original.version,
        );
        break;
      default:
        if (handleOriginal) {
          handleOriginal();
        }
        break;
    }
  }

  isSelected(rowInfo) {
    if (!this.props.selected || !rowInfo) {
      return false;
    }
    return this.props.selected.id === rowInfo.row.id && this.props.selected.version === rowInfo.row._original.version;
  }

  render() {
    const pages = this.props.pager && Math.ceil(this.props.pager.count / this.props.pager.size);

    // TODO: Move to reducer
    this.props.items.forEach((item) => {
      item.selected = (this.props.selectedResources.some((r) => r.id === item.id && r.version === item.version));
      item.revisions.forEach((item) => {
        item.selected = (this.props.selectedResources.some((r) => r.id === item.id && r.version === item.version));
      });
    });

    return (
      <Table
        name="Resource explorer"
        id="resource-explorer"
        columns={resourceColumns}
        data={this.props.items}
        defaultPageSize={10}
        showPageSizeOptions={false}
        manual
        onPageChange={(index) => {
          this.props.setPager({ ...this.props.pager, index });
          this.props.fetchResources({
            query: { ...this.props.filters },
            pagingOptions: { pageIndex: index, pageSize: this.props.pager.size }
          });
        }}
        onPageSizeChange={(size) => {
          this.props.setPager({ ...this.props.pager, size });
          this.props.fetchResources({
            query: { ...this.props.filters },
            pagingOptions: { pageIndex: this.props.pager.index, pageSize: size }
          });
        }}
        getTrProps={(state, rowInfo) => ({
          onClick: () => {
            this.props.setSelectedResource(rowInfo.row.id, rowInfo.row.version);
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
                    name="Resource explore"
                    id="resource-explore"
                    minRows={1}
                    columns={resourceHistoryColumns}
                    data={row.original.revisions.filter((v) => v.version !== row.original.version)}
                    noDataText="No other revisions"
                    defaultPageSize={row.original.revisions.length}
                    showPagination={false}
                    getTrProps={(state, rowInfo) => ({
                      onClick: () => {
                        this.props.setSelectedResource(rowInfo.row.id, rowInfo.row.version);
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

