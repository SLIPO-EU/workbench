import * as React from 'react';

import {
  FormattedTime
} from 'react-intl';

import {
  ErrorLevel,
  Table,
} from '../../helpers';

const eventColumns = [{
  accessor: 'id',
  show: false,
}, {
  Header: 'Level',
  accessor: 'level',
  Cell: props => {
    return (
      <ErrorLevel value={props.value} />
    );
  },
}, {
  Header: 'Category',
  accessor: 'category',
  style: { 'textAlign': 'center' }
}, {
  Header: 'Code',
  accessor: 'code',
  style: { 'textAlign': 'center' }
}, {
  Header: 'Created On',
  accessor: 'createdOn',
  Cell: props => {
    return (
      <FormattedTime value={props.value} day='numeric' month='numeric' year='numeric' />
    );
  },
  minWidth: 132
}, {
  id: 'message',
  Header: 'Message',
  accessor: d => d.message,
  minWidth: 400,
}, {
  Header: () => <span>Source</span>,
  accessor: 'clientAddress',
  style: { 'textAlign': 'center' }
}, {
  Header: () => <span>Account</span>,
  accessor: 'userName',
  style: { 'textAlign': 'center' }
}];

export default class Processes extends React.Component {

  constructor(props) {
    super(props);
  }

  isSelected(rowInfo) {
    if (!rowInfo || !this.props.selected) {
      return false;
    }
    return this.props.selected.id === rowInfo.row.id;
  }

  render() {
    const { items, pager } = this.props;
    const pages = pager && Math.ceil(pager.count / pager.size);

    return (
      <Table
        name="Event viewer"
        id="event-viewer"
        columns={eventColumns}
        data={items}
        defaultPageSize={10}
        showPageSizeOptions={false}
        manual
        onPageChange={(index) => {
          this.props.setPager({ ...pager, index });
          this.props.fetchEvents({
            query: { ...this.props.filters },
            pagingOptions: { pageIndex: index, pageSize: pager.size }
          });
        }}
        onPageSizeChange={(size) => {
          this.props.setPager({ ...pager, size });
          this.props.fetchEvents({
            query: { ...this.props.filters },
            pagingOptions: { pageIndex: pager.index, pageSize: size }
          });
        }}
        getTrProps={(state, rowInfo) => ({
          onClick: () => {
            this.props.setSelected(rowInfo.row.id);
          },
          className: (this.isSelected(rowInfo) ? 'slipo-react-table-selected' : null),
        })}
        pages={pages}
        page={pager.index}
        pageSize={pager.size}
        showPagination
      />
    );
  }
}
