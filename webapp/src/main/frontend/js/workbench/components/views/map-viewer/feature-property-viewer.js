import _ from 'lodash';
import * as React from 'react';
import PropTypes from 'prop-types';

import {
  Card,
  CardBody,
  CardHeader,
} from 'reactstrap';

import {
  Table,
} from '../../helpers';

import {
  FEATURE_COLOR_PROPERTY,
  FEATURE_ICON_PROPERTY,
  FEATURE_LAYER_PROPERTY,
  FEATURE_NAME,
  FEATURE_OUTPUT_KEY,
  FEATURE_PROPERTY_PREFIX,
  FEATURE_ID,
  FEATURE_URI,
} from '../../helpers/map/model/constants';

import {
  LayerLegend,
} from './';

const createColumns = (props) => {
  const { layers, features } = props;

  const metadata = features.reduce((result, f) => {
    const properties = f.getProperties();
    if (!result.layers[properties[FEATURE_LAYER_PROPERTY]]) {
      const keys = Object.keys(properties)
        .filter((k) => k !== f.getGeometryName() && !k.startsWith(FEATURE_PROPERTY_PREFIX));

      result.keys = _.uniq([...result.keys, ...keys]).sort();
      result.layers = {
        ...result.layers,
        [properties[FEATURE_LAYER_PROPERTY]]: properties[FEATURE_LAYER_PROPERTY],
      };
    }
    return result;
  }, { layers: {}, keys: [] });

  return [FEATURE_OUTPUT_KEY, FEATURE_ICON_PROPERTY, FEATURE_NAME, ...metadata.keys].map((key) => {
    switch (key) {
      case FEATURE_OUTPUT_KEY:
        return {
          expander: true,
          Header: '',
          id: 'actions',
          style: { 'textAlign': 'center' },
          width: 30,
          Expander: (cell) => {
            return cell.original[FEATURE_OUTPUT_KEY] ?
              <span>
                <i data-action="provenance" title="View POI provenance" className="fa fa-search slipo-table-row-action" />
              </span> : null;
          },
        };

      case FEATURE_NAME:
        return {
          Header: key,
          accessor: key,
          show: false,
        };

      case FEATURE_ICON_PROPERTY: {
        return {
          Header: 'Source',
          accessor: key,
          maxWidth: 100,
          className: 'd-flex',
          Cell: (cell) => {
            const layer = layers.find(l => l.tableName === cell.original[FEATURE_LAYER_PROPERTY]);
            const { style } = layer;

            return (
              <React.Fragment>
                {cell.original[FEATURE_ICON_PROPERTY] &&
                  <div style={{ color: cell.original[FEATURE_COLOR_PROPERTY], font: 'normal 16px FontAwesome' }}>
                    {cell.original[FEATURE_ICON_PROPERTY]}
                  </div>
                }
                {!cell.original[FEATURE_ICON_PROPERTY] && style &&
                  <div>
                    <LayerLegend
                      height={22}
                      width={22}
                      symbol={style.symbol}
                      size={16}
                      fillColor={style.fill.color}
                      strokeWidth={style.stroke.width}
                      strokeColor={style.stroke.color}
                      opacity={style.opacity}
                    />
                  </div>
                }
                <div className="pl-2">{cell.original[FEATURE_NAME]}</div>
              </React.Fragment>
            );
          }
        };
      }

      default:

        return {
          Header: key,
          accessor: key,
        };
    }
  });
};

const createRows = (props) => {
  const { features } = props;

  return features.map((f) => {
    return f.getProperties();
  });
};

class FeaturePropertyViewer extends React.Component {

  constructor(props) {
    super(props);
  }

  static propTypes = {
    features: PropTypes.arrayOf(PropTypes.object).isRequired,
  }

  handleRowAction(rowInfo, e, handleOriginal) {
    switch (e.target.getAttribute('data-action')) {
      case 'provenance':
        this.props.fetchFeatureProvenance(
          rowInfo.original[FEATURE_OUTPUT_KEY],
          rowInfo.original[FEATURE_ID],
          rowInfo.original[FEATURE_URI],
        );
        break;
      default:
        if (handleOriginal) {
          handleOriginal();
        }
        break;
    }
  }


  isSelected(rowInfo) {
    if (!rowInfo || !this.props.selectedFeature) {
      return false;
    }
    const { outputKey, featureId } = this.props.selectedFeature;

    return (outputKey === rowInfo.original[FEATURE_OUTPUT_KEY]) && (featureId === rowInfo.original[FEATURE_ID]);
  }

  render() {
    const { features } = this.props;

    if (!features.length) {
      return null;
    }

    return (
      <Card style={{ maxHeight: 380 }}>
        <CardHeader className="handle" style={{ padding: '0.75rem 1rem' }}>
          <div style={{ display: 'flex' }}>
            <div style={{ flex: '0 0 25px' }}><i className="fa fa-map-o"></i></div>
            <div style={{ flex: '1 1 100%' }}>{features.length > 1 ? `${features.length} features selected` : '1 feature selected'}</div>
            <div style={{ cursor: 'pointer' }}>
              <i className="fa fa-remove" onClick={(e) => this.props.close(e)} title="Close"></i>
            </div>
          </div>
        </CardHeader>
        <CardBody>
          <Table
            id={'features'}
            name={'features'}
            minRows={10}
            columns={createColumns(this.props)}
            data={createRows(this.props)}
            noDataText="No features selected"
            showPagination={false}
            defaultPageSize={Number.MAX_VALUE}
            style={{ maxHeight: 300 }}
            getTdProps={(state, rowInfo, column) => ({
              onClick: this.handleRowAction.bind(this, rowInfo)
            })}
            getTrProps={(state, rowInfo) => ({
              className: (this.isSelected(rowInfo) ? 'slipo-react-table-selected' : null),
            })}
          />
        </CardBody>
      </Card>
    );
  }

}

export default FeaturePropertyViewer;
