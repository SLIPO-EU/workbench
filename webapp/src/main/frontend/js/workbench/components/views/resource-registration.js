import * as React from 'react';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';

import {
  Card,
  CardBody,
  Col,
  Row,
} from 'reactstrap';

import { ResourceWizard } from './resource/register/';
import { create as createResource } from '../../ducks/ui/views/resource-explorer';
import { saveTempResource, clearTempResource } from '../../ducks/ui/views/resource-registration';

import {
  createFolder,
  uploadFile,
  deletePath,
} from '../../ducks/config';

/**
 * Register a new resource
 *
 * @class ResourceRegistration
 * @extends {React.Component}
 */
class ResourceRegistration extends React.Component {

  render() {
    return (
      <Row>
        <Col>
          <Card>
            <CardBody className="card-body">
              <ResourceWizard
                saveTemp={this.props.saveTempResource}
                clearTemp={this.props.clearTempResource}
                initialActive={this.props.step}
                initialValues={this.props.values}
                createResource={this.props.createResource}
                goTo={this.props.history.push}
                filesystem={this.props.filesystem}
                appConfiguration={this.props.appConfiguration}
                createFolder={this.props.createFolder}
                uploadFile={this.props.uploadFile}
                deletePath={this.props.deletePath}
              />
            </CardBody>
          </Card>
        </Col>
      </Row>
    );
  }
}

const mapStateToProps = (state) => ({
  values: state.ui.views.resources.registration.values,
  step: state.ui.views.resources.registration.step,
  filesystem: state.config.filesystem,
  appConfiguration: state.config,

});
const mapDispatchToProps = (dispatch) => bindActionCreators({
  createResource,
  saveTempResource,
  clearTempResource,
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

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(ResourceRegistration);
