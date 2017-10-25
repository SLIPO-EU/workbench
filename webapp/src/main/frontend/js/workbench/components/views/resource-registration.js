import * as React from 'react';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';

import {
  Card,
  CardBlock,
  Col,
  Row,
} from 'reactstrap';

import { ResourceWizard } from './resource/register/';
import { createResource } from '../../ducks/data/resources';
import { saveTempResource, clearTempResource } from '../../ducks/ui/views/resource-registration';

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
        <Col sm="12" md="12" lg="6">
          <Card>
            <CardBlock className="card-body">
              <ResourceWizard 
                saveTemp={this.props.saveTempResource}
                clearTemp={this.props.clearTempResource}
                initialActive={this.props.step}
                initialValues={this.props.values}
                createResource={this.props.createResource} 
                goTo={this.props.history.push}
                fsResources={this.props.fsResources}
              />
            </CardBlock>
          </Card>
        </Col>
      </Row>
    );
  }

}

const mapStateToProps = (state) => ({
  values: state.ui.views.resources.registration.values,
  step: state.ui.views.resources.registration.step,
  fsResources: state.config.filesystem.files,

});
const mapDispatchToProps = (dispatch) => bindActionCreators({ 
  createResource, 
  saveTempResource, 
  clearTempResource, 
}, dispatch);

const mergeProps = (stateProps, dispatchProps, ownProps) => {
  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,

  };
};

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(ResourceRegistration);
