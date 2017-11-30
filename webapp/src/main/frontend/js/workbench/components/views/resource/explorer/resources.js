import * as React from 'react';
import ReactTable from 'react-table';
import { FormattedTime } from 'react-intl';
import { Link } from 'react-router-dom';

import { DynamicRoutes, buildPath } from '../../../../model/routes';
import {
  EnumResourceType,
  EnumProcessInput,
  ResourceTypeIcons,
} from '../../process/designer';
import { Component } from '../register/filesystem';
import Table from '../../../helpers/table';

/**
 * Creates a plain JavaScript object representing a catalog resource
 *
 * @param {any} rowInfo the rowInfo object for the selected row
 * @returns a plain JavaScript object
 */
function createResource(rowInfo) {
  return {
    inputType: EnumProcessInput.CATALOG,
    resourceType: rowInfo.original.type,
    id: rowInfo.original.id,
    version: rowInfo.original.version,
    title: `${rowInfo.original.metadata.name} (ver. ${rowInfo.original.version})`,
    iconClass: ResourceTypeIcons[rowInfo.original.type],
  };
}

const resourceColumns = [
  {
    expander: true,
    Header: 'Version',
    width: 45,
    Expander: ({ isExpanded, ...rest }) => {
      if (rest.original.versions.length > 0) {
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
  }, {
    Header: 'id',
    accessor: 'id',
    maxWidth: 30,
    show: false
  }, {
    Header: 'Actions',
    id: 'actions',
    Cell: props => {
      return (
        <i data-action="add-to-bag" className='fa fa-bookmark-o'></i>
      );
    },
    style: { 'textAlign': 'center' },
    maxWidth: 50,
  }, {
    Header: 'Name',
    id: 'name',
    accessor: r => r.metadata.name,
    Cell: props => {
      return (
        <Link to={buildPath(DynamicRoutes.ResourceViewer, [props.row.id])}>{props.value}</Link>
      );
    },
  }, {
    Header: 'Path',
    id: 'path',
    accessor: r => r.fileName,
  },
];

const resourceSubColumns = [
  {
    Header: 'Version',
    accessor: 'version',
    maxWidth: 60,
  },
  {
    Header: 'id',
    accessor: 'id',
    show: false
  },
  {
    Header: 'Actions',
    id: 'actions',
    Cell: props => {
      return (
        <i data-action="add-to-bag" className='fa fa-bookmark-o'></i>
      );
    },
    style: { 'textAlign': 'center' },
    maxWidth: 50,
  },
  {
    Header: 'Size',
    accessor: 'fileSize',
  },
  {
    Header: 'updatedOn',
    id: 'updatedOn',
    accessor: r => <FormattedTime value={r.updatedOn} day='numeric' month='numeric' year='numeric' />,
  },
];


export default class Resources extends React.Component {

  constructor(props) {
    super(props);
  }

  /**
 * Adds a catalog resource to the resource bag
 *
 * @param {any} e react synthetic event instance
 * @param {any} handleOriginal the table's original event handler
 */

  /**
   * Adds a catalog resource to the resource bag
   *
   * @param {any} rowInfo the rowInfo object for the selected row
   * @param {any} e react synthetic event instance
   * @param {any} handleOriginal the table's original event handler
   * @memberof Resources
   */
  addResourceToBag(rowInfo, e, handleOriginal) {
    switch (e.target.getAttribute('data-action')) {
      case 'add-to-bag':
        this.props.addResourceToBag(createResource(rowInfo));
        break;
      default:
        if (handleOriginal) {
          handleOriginal();
        }
        break;
    }
  }

  render() {
    const pages = this.props.resources &&
      this.props.resources.pagingOptions &&
      Math.ceil(this.props.resources.pagingOptions.count / this.props.resources.pagingOptions.pageSize);

    return (
      <Table
        name="Resource explore"
        id="resource-explore"
        columns={resourceColumns}
        data={this.props.resources.items}
        defaultPageSize={10}
        showPageSizeOptions={false}
        manual
        onPageChange={(index) => {
          this.props.setPager({ ...this.props.pager, index });
          this.props.fetchResources({
            pagingOptions: { pageIndex: index, pageSize: this.props.pager.size }
          });
        }}
        onPageSizeChange={(size) => {
          this.props.setPager({ ...this.props.pager, size });
          this.props.fetchResources({
            pagingOptions: { pageIndex: this.props.pager.index, pageSize: size }
          });
        }}
        getTrProps={(state, rowInfo) => ({
          onClick: (e) => {
            this.props.setSelectedResource(rowInfo.row.id, rowInfo.original.version);
          },
          style: {
            background: rowInfo && this.props.selectedResource === rowInfo.row.id ? '#20a8d8' : null,
          }
        })}
        getTdProps={(state, rowInfo, column) => ({
          onClick: this.addResourceToBag.bind(this, rowInfo)
        })}
        pages={pages}
        page={this.props.pager.index}
        pageSize={this.props.pager.size}
        showPagination
        SubComponent={
          row => {
            if (row.original.versions.length > 0) {
              return (
                <div style={{ padding: "4px", marginLeft: 10 }}>
                  <Table
                    name="Resource explore"
                    id="resource-explore"
                    columns={resourceSubColumns}
                    data={row.original.versions}
                    noDataText="No other versions"
                    defaultPageSize={Object.keys(row.original.versions).length}
                    showPagination={false}
                    getTrProps={(state, rowInfo) => ({
                      onClick: (e) => {
                        this.props.setSelectedResource(rowInfo.row.id, rowInfo.row.version);
                      },
                      style: {
                        lineHeight: 0.8,
                        background: rowInfo && this.props.selectedResourceVersion === rowInfo.row.version ? '#20a8d8' : null,
                      }
                    })}
                    getTdProps={(state, rowInfo, column) => ({
                      onClick: this.addResourceToBag.bind(this, rowInfo)
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

