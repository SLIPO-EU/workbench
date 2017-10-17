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
    return (
      <div className="slipo-table-container">
        <ReactTable
          data={this.props.data}
          columns={this.props.columns}
          showPagination={this.props.showPagination || false}
          defaultPageSize={10}
          minRows={5}
        />
      </div>
    );
  }
}

Table.propTypes = {
  data: PropTypes.arrayOf(PropTypes.object).isRequired,
  columns: PropTypes.arrayOf(PropTypes.object).isRequired,
};

export default Table;
