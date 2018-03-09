import * as React from 'react';
import PropTypes from 'prop-types';

import {
  Col,
  Row,
} from 'reactstrap';

import {
  BarChart,
} from '../../../helpers';

class KpiChartView extends React.Component {

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

    const config = {
      series: [{
        name: 'KPI',
        points: this.props.data.map((d) => ({
          x: d.key, y: d.value, label: d.description || '-',
        }))
      }],
      options: {
        showLabels: true,
      }
    };

    return (
      <div>
        <Row className="mb-4">
          <Col>
            <i className="fa fa-bar-chart"></i>
            <span>{` KPI Chart - ${this.props.file.filePath}`}</span>
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
            <BarChart {...config} />
          </Col>
        </Row>
      </div>
    );
  }

}

export default KpiChartView;
