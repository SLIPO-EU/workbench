import * as React from 'react';

import {
  FormattedTime
} from 'react-intl';

import {
  copyToClipboard,
} from '../../../util';

import {
  StatusLabel,
  Table,
} from '../../helpers';

import {
  message,
} from '../../../service';

const statusMappings = [{
  value: 'REVOKED',
  className: 'slipo-app-key-revoked',
}, {
  value: 'ACTIVE',
  className: 'slipo-app-key-active',
}];

const columns = [{
  accessor: 'id',
  show: false,
}, {
  expander: true,
  id: 'actions',
  Header: 'Actions',
  width: 60,
  Expander: (row) => {
    return (
      <span>
        <i data-action="view-key" title="View Key" className='fa fa-search slipo-table-row-action p-1 mr-1'></i>
        {!row.original.revoked &&
          <i data-action="revoke" title="Revoke" className='fa fa-trash slipo-table-row-action'></i>
        }
      </span>
    );
  },
}, {
  Header: 'Status',
  accessor: 'revoked',
  Cell: props => {
    const value = props.original.revoked ? 'REVOKED' : 'ACTIVE';
    return (
      <StatusLabel
        mappings={statusMappings}
        value={value}
        label={value}
      />
    );
  },
  width: 80,
  style: { 'textAlign': 'center' },
}, {
  Header: 'Name',
  accessor: 'name',
}, {
  Header: 'Mapped To',
  accessor: 'mappedAccount',
  style: { 'textAlign': 'center' },
  Cell: props => {
    return (
      <span>{props.value.username}</span>
    );
  },
}, {
  Header: 'Created On',
  accessor: 'createdOn',
  Cell: props => {
    return (
      <FormattedTime value={props.value} day='numeric' month='numeric' year='numeric' />
    );
  },
  width: 120,
  style: { 'textAlign': 'center' },
}, {
  Header: 'Created By',
  accessor: 'createdBy',
  style: { 'textAlign': 'center' },
  Cell: props => {
    return (
      <span>{`${props.value.familyName} ${props.value.givenName}`}</span>
    );
  },
}, {
  Header: 'Revoked On',
  accessor: 'revokedOn',
  Cell: props => {
    return (
      props.value ? (<FormattedTime value={props.value} day='numeric' month='numeric' year='numeric' />) : null
    );
  },
  width: 120,
  style: { 'textAlign': 'center' },
}, {
  Header: 'Revoked By',
  accessor: 'revokedBy',
  style: { 'textAlign': 'center' },
  Cell: props => {
    return (
      props.value ? (<span>{`${props.value.familyName} ${props.value.givenName}`}</span>) : null
    );
  },
}];

export default class ApplicationKeys extends React.Component {

  constructor(props) {
    super(props);
  }

  isSelected(rowInfo) {
    if (!rowInfo || !this.props.selected) {
      return false;
    }
    return this.props.selected === rowInfo.row.id;
  }

  handleRowAction(rowInfo, e, handleOriginal) {
    this.props.setSelected(rowInfo.row.id);

    switch (e.target.getAttribute('data-action')) {
      case 'revoke':
        this.props.revoke(rowInfo.original);
        break;
      case 'view-key':
        this.props.setExpanded(rowInfo.index, 'key');
        break;
      default:
        if (handleOriginal) {
          handleOriginal();
        }
        break;
    }
  }

  copyKeyToClipboard(value) {
    const result = copyToClipboard('copy-key', value);

    if (result) {
      message.info('Key has been copied to clipboard', 'fa-clipboard');
    } else {
      message.error('error.NOT_IMPLEMENTED', 'fa-warning');
    }
  }

  render() {
    const { items, pager } = this.props;
    const pages = pager && Math.ceil(pager.count / pager.size);

    return (
      <React.Fragment>
        <textarea id="copy-key"
          style={{
            tabIndex: -1,
            ariaHidden: true,
            position: 'absolute',
            left: -9999
          }}
        >
        </textarea>

        <Table
          name="Application key viewer"
          id="application-key-viewer"
          columns={columns}
          data={items}
          defaultPageSize={10}
          showPageSizeOptions={false}
          manual
          onPageChange={(index) => {
            this.props.setPager({ ...pager, index });
            this.props.query({
              query: { ...this.props.filters },
              pagingOptions: { pageIndex: index, pageSize: pager.size }
            });
          }}
          onPageSizeChange={(size) => {
            this.props.setPager({ ...pager, size });
            this.props.query({
              query: { ...this.props.filters },
              pagingOptions: { pageIndex: pager.index, pageSize: size }
            });
          }}
          getTrProps={(state, rowInfo) => ({
            className: (this.isSelected(rowInfo) ? 'slipo-react-table-selected' : null),
          })}
          getTdProps={(state, rowInfo, column) => ({
            onClick: this.handleRowAction.bind(this, rowInfo)
          })}
          pages={pages}
          page={pager.index}
          pageSize={pager.size}
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
              return (
                <div className="slipo-app-key-value">
                  {row.original.key}
                  <div className="copy-action"><i className="fa fa-clipboard" onClick={() => this.copyKeyToClipboard(row.original.key)} /></div>
                </div>
              );
            }
          }
        />
      </React.Fragment>
    );
  }
}
