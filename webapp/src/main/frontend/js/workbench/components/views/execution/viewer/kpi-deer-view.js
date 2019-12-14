import _ from 'lodash';
import * as React from 'react';
import PropTypes from 'prop-types';

import {
  Col,
  Card,
  CardBody,
  Row,
} from 'reactstrap';

import {
  KpiSharedView,
} from '.';

import {
  PieChart,
} from '../../../helpers';

class KpiDeerView extends React.Component {

  static propTypes = {
    data: PropTypes.arrayOf(PropTypes.shape({
      key: PropTypes.string.isRequired,
      value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
      description: PropTypes.string,
    })),
    file: PropTypes.object.isRequired,
    original: PropTypes.object,
  }

  getChartData() {
    const { original: data } = this.props;
    if (!data) {
      return [];
    }

    const { enrichedPOIs: enriched, totalPOIs: total } = data.globalStats;

    return [
      {
        id: 'Unmodified',
        label: 'Enriched POIs',
        value: enriched,
        color: 'hsl(33, 70%, 50%)'
      },
      {
        id: 'Enriched',
        label: 'Unmodified POIs',
        value: total - enriched,
        color: 'hsl(176, 70%, 50%)'
      },
    ];
  }

  render() {
    if (!this.props.data) {
      return null;
    }

    const chartData = this.getChartData();
    const totalPOIs = _.sumBy(chartData, 'value');

    return (
      <div>
        <Row>
          <Col xl={6}>
            <Card>
              <CardBody>
                <KpiSharedView
                  data={this.props.data}
                  file={this.props.file}
                  original={this.props.original}
                />
              </CardBody>
            </Card>
          </Col>
          <Col xl={6}>
            <Card>
              <CardBody>
                <Row className="mb-4">
                  <Col>
                    <i className="fa fa-pie-chart pr-1"></i>
                    <span>{`Total POIs ${totalPOIs}`}</span>
                  </Col>
                </Row>
                <Row>
                  <Col>
                    <PieChart
                      data={chartData}
                    />
                  </Col>
                </Row>
              </CardBody>
            </Card>
          </Col>
        </Row>
      </div>
    );
  }
}

export default KpiDeerView;
