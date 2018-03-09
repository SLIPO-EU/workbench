import * as React from 'react';
import PropTypes from 'prop-types';

import {
  Col,
  Row,
} from 'reactstrap';

import {
  Table,
} from '../../../helpers';

export const KpiGridColumns = [{
  Header: 'Name',
  accessor: 'key',
  headerStyle: { 'textAlign': 'left' },
}, {
  Header: 'Value',
  accessor: 'value',
  headerStyle: { 'textAlign': 'center' },
  style: { 'textAlign': 'center' },
}, {
  Header: 'Description',
  accessor: 'description',
  headerStyle: { 'textAlign': 'left' },
}];

class KpiGridView extends React.Component {

  constructor(props) {
    super(props);
  }

  static propTypes = {
    hide: PropTypes.func,
    data: PropTypes.arrayOf(PropTypes.shape({
      key: PropTypes.string.isRequired,
      value: PropTypes.number.isRequired,
      description: PropTypes.string,
    })),
    file: PropTypes.object.isRequired,
  }

  render() {
    if (!this.props.data) {
      return null;
    }

    return (
      <div>
        <Row className="mb-4">
          <Col>
            <i className="fa fa-th"></i>
            <span>{` KPI Data - ${this.props.file.filePath}`}</span>
          </Col>
          {this.props.hide &&
            <Col>
              <div className="float-right">
                <i className="slipo-action-icon fa fa-times" onClick={() => { this.props.hide(); }}></i>
              </div>
            </Col>
          }
        </Row>
        <Row>
          <Col>
            <Table
              data={this.props.data}
              columns={KpiGridColumns}
            />
          </Col>
        </Row>
      </div>
    );
  }

}

export default KpiGridView;
