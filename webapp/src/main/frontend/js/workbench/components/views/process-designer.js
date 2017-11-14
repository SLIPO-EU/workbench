import * as React from 'react';
import * as ReactRedux from 'react-redux';
import { FormattedTime } from 'react-intl';
import {
  Card, CardBlock, Row, Col, Button,
} from 'reactstrap';
import { bindActionCreators } from 'redux';

import {
  EnumHarvester,
  EnumTool,
  EnumOperation,
  EnumViews,
  Harvester,
  Operation,
  Designer,
  Toolbox,
  StepConfig,
  DataSourceConfig,
} from './process/designer';
import {
  reset,
  addStep,
  removeStep,
  configureStepBegin,
  configureStepEnd,
  addStepInput,
  removeStepInput,
  addStepDataSource,
  removeStepDataSource,
  configureStepDataSourceBegin,
  configureStepDataSourceEnd,
  setActiveStep,
  setActiveStepInput,
  setActiveStepDataSource,
  setActiveResource,
} from '../../ducks/ui/views/process-designer';

/**
 * Create/Update a POI data integration process
 *
 * @class ProcessDesigner
 * @extends {React.Component}
 */
class ProcessDesigner extends React.Component {

  constructor(props) {
    super(props);
  }

  render() {
    return (
      <div className="animated fadeIn">
        <Row>
          <Col className="col-12">
            {this.props.view.type === EnumViews.Designer &&
              <Card>
                <CardBlock className="card-body">
                  <Row className="mb-2">
                    <Col>
                      <Toolbox />
                      <p className='text-muted slipo-pd-tip float-left'>Drag SLIPO Toolkit components to the designer surface for building a workflow.</p>
                    </Col>
                  </Row>
                  <Row>
                    <Col>
                      <Button color="warning" onClick={this.props.reset} className="float-left">Clear</Button>
                      <Button color="primary" className="float-right">Save</Button>
                    </Col>
                  </Row>
                </CardBlock>
                <CardBlock className="card-body">
                  <Row className="mb-2">
                    <Col style={{ padding: '9px' }}>
                      < Designer
                        active={this.props.active}
                        steps={this.props.steps}
                        addStep={this.props.addStep}
                        configureStepBegin={this.props.configureStepBegin}
                        removeStep={this.props.removeStep}
                        addStepInput={this.props.addStepInput}
                        removeStepInput={this.props.removeStepInput}
                        addStepDataSource={this.props.addStepDataSource}
                        removeStepDataSource={this.props.removeStepDataSource}
                        configureStepDataSourceBegin={this.props.configureStepDataSourceBegin}
                        setActiveStep={this.props.setActiveStep}
                        setActiveStepInput={this.props.setActiveStepInput}
                        setActiveStepDataSource={this.props.setActiveStepDataSource}
                        setActiveResource={this.props.setActiveResource}
                      />
                    </Col>
                  </Row>
                </CardBlock>
              </Card>
            }
            {this.props.view.type === EnumViews.StepConfiguration &&
              <StepConfig
                step={this.props.view.step}
                configuration={this.props.view.configuration}
                configureStepEnd={this.props.configureStepEnd}
              />
            }
            {this.props.view.type === EnumViews.DataSourceConfiguration &&
              <DataSourceConfig
                step={this.props.view.step}
                dataSource={this.props.view.dataSource}
                configuration={this.props.view.configuration}
                configureStepDataSourceEnd={this.props.configureStepDataSourceEnd}
              />
            }
          </Col>
        </Row>
      </div>
    );
  }
}


const mapStateToProps = (state) => ({
  view: state.ui.views.process.designer.view,
  active: state.ui.views.process.designer.active,
  steps: state.ui.views.process.designer.steps,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  reset,
  addStep,
  removeStep,
  configureStepBegin,
  configureStepEnd,
  addStepInput,
  removeStepInput,
  addStepDataSource,
  removeStepDataSource,
  configureStepDataSourceBegin,
  configureStepDataSourceEnd,
  setActiveStep,
  setActiveStepInput,
  setActiveStepDataSource,
  setActiveResource,
}, dispatch);

const mergeProps = (stateProps, dispatchProps, ownProps) => {
  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
  };
};

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(ProcessDesigner);
