import React from 'react';
import PropTypes from 'prop-types';
import Dropzone from 'react-dropzone';

import { formatFileSize } from '../../../../util';
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

/**
 * Presentation modes for {@link FileSelect} component
 * @readonly
 * @enum {string}
 */
export const EnumFileSelectMode = {
  /** View selected file relative path as an input component */
  FIELD: 'FIELD',
  /** Browse the files of the current folder */
  BROWSER: 'BROWSER',
  /** Create a new folder */
  NEW_FOLDER: 'NEW_FOLDER',
  /** Upload a file */
  UPLOAD: 'UPLOAD',
};

/**
 * Actions for {@link FileSelect} component
 * @readonly
 * @enum {string}
 */
const EnumAction = {
  /** Delete file or folder */
  Delete: 'Delete',
  /** Cancel current action */
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
      maxWidth: 120,
    },
    {
      Header: 'Modified',
      id: 'modified',
      accessor: 'modified',
      style: { 'textAlign': 'center' },
      maxWidth: 140,
      Cell: props => (
        <FormattedTime value={props.value} day='numeric' month='numeric' year='numeric' />
      ),
    },
  ];
}

/**
 * A server file
 *
 * @typedef {Object} File
 * @property {string} modified - Most recent modification timestamp
 * @property {string} name - File name
 * @property {string} path - File relative path
 * @property {number} size - File size in bytes
 */

/**
 * A server folder
 *
 * @typedef {Object} Folder
 * @property {number} count - # of files in the folder
 * @property {File[]} files - Array of {@link File} objects
 * @property {Folder[]} folders - Array of {@link Folder} objects
 * @property {number} modified - Most recent modification timestamp
 * @property {string} name - Folder name
 * @property {string} path - Folder relative path
 * @property {number} size - Folder contents size in bytes
 */

/**
 * Component for browsing the server file system and managing user files.
 *
 * @export
 * @class FileSelect
 * @extends {React.Component}
 */

