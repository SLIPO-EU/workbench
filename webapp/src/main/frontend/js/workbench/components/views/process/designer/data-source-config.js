import * as React from 'react';
import PropTypes from 'prop-types';
import {
  Button, Card, CardBlock, Row, Col,
} from 'reactstrap';

/**
 * Presentational component that wraps the data source configuration options
 *
 * @class DataSourceConfig
 * @extends {React.Component}
 */
class DataSourceConfig extends React.Component {

  constructor(props) {
    super(props);
  }

  static propTypes = {
    step: PropTypes.object.isRequired,
    dataSource: PropTypes.object.isRequired,
    configuration: PropTypes.object,
    configureStepDataSourceEnd: PropTypes.func.isRequired,
  }

  save(e) {
    // TODO: Return a valid configuration
    this.props.configureStepDataSourceEnd(this.props.step, this.props.dataSource, {});
  }

  cancel(e) {
    this.props.configureStepDataSourceEnd(this.props.step, this.props.dataSource, null);
  }

  render() {
    return (
      <Card>
        <CardBlock className="card-body">
          <Row className="mb-2">
            <Col>
              <i className={this.props.dataSource.iconClass + ' mr-2'}></i><span>{this.props.dataSource.title}</span>
            </Col>
          </Row>
          <Row className="mb-2">
            <Col>
              <Button color="danger" onClick={(e) => { this.cancel(e); }} className="float-left">Cancel</Button>
              <Button color="primary" onClick={(e) => { this.save(e); }} className="float-right">Save</Button>
            </Col>
          </Row>
        </CardBlock>
      </Card>
    );
  }
}

export default DataSourceConfig;
