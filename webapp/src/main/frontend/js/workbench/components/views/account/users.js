import * as React from 'react';

import {
  FormattedTime
} from 'react-intl';

import {
  Table,
} from '../../helpers';

const accountColumns = [{
  accessor: 'id',
  show: false,
}, {
  Header: 'User Name',
  accessor: 'username',
}, {
  Header: 'Email',
  accessor: 'email',
}, {
  Header: 'First Name',
  accessor: 'givenName',
}, {
  Header: 'Last Name',
  accessor: 'familyName',
}, {
  Header: 'Created On',
  accessor: 'registeredAt',
  style: { 'textAlign': 'center' },
  Cell: props => {
    return (
      <FormattedTime value={props.value} day='numeric' month='numeric' year='numeric' />
    );
  },
  maxWidth: 135,
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
        name="Account viewer"
        id="account-viewer"
        columns={accountColumns}
        data={items}
        defaultPageSize={10}
        showPageSizeOptions={false}
        manual
        onPageChange={(index) => {
          this.props.setPager({ ...pager, index });
          this.props.fetchAccounts({
            query: { ...this.props.filters },
            pagingOptions: { pageIndex: index, pageSize: pager.size }
          });
        }}
        onPageSizeChange={(size) => {
          this.props.setPager({ ...pager, size });
          this.props.fetchAccounts({
            query: { ...this.props.filters },
            pagingOptions: { pageIndex: pager.index, pageSize: size }
          });
        }}
        getTrProps={(state, rowInfo) => ({
          onClick: (e) => {
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
