import * as React from 'react';
import PropTypes from 'prop-types';

import ReactJson from 'react-json-view';

import {
  Col,
  Row,
} from 'reactstrap';

export const KpiGridColumns = [{
  Header: 'Name',
  accessor: 'key',
  headerStyle: { 'textAlign': 'left' },
}, {
  Header: 'Value',
  accessor: 'value',
  headerStyle: { 'textAlign': 'center' },
}, {
  Header: 'Description',
  accessor: 'description',
  headerStyle: { 'textAlign': 'left' },
  show: false,
}];

class KpiSharedView extends React.Component {

  constructor(props) {
    super(props);
  }

  static propTypes = {
    data: PropTypes.arrayOf(PropTypes.shape({
      key: PropTypes.string.isRequired,
      value: PropTypes.oneOfType([PropTypes.string, PropTypes.number, PropTypes.bool]),
      description: PropTypes.string,
    })),
    file: PropTypes.object.isRequired,
    original: PropTypes.object.isRequired,
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
            <span>{` KPI File : ${this.props.file.filePath.split('/').reverse()[0]}`}</span>
          </Col>
        </Row>
        <Row>
          <Col>
            <ReactJson
              collapsed={1}
              displayDataTypes={false}
              enableClipboard={false}
              name={'metadata'}
              src={this.props.original}
              style={{
                maxHeight: 600,
                overflowY: 'auto'
              }} />
          </Col>
        </Row>
      </div>
    );
  }

}

export default KpiSharedView;
