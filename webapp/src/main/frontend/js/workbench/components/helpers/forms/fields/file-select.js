import React from 'react';
import PropTypes from 'prop-types';
import ReactTable from 'react-table';
import { FormattedTime } from 'react-intl';
import { Row, Button } from 'reactstrap';

import formatFileSize from '../../../../util/file-size'; 
import decorateField from './formfield';

const fileColumns = [
  {
    Header: '',
    accessor: 'type',
    maxWidth: 30,
    Cell: props => props.value === 'folder' ? 
      <i className="fa fa-folder" />
      :
      <i className="fa fa-file" />
  },
  {
    Header: 'File Name',
    accessor: 'name',
  },
  {
    Header: 'File Path',
    accessor: 'path',
  },
  {
    Header: 'File Size',
    id: 'size',
    accessor: r => formatFileSize(r.size),
  },
  {
    Header: 'Created',
    id: 'createdOn',
    accessor: 'createdOn',
    Cell: props => (
      <FormattedTime value={props.value} day='numeric' month='numeric' year='numeric' />
    ),
  },
];

export class FileSelect extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      folder: this.props.filesystem,
    };
  }
  _getParentDir(level) {
    const i = 0;
    let folder = this.props.filesystem;
    while (i < level && folder) {
      folder = folder.folders;
    }
    return folder;
  }

  render() {
    const { folder } = this.state;
    const data = [
      ...folder.folders.map(f => ({ ...f, type: 'folder' })), 
      ...folder.files.map(f => ({ ...f, type: 'file' })),
    ];
    const path = folder.path.split('/').slice(0, -1).map((name, level) => level === 0 ? ({ name: '..', folder: this.props.filesystem }) : ({ name, folder: this._getParentDir(level) }));

    return (
      <div>
        { 
          path.map((item, i, arr) => (
            <span key={i}>
              <Button 
                color="link"
                onClick={(e) => { 
                  if (item && item.folder) { 
                    this.setState({ folder: item.folder }); 
                  } 
                }}
              >
                {item.name}
              </Button>
              { i !== arr.length -1 ? <span>/</span> : null }
            </span>
          ))
        }
        <ReactTable
          name={this.props.id} 
          id={this.props.id} 
          getTrProps={(state, rowInfo) => ({
            onClick: (e) => { 
              if (rowInfo.row.type === 'file') {
                this.props.onChange(rowInfo.row); 
              } else if (rowInfo.row.type === 'folder') {
                this.setState({ folder: folder.folders[rowInfo.index] });
              }
            },
            style: {
              background: this.props.value && rowInfo && rowInfo.row.type === 'file' && rowInfo.row.path === this.props.value.path ? '#20a8d8' : null,
            }    
          })}
          defaultPageSize={data.length}
          showPagination={false}
          columns={fileColumns}
          data={data}
        />
      </div>
    );
  }
}

export default decorateField(FileSelect);


FileSelect.propTypes = {
  id: PropTypes.string.isRequired,
  filesystem: PropTypes.object.isRequired,
  value: PropTypes.any,
  onChange: PropTypes.func.isRequired,
};

