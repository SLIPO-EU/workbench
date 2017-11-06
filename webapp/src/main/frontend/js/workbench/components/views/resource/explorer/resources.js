import * as React from 'react';
import ReactTable from 'react-table';

const resourceColumns = [
  {
    Header: 'id',
    accessor: 'id',
    maxWidth: 30,
  },
  {
    Header: 'Name',
    id: 'name',
    accessor: r => r.metadata.name,
  },
  {
    Header: 'Path',
    id: 'path',
    accessor: r => r.fileName,
  },
];

export default function Resources(props) {
  const pages = props.resources && props.resources.pagingOptions && Math.ceil(props.resources.pagingOptions.count / props.resources.pagingOptions.pageSize);
  return (
    <ReactTable
      name="Resource explore"
      id="resource-explore"
      columns={resourceColumns}
      data={props.resources.items}
      defaultPageSize={10}
      showPageSizeOptions={false}
      manual
      onPageChange={(index) => {
        props.setPager({ ...props.pager, index });
        props.fetchResources({
          pagingOptions: { pageIndex: index, pageSize: props.pager.size }
        });
      }}
      onPageSizeChange={(size) => {
        props.setPager({ ...props.pager, size });
        props.fetchResources({
          pagingOptions: { pageIndex: props.pager.index, pageSize: size }
        });
      }}
      getTrProps={(state, rowInfo) => ({
        onClick: (e) => {
          props.setSelectedResource(rowInfo.row.id);
        },
        style: {
          background: rowInfo && props.selectedResource === rowInfo.row.id ? '#20a8d8' : null,
        }
      })}
      pages={pages}
      page={props.pager.index}
      pageSize={props.pager.size}
      showPagination
    />
  );
}

