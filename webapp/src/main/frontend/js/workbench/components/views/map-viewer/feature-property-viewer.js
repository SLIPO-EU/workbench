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
  FEATURE_PROPERTY_PREFIX,
  FEATURE_LAYER_PROPERTY,
  FEATURE_COLOR_PROPERTY,
  FEATURE_ICON_PROPERTY,
} from '../../helpers/map/model/constants';

const createColumns = (props) => {
  const { features } = props;
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
  }, { layers: {}, keys: [FEATURE_ICON_PROPERTY] });

  return metadata.keys.map((key) => {
    switch (key) {
      case FEATURE_ICON_PROPERTY:
        return {
          Header: '',
          accessor: key,
          maxWidth: 30,
          style: { 'textAlign': 'center' },
          Cell: (cell) => {
            return (
              <span style={{ color: cell.original[FEATURE_COLOR_PROPERTY], font: 'normal 16px FontAwesome' }}>
                {cell.original[FEATURE_ICON_PROPERTY]}
              </span>
            );
          }
        };

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

  render() {
    const { features } = this.props;

    if (!features.length) {
      return null;
    }

    return (
      <Card style={{ maxWidth: 600, maxHeight: 380 }}>
        <CardHeader>
          <i className="fa fa-map-o"></i>
          <span>{features.length > 1 ? `${features.length} features selected` : '1 feature selected'}</span>
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
            style={{ maxWidth: 600, maxHeight: 300 }}
          />
        </CardBody>
      </Card>
    );
  }

}

export default FeaturePropertyViewer;
