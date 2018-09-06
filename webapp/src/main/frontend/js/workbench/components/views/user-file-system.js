import * as React from 'react';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';

import {
  Card as ReactCard, CardBody, Row, Col,
} from 'reactstrap';

import {
  EnumFileSelectMode,
  FileSelect,
} from '../helpers/forms/fields/file-select';

import {
  createFolder,
  uploadFile,
  deletePath,
} from '../../ducks/config';

class UserFileSystem extends React.Component {

  render() {
    return (
      <div className="animated fadeIn">
        <ReactCard>
          <CardBody className="card-body" >
            <Row>
              <Col>
                <FileSelect
                  id="filesystem"
                  defaultMode={EnumFileSelectMode.BROWSER}
                  filesystem={this.props.filesystem}
                  createFolder={this.props.createFolder}
                  uploadFile={this.props.uploadFile}
                  deletePath={this.props.deletePath}
                  onChange={() => null}
                  allowUpload
                  allowNewFolder
                  allowDelete
                  allowDownload
                />
              </Col>
            </Row>
          </CardBody>
        </ReactCard>
      </div>
    );
  }

}

const mapStateToProps = (state) => ({
  filesystem: state.config.filesystem,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  createFolder,
  uploadFile,
  deletePath,
}, dispatch);

const mergeProps = (stateProps, dispatchProps, ownProps) => {
  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
  };
};

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(UserFileSystem);
