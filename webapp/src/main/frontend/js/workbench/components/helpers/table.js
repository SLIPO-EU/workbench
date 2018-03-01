import * as React from 'react';
import * as PropTypes from 'prop-types';

import ReactTable from 'react-table';

/**
 * A wrapper component for {@link ReactTable}.
 *
 * @class Table
 * @extends {React.Component}
 */
class Table extends React.Component {

  constructor(props) {
    super(props);
  }

  render() {
    let { showPagination, defaultPageSize, minRows, sortable, ...rest } = this.props;
    return (
      <div className="slipo-table-container">
        <ReactTable
          {...rest}
          showPagination={showPagination || false}
          defaultPageSize={defaultPageSize || 10}
          minRows={minRows || 5}
          sortable={sortable || false}
          showPageSizeOptions={false}
        />
      </div>
    );
  }
}

Table.propTypes = {
  data: PropTypes.arrayOf(PropTypes.object),
  columns: PropTypes.arrayOf(PropTypes.object).isRequired,
};

export default Table;