export class FileSelect extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      folder: this.findFolderFromPath(),
      mode: this.props.defaultMode || EnumFileSelectMode.BROWSER,
      newFolderName: '',
      confirmMessage: '',
      confirmDialogOpen: false,
      isUploading: false,
      collapsed: this.props.defaultMode === EnumFileSelectMode.FIELD,
    };

    this.confirmDialogHandler = this.confirmDialogHandler.bind(this);
  }

  static propTypes = {
    id: PropTypes.string.isRequired,
    filesystem: PropTypes.object.isRequired,
    value: PropTypes.any,
    onChange: PropTypes.func.isRequired,
    allowUpload: PropTypes.bool.isRequired,
    allowNewFolder: PropTypes.bool.isRequired,
    allowDelete: PropTypes.bool.isRequired,
    createFolder: PropTypes.func,
    uploadFile: PropTypes.func,
    deletePath: PropTypes.func,
  }

  static defaultProps = {
    allowUpload: false,
    allowNewFolder: false,
    allowDelete: false,
  }

  /**
   * Returns the current selected path
   *
   * @readonly
   * @memberof FileSelect
   */
  get selectedPath() {
    return this.props.value ? typeof this.props.value === 'object' ? this.props.value.path : this.props.value : null;
  }

  /**
   * Finds a folder object from the current path
   *
   * @param {string} path - The path of the file system
   * @returns A {@link Folder} object
   * @memberof FileSelect
   */
  findFolderFromPath(path) {
    const currentPath = path || this.selectedPath || (this.state && this.state.folder && this.state.folder.path) || '/';
    let folder = this.props.filesystem;

    currentPath.split('/').forEach((name) => {
      if (name) {
        folder = folder.folders.find((f) => f.name === name) || folder;
      }
    });

    return folder;
  }

  /**
   * Creates a hierarchy of folders given a relative path. The hierarchy is
   * represented as an array of objects with two properties, namely, the folder
   * name and the corresponding {@link Folder} object
   *
   * @param {string} path - A relative path
   * @returns A hierarchy of folders
   * @memberof FileSelect
   */
  getFolderHierarchy(path) {
    const hierarchy = [{ name: '..', folder: this.props.filesystem }];
    let currentFolder = this.props.filesystem;

    path.split('/').forEach((name) => {
      if (name) {
        currentFolder = currentFolder.folders.find((f) => f.name === name);
        hierarchy.push({ name, folder: currentFolder });
      }
    });

    return hierarchy.length === 1 && !path ? [{ name: '.', folder: this.props.filesystem }] : hierarchy;
  }

  /**
   * Checks if the file represented by the given grid row is selected
   *
   * @param {*} rowInfo - A grid row
   * @returns True if the corresponding file is selected; Otherwise false
   * @memberof FileSelect
   */
  isFileSelected(rowInfo) {
    return this.selectedPath && rowInfo && rowInfo.row.type === 'file' && rowInfo.row.path === this.selectedPath;
  }

  /**
   * Sets the mode of the component to any of the values defined in
   * {@link EnumFileSelectMode}
   *
   * @param {EnumFileSelectMode} mode - The new component mode
   * @memberof FileSelect
   */
  setMode(mode) {
    this.setState({
      mode,
    });
  }

  /**
   * Sets the name of a new folder
   *
   * @param {string} name - The folder name
   * @memberof FileSelect
   */
  setFolderName(name) {
    this.setState({
      newFolderName: name,
    });
  }

  /**
   * Creates a new folder
   *
   * @memberof FileSelect
   */
  createNewFolder() {
    if ((!this.props.readOnly) && (this.props.allowNewFolder)) {
      const newFolder = this.state.folder.path + '/' + this.state.newFolderName;

      this.props.createFolder(newFolder)
        .then(() => {
          this.setState({
            folder: this.findFolderFromPath(newFolder),
            mode: EnumFileSelectMode.BROWSER,
            newFolderName: '',
          });
        })
        .catch(err => {
          this.displayToast(err.message);
        });
    }
  }

  /**
   * Cancels the creation of a new folder
   *
   * @memberof FileSelect
   */
  discardNewFolder() {
    this.setState({
      mode: EnumFileSelectMode.BROWSER,
      newFolderName: '',
    });
  }

  /**
   * Cancels the uploading of a new file
   *
   * @memberof FileSelect
   */
  cancelUpload() {
    this.setState({
      mode: EnumFileSelectMode.BROWSER,
      file: null,
    });
  }

  /**
   * Displays a warning message to the user
   *
   * @param {string} message - The message to display
   * @memberof FileSelect
   */
  displayToast(message) {
    toast.dismiss();

    toast.error(
      <ToastTemplate iconClass='fa-warning' text={message} />
    );
  }

  handleRowAction(rowInfo, e, handleOriginal) {
    if (!rowInfo) {
      return;
    }
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
          .then(() => {
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
    if (!this.state.confirmDialogOpen) {
      return null;
    }

    return (
      <Dialog className="modal-dialog-centered"
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

  renderField() {
    if (this.state.mode !== EnumFileSelectMode.FIELD) {
      return null;
    }

    return (
      <div className="input-group">
        <input type="text" className="form-control" readOnly value={this.selectedPath || this.props.placeHolder || ''} />
        {!this.props.readOnly &&
          <span className="input-group-btn">
            {this.selectedPath && <button className="btn btn-danger" type="button" onClick={(e) => this.props.onChange(null)}><i className="fa fa-trash" /></button>}
            <button className="btn btn-default" type="button" onClick={(e) => this.setMode(EnumFileSelectMode.BROWSER)}><i className="fa fa-pencil" /></button>
          </span>
        }
      </div>
    );
  }

  renderHeader() {
    const { folder } = this.state;
    const hierarchy = this.getFolderHierarchy(folder.path);

    if (this.state.mode === EnumFileSelectMode.FIELD) {
      return null;
    }

    return (
      <div style={{ display: 'flex' }}>
        <div style={{ flexGrow: '1' }}>
          {
            hierarchy.map((item, i, arr) => {
              const isLast = (i === arr.length - 1);

              return (
                <span key={i}>
                  {isLast ?
                    <span className="btn" style={{ cursor: 'default' }}>{item.name}</span>
                    :
                    <Button
                      disabled={this.state.mode !== EnumFileSelectMode.BROWSER}
                      color="link"
                      onClick={(e) => {
                        if (item && item.folder) {
                          this.setState({ folder: item.folder });
                        }
                      }}
                    >
                      {item.name}
                    </Button>
                  }
                  {!isLast ? <span>/</span> : null}
                </span>
              );
            })
          }
        </div>
        <div style={{ fontSize: '1.5em', paddingTop: '4px' }}>
          {!this.props.readOnly && this.state.collapsed && this.state.mode === EnumFileSelectMode.BROWSER &&
            <Button color="primary" className="ml-1 mb-1" onClick={(e) => this.setMode(EnumFileSelectMode.FIELD)}><i className="fa fa-check" /></Button>
          }
          {!this.props.readOnly && this.props.allowUpload && this.state.mode === EnumFileSelectMode.BROWSER &&
            <Button color="default" className="ml-1 mb-1" onClick={(e) => this.setMode(EnumFileSelectMode.UPLOAD)}><i className="fa fa-cloud-upload" /></Button>
          }
          {!this.props.readOnly && this.props.allowNewFolder && this.state.mode === EnumFileSelectMode.BROWSER &&
            <Button color="default" className="ml-1 mb-1" onClick={(e) => this.setMode(EnumFileSelectMode.NEW_FOLDER)}><i className="fa fa-folder-o" /></Button>
          }
          {!this.props.readOnly && this.state.mode === EnumFileSelectMode.UPLOAD && !this.state.isUploading &&
            <Button color="danger" className="ml-1 mb-1" onClick={(e) => this.cancelUpload()}><i className="fa fa-times" /></Button>
          }
        </div>
      </div>
    );
  }

  renderCreateFolder() {
    if (this.props.readOnly || this.state.mode !== EnumFileSelectMode.NEW_FOLDER) {
      return null;
    }

    return (
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
    );
  }

  renderBrowser() {
    const { style } = this.props;
    const { folder } = this.state;
    const data = [
      ...folder.folders.map(f => ({ ...f, type: 'folder' })),
      ...folder.files.map(f => ({ ...f, type: 'file' })),
    ];

    if (this.state.mode !== EnumFileSelectMode.BROWSER && this.state.mode !== EnumFileSelectMode.NEW_FOLDER) {
      return null;
    }

    return (
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
        showPagination={false}
        defaultPageSize={Number.MAX_VALUE}
      />
    );
  }

  renderUpload() {
    if (this.props.readOnly || this.state.mode !== EnumFileSelectMode.UPLOAD) {
      return null;
    }

    return (
      <div>
        <Dropzone
          onDrop={(accepted, rejected) => {
            if (rejected.length) {
              console.error('Rejected file:', rejected);
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
                  mode: EnumFileSelectMode.BROWSER,
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
    );
  }

  render() {
    return (
      <div>
        {this.renderField()}
        {this.renderHeader()}
        {this.renderCreateFolder()}
        {this.renderBrowser()}
        {this.renderUpload()}
        {this.renderConfirmDialog()}
      </div >
    );
  }
}

export default decorateField(FileSelect);
