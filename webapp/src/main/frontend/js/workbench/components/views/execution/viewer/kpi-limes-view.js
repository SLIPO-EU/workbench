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
  StackedBarChart,
} from '../../../helpers';

class KpiLimesView extends React.Component {

  constructor(props) {
    super(props);
  }

  static propTypes = {
    data: PropTypes.arrayOf(PropTypes.shape({
      key: PropTypes.string.isRequired,
      value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
      description: PropTypes.string,
    })),
    file: PropTypes.object.isRequired,
    original: PropTypes.object,
  }

  getSeries() {
    const { original: data = null } = this.props;

    if (!data) {
      return [];
    }

    const sizeKeys = [
      'Input Source Size',
      'Input Target Size',
      'Output Acceptance',
    ];
    const sizeSeries = [{
      label: sizeKeys[0],
      [sizeKeys[0]]: data.inputSizes.source,
    }, {
      label: sizeKeys[1],
      [sizeKeys[1]]: data.inputSizes.target,
    }, {
      label: sizeKeys[2],
      [sizeKeys[2]]: data.outputSizes.acceptance,
    }];
    const percentKeys = [
      'Acceptance Precision',
      'Acceptance Recall',
      'Acceptance F-Measure',
      'All Precision',
      'All Recall',
      'All F-Measure',
    ];
    const percentSeries = [{
      label: percentKeys[0],
      [percentKeys[0]]: data.pseudoPRF.acceptance.precision
    }, {
      label: percentKeys[1],
      [percentKeys[1]]: data.pseudoPRF.acceptance.recall,
    }, {
      label: percentKeys[2],
      [percentKeys[2]]: data.pseudoPRF.acceptance['f-measure'],
    }, {
      label: percentKeys[3],
      [percentKeys[3]]: data.pseudoPRF.all.precision,
    }, {
      label: percentKeys[4],
      [percentKeys[4]]: data.pseudoPRF.all.recall,
    }, {
      label: percentKeys[5],
      [percentKeys[5]]: data.pseudoPRF.all['f-measure'],
    }];

    return [sizeKeys, sizeSeries, percentKeys, percentSeries];
  }

  render() {
    const { data, file, original } = this.props;

    if (!data) {
      return null;
    }

    const [sizeKeys, sizeSeries, percentKeys, percentSeries] = this.getSeries();

    return (
      <div>
        <Card>
          <CardBody>
            <Row className="mb-4">
              <Col>
                <i className="fa fa-bar-chart pr-1"></i>Charts
              </Col>
            </Row>
            <Row>
              <Col>
                <StackedBarChart
                  data={sizeSeries}
                  indexBy={'label'}
                  keys={sizeKeys}
                  tooltip={(datum) => {
                    const { id, value } = datum;
                    const computedValue = Math.floor(value) === value ? value : value.toFixed(2);

                    return (
                      <div>
                        <div>{id}</div>
                        <div>{computedValue}</div>
                      </div>
                    );
                  }}
                />
              </Col>
            </Row>
            <Row>
              <Col>
                <StackedBarChart
                  data={percentSeries}
                  indexBy={'label'}
                  keys={percentKeys}
                  maxValue={1}
                  tooltip={(datum) => {
                    const { id, value } = datum;

                    return (
                      <div>
                        <div>{id}</div>
                        <div>{`${(value * 100).toFixed(2)} %`}</div>
                      </div>
                    );
                  }}
                />
              </Col>
            </Row>
          </CardBody>
        </Card>
        <Row>
          <Col>
            <Card>
              <CardBody>
                <KpiSharedView
                  data={data}
                  file={file}
                  original={original}
                />
              </CardBody>
            </Card>
          </Col>
        </Row>
      </div>
    );
  }
}

export default KpiLimesView;
