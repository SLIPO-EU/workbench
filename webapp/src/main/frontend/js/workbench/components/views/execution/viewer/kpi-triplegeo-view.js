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
  BarChart,
  OpenLayers,
} from '../../../helpers';

import {
  kpiDataToSeriesByPrefix,
} from '../../../../util/chart';

import {
  toFeatureCollection,
} from '../../../../util/geojson';


const ATTRIBUTE_PREFIX = 'Attribute Statistics.';
const BBOX_PREFIX = 'MBR of transformed geometries (WGS84).';

class KpiTripleGeoView extends React.Component {

  static propTypes = {
    data: PropTypes.arrayOf(PropTypes.shape({
      key: PropTypes.string.isRequired,
      value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
      description: PropTypes.string,
    })),
    file: PropTypes.object.isRequired,
  }

  get bbox() {
    const bbox = this.props.data
      .filter((d) => {
        return d.key.startsWith(BBOX_PREFIX);
      })
      .map((d) => {
        return {
          ...d,
          key: d.key.substring(BBOX_PREFIX.length),
        };
      })
      .reduce((result, attr) => {
        switch (attr.key) {
          case 'X_min':
            result[0] = attr.value;
            break;
          case 'X_max':
            result[2] = attr.value;
            break;
          case 'Y_min':
            result[1] = attr.value;
            break;
          case 'Y_max':
            result[3] = attr.value;
            break;
        }
        return result;
      }, []);

    if (bbox.length !== 4) {
      return null;
    }

    return toFeatureCollection([{
      type: 'Polygon',
      coordinates: [[
        [bbox[0], bbox[1]],
        [bbox[0], bbox[3]],
        [bbox[2], bbox[3]],
        [bbox[2], bbox[1]],
      ]]
    }]);
  }

  get series() {
    return kpiDataToSeriesByPrefix(ATTRIBUTE_PREFIX, this.props.data);
  }

  render() {
    if (!this.props.data) {
      return null;
    }
    const series = this.series;
    const bbox = this.bbox;

    return (
      <div>
        <Row>
          <Col xl={bbox ? 7 : 12}>
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
          {bbox &&
            <Col xl="5">
              <Card>
                <CardBody>
                  <Row className="mb-4">
                    <Col>
                      <i className="fa fa-map-o pr-1"></i>
                      <span>{'Bounding Box'}</span>
                    </Col>
                  </Row>
                  <Row>
                    <Col>
                      <OpenLayers.Map minZoom={1} maxZoom={18} zoom={15} center={this.center} height={415}>
                        <OpenLayers.Layers>
                          <OpenLayers.Layer.OSM
                            key="osm"
                            url="http://tile.stamen.com/terrain/{z}/{x}/{y}.jpg"
                          />
                          <OpenLayers.Layer.GeoJSON
                            features={bbox}
                            fitToExtent={true}
                          />
                        </OpenLayers.Layers>
                      </OpenLayers.Map>
                    </Col>
                  </Row>
                </CardBody>
              </Card>
            </Col>
          }
        </Row>
        {series.length !== 0 &&
          <Card>
            <CardBody>
              <Row className="mb-4">
                <Col>
                  <i className="fa fa-bar-chart pr-1"></i>
                  <span>{'Attribute Statistics'}</span>
                </Col>
              </Row>
              <Row>
                <Col>
                  <BarChart
                    data={series}
                  />
                </Col>
              </Row>
            </CardBody>
          </Card>
        }
      </div>
    );
  }
}

export default KpiTripleGeoView;
