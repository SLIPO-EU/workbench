import * as React from 'react';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';

import {
  Card,
  CardBody,
  Col,
  Row,
} from 'reactstrap';

import {
  ResourceWizard,
} from './resource/export/';

import {
  exportResource,
} from '../../ducks/ui/views/resource-explorer';

import {
  clearTemp,
  setTemp,
} from '../../ducks/ui/views/resource-export';

import {
  createFolder,
  uploadFile,
  deletePath,
} from '../../ducks/config';

/**
 * Export an existing resource
 *
 * @class ResourceExport
 * @extends {React.Component}
 */
class ResourceExport extends React.Component {

  render() {
    return (
      <Row>
        <Col>
          <Card>
            <CardBody className="card-body">
              <ResourceWizard
                clearTemp={this.props.clearTemp}
                appConfiguration={this.props.appConfiguration}
                createFolder={this.props.createFolder}
                deletePath={this.props.deletePath}
                exportResource={this.props.exportResource}
                filesystem={this.props.filesystem}
                goTo={this.props.history.push}
                initialActive={this.props.step}
                initialValues={this.props.values}
                reset={this.props.reset}
                saveTemp={this.props.setTemp}
                uploadFile={this.props.uploadFile}
              />
            </CardBody>
          </Card>
        </Col>
      </Row>
    );
  }

}

const mapStateToProps = (state) => ({
  appConfiguration: state.config,
  filesystem: state.config.filesystem,
  step: state.ui.views.resources.export.step,
  values: state.ui.views.resources.export.values,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  clearTemp,
  createFolder,
  deletePath,
  exportResource,
  setTemp,
  uploadFile,
}, dispatch);

const mergeProps = (stateProps, dispatchProps, ownProps) => {
  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,

  };
};

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(ResourceExport);
