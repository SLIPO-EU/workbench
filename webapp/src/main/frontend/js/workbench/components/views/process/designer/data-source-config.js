import * as React from 'react';
import PropTypes from 'prop-types';
import {
  Button, Card, CardBlock, Row, Col,
} from 'reactstrap';

import { ResourceConfigWizard } from './';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';

import { saveTempConfig, clearTempConfig } from '../../../../ducks/ui/views/process-config-step';
import { configureStepEnd } from '../../../../ducks/ui/views/process-designer';


class DataSourceConfig extends React.Component {

  render() {
    return (
      <Row>
        <Col sm="12" md="12" lg="6">
          <Card>
            <CardBlock className="card-body">
              <ResourceConfigWizard 
                configureStepEnd={this.props.configureStepEnd}
                stepId= {this.props.step}
                saveTemp={this.props.saveTempConfig}
                clearTemp={() => (this.props.configureStepEnd(this.props.step,null))}
                initialActive={ this.props.dataSource.source==="FILESYSTEM" ? "FILESYSTEM" : this.props.dataSource.source==="EXTERNAL_URL" ? "external" : "metadata" }
                initialValues={this.props.values}
                filesystem={this.props.filesystem}
              />
            </CardBlock>
          </Card>
        </Col>
      </Row>
    );
  }

}


const mapStateToProps = (state) => ({
  values: state.ui.views.process.configuration.values,
  filesystem: state.config.filesystem,

});
const mapDispatchToProps = (dispatch) => bindActionCreators({  
  saveTempConfig, 
  clearTempConfig, 
  configureStepEnd,
}, dispatch);

const mergeProps = (stateProps, dispatchProps, ownProps) => {
  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,

  };
};

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(DataSourceConfig);
