import React from 'react';
import PropTypes from 'prop-types';
import Dropzone from 'react-dropzone';

import formatFileSize from '../../../../util/file-size';
import decorateField from './form-field';

import {
  FormattedTime,
} from 'react-intl';

import {
  toast,
} from 'react-toastify';

import {
  Button,
  Input,
} from 'reactstrap';

import {
  Dialog,
  Table,
  ToastTemplate,
} from '../../';

const EnumMode = {
  BROWSER: 'BROWSER',
  NEW_FOLDER: 'NEW_FOLDER',
  UPLOAD: 'UPLOAD',
};

const EnumAction = {
  Delete: 'Delete',
  Cancel: 'Cancel',
};

function createFileColumns(props) {
  return [
    {
      Header: '',
      accessor: 'delete',
      maxWidth: 30,
      show: props.allowDelete && !props.readOnly,
      style: { 'textAlign': 'center' },
      Cell: props => <i data-action="delete" className='fa fa-trash slipo-table-row-action'></i>
    },
    {
      Header: '',
      accessor: 'type',
      maxWidth: 30,
      style: { 'textAlign': 'center' },
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
}

export class FileSelect extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      folder: this.findFolderFromPath(),
      mode: EnumMode.BROWSER,
      newFolderName: '',
      confirmMessage: '',
      confirmDialogOpen: false,
      isUploading: false,
    };

    this.confirmDialogHandler = this.confirmDialogHandler.bind(this);
  }

  static propTypes = {
    id: PropTypes.string.isRequired,
    filesystem: PropTypes.object.isRequired,
    value: PropTypes.any,
    onChange: PropTypes.func.isRequired,
    allowUpload: PropTypes.bool,
    allowNewFolder: PropTypes.bool,
    allowDelete: PropTypes.bool,
    createFolder: PropTypes.func,
    uploadFile: PropTypes.func,
    deletePath: PropTypes.func,
  }

  static defaultProps = {
    allowUpload: false,
    allowNewFolder: false,
    allowDelete: false,
  }

  findFolderFromPath(path) {
    const currentPath = path || (this.props.value && this.props.value.path) || (this.state && this.state.folder && this.state.folder.path) || '/';

    let folder = this.props.filesystem;

    currentPath.split('/').slice(0, -1).forEach((name) => {
      if (name) {
        folder = folder.folders.find((f) => f.name === name);
      }
    });

    return folder;
  }

  getFolderHierarchy(path) {
    const hierarchy = [{ name: '..', folder: this.props.filesystem }];
    let currentFolder = this.props.filesystem;

    path.split('/').slice(0, -1).forEach((name) => {
      if (name) {
        currentFolder = currentFolder.folders.find((f) => f.name === name);
        hierarchy.push({ name, folder: currentFolder });
      }
    });

    return hierarchy;
  }

  isFileSelected(rowInfo) {
    return this.props.value && rowInfo && rowInfo.row.type === 'file' && rowInfo.row.path === this.props.value.path;
  }

  setMode(mode) {
    this.setState({
      mode,
    });
  }

  setFolderName(name) {
    this.setState({
      newFolderName: name,
    });
  }

  createNewFolder() {
    if ((!this.props.readOnly) && (this.props.allowNewFolder)) {
      this.props.createFolder(this.state.folder.path + this.state.newFolderName)
        .then(fs => {
          this.setState({
            folder: this.findFolderFromPath(this.state.folder.path),
            mode: EnumMode.BROWSER,
            newFolderName: '',
          });
        })
        .catch(err => {
          this.displayToast(err.message);
        });
    }
  }

  discardNewFolder() {
    this.setState({
      mode: EnumMode.BROWSER,
      newFolderName: '',
    });
  }

  cancelUpload() {
    this.setState({
      mode: EnumMode.BROWSER,
      file: null,
    });
  }

  displayToast(message) {
    toast.dismiss();

    toast.error(
      <ToastTemplate iconClass='fa-warning' text={message} />
    );
  }

  handleRowAction(rowInfo, e, handleOriginal) {
    switch (e.target.getAttribute('data-action')) {
      case 'delete':
        this.setState({
          confirmDialogOpen: true,
          confirmMessage: <span>Do you want to delete {rowInfo.row.type === 'file' ? 'file' : 'folder'} <b>{rowInfo.row.name}</b> permanently?</span>,
          pathToDelete: rowInfo.row.path,
        });
        break;

      default:
        if (rowInfo.row.type === 'file') {
          this.props.onChange(rowInfo.row);
        } else if (rowInfo.row.type === 'folder') {
          this.setState({ folder: this.state.folder.folders[rowInfo.index] });
        }
        if (handleOriginal) {
          handleOriginal();
        }
    }
  }

  confirmDialogHandler(action) {
    this.setState({
      confirmDialogOpen: false,
    });

    switch (action.key) {
      case EnumAction.Delete:
        if ((this.props.value) && (this.props.value.path === this.state.pathToDelete)) {
          this.props.onChange(null);
        }
        this.props.deletePath(this.state.pathToDelete)
          .then(fs => {
            this.setState({
              folder: this.findFolderFromPath(this.state.folder.path),
              pathToDelete: '',
            });
          })
          .catch(err => {
            this.displayToast(err.message);
          });
        break;

      default:
        break;

    }
  }

  renderConfirmDialog() {
    return (
      <Dialog
        header={
          <span>
            <i className={'fa fa-warning mr-2'}></i>System Message
          </span>
        }
        modal={this.state.confirmDialogOpen}
        handler={this.confirmDialogHandler}
        actions={[
          {
            key: EnumAction.Delete,
            label: 'Yes',
            iconClass: 'fa fa-trash',
            color: 'danger',
          }, {
            key: EnumAction.Cancel,
            label: 'No',
            iconClass: 'fa fa-undo',
          }
        ]}
      >
        <div>{this.state.confirmMessage}</div>
      </Dialog>
    );
  }

  render() {
    const { style } = this.props;
    const { folder } = this.state;
    const data = [
      ...folder.folders.map(f => ({ ...f, type: 'folder' })),
      ...folder.files.map(f => ({ ...f, type: 'file' })),
    ];
    const hierarchy = this.getFolderHierarchy(folder.path);

    return (
      <div>
        <div style={{ display: 'flex' }}>
          <div style={{ flexGrow: '1' }}>
            {
              hierarchy.map((item, i, arr) => (
                <span key={i}>
                  <Button
                    disabled={this.state.mode !== EnumMode.BROWSER}
                    color="link"
                    onClick={(e) => {
                      if (item && item.folder) {
                        this.setState({ folder: item.folder });
                      }
                    }}
                  >
                    {item.name}
                  </Button>
                  {i !== arr.length - 1 ? <span>/</span> : null}
                </span>
              ))
            }
          </div>
          <div style={{ fontSize: '1.5em', paddingTop: '4px' }}>
            {!this.props.readOnly && this.props.allowUpload && this.state.mode === EnumMode.BROWSER &&
              <i className="fa fa-cloud-upload" style={{ cursor: 'pointer' }} onClick={(e) => { this.setMode(EnumMode.UPLOAD); }} />
            }
            {!this.props.readOnly && this.props.allowNewFolder && this.state.mode === EnumMode.BROWSER &&
              <i className="fa fa-folder-o" style={{ cursor: 'pointer', paddingLeft: '5px', }} onClick={(e) => { this.setMode(EnumMode.NEW_FOLDER); }} />
            }
            {!this.props.readOnly && this.state.mode === EnumMode.UPLOAD && !this.state.isUploading &&
              <Button color="danger" onClick={(e) => this.cancelUpload()}><i className="fa fa-times" /></Button>
            }
          </div>
        </div>
        {!this.props.readOnly && this.state.mode === EnumMode.NEW_FOLDER &&
          <div style={{ padding: '5px 0px', display: 'flex', }}>
            <Input
              type="text"
              name="newFolderName"
              id="newFolderName"
              value={this.state.newFolderName || ''}
              autoComplete="off"
              onChange={(e) => this.setFolderName(e.target.value)}
              placeholder="New folder name"
              valid={!!this.state.newFolderName}
            />
            <div className="ml-2" style={{ float: 'left' }}>
              <Button color="success" onClick={(e) => this.createNewFolder()} disabled={!this.state.newFolderName}><i className="fa fa-check" /></Button>
            </div>
            <div className="ml-2" style={{ float: 'left' }}>
              <Button color="danger" onClick={(e) => this.discardNewFolder()}><i className="fa fa-times" /></Button>
            </div>
          </div>
        }
        <div>
          {(this.state.mode === EnumMode.BROWSER || this.state.mode === EnumMode.NEW_FOLDER) &&
            <Table
              id={this.props.id}
              name={this.props.id}
              style={style}
              getTrProps={(state, rowInfo) => ({
                className: (this.isFileSelected(rowInfo) ? 'slipo-react-table-selected' : null),
              })}
              getTdProps={(state, rowInfo, column) => ({
                onClick: this.handleRowAction.bind(this, rowInfo)
              })}
              minRows={10}
              columns={createFileColumns(this.props)}
              data={data}
              noDataText="No files found"
            />
          }
          {!this.props.readOnly && this.state.mode === EnumMode.UPLOAD &&
            <div>
              <Dropzone
                onDrop={(accepted, rejected) => {
                  if (rejected.length) {
                    console.error('rejected file:', rejected);
                  }
                  const file = accepted && accepted.length && accepted[0];
                  this.setState({
                    isUploading: true,
                  });
                  let path = this.state.folder.path;
                  if (path.startsWith('/')) {
                    path = path.slice(1);
                  }
                  this.props.uploadFile({ path, filename: file.name, }, file, )
                    .then(fs => {
                      this.setState({
                        folder: this.findFolderFromPath(this.state.folder.path),
                        mode: EnumMode.BROWSER,
                        newFolderName: '',
                        isUploading: false,
                      });
                    })
                    .catch(err => {
                      this.displayToast(err.message);
                      this.setState({
                        isUploading: false,
                      });
                    });
                }}
                style={{
                  textAlign: 'center',
                  fontSize: '3em',
                  color: '#656565',
                  border: '1px dotted #656565',
                  height: '12rem',
                }}
                disableClick={false}
                multiple={false}
                disabled={this.state.isUploading}
              >
                {this.state.isUploading ?
                  <div style={{ paddingTop: '3rem' }}>
                    <i className="fa fa-refresh fa-spin"></i>
                  </div>
                  :
                  <div>
                    <i className="fa fa-cloud-upload fa-4x"></i>
                  </div>
                }
              </Dropzone>
              <div>
                {this.state.file && this.state.file.name}
                {this.state.file && ` (${formatFileSize(this.state.file.size)})`}
              </div>
            </div>
          }
        </div>
        {this.state.confirmDialogOpen &&
          this.renderConfirmDialog()
        }
      </div >
    );
  }
}

export default decorateField(FileSelect);
