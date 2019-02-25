import * as React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import moment from '../../../moment-localized';

import {
  Card,
  CardBody,
  CardHeader,
} from 'reactstrap';

import {
  Table,
} from '../../helpers';

import {
  ATTRIBUTE_GEOMETRY,
  ATTRIBUTE_PROPERTIES,
  Attributes,
  EnumCellValueType,
} from '../../../model/map-viewer';

const PropertyTypeMapping = [{
  id: 'homepage',
  type: EnumCellValueType.Link
}, {
  id: 'image',
  type: EnumCellValueType.Image,
}];

const createColumns = (state, componentProps) => {
  const { filterable } = state;
  const { evolution: { header, properties, data, processVersion } } = componentProps;
  const columns = [];

  // Attribute column
  columns.push({
    id: 'attribute',
    accessor: 'attribute',
    filterable,
    minWidth: 200,
    Header: () => {
      return (<span />);
    },
    Cell: (cell) => {
      const attr = Attributes.find(a => a.key === properties[cell.index]);
      return (<span>{attr ? attr.title : properties[cell.index]}</span>);
    },
  });

  // Add revisions
  header.forEach((header) => {
    columns.push({
      id: `revision-${header.version}`,
      minWidth: 250,
      Header: () => {
        return (
          <div className={header.version === processVersion ? 'slipo-tl-selected' : ''}>
            <div>
              {header.version === processVersion &&
                <React.Fragment>
                  <i className="fa fa-code-fork pr-2" />{`Revision ${header.version}`}
                </React.Fragment>
              }
              {header.version !== processVersion &&
                <a className="slipo-table-row-action" onClick={(e) => {
                  e.preventDefault();
                  componentProps.viewRevision(header.id, header.version, header.executionId);
                }}>
                  <i className="fa fa-code-fork pr-2" />
                  <span style={{ textDecoration: 'underline' }}>{`Revision ${header.version}`}</span>
                </a>
              }
            </div>
            <div>
              <i className="fa fa-user pr-2" />{header.userName}
            </div>
            <div>
              <i className="fa fa-clock-o pr-2" />{moment(header.submittedOn).format('DD/MM/YYYY h:mm:ss a')}
            </div>
          </div>
        );
      },
      headerStyle: {
        textAlign: 'left',
      },
      headerClassName: 'slipo-th-level-1',
      className: classnames({
        'slipo-tl-cell-default': true,
        'slipo-tl-cell-odd': header.index % 2 === 1,
        'slipo-tl-version-separator': true,
      }),
      Cell: (cellProps) => {
        const cell = data[cellProps.index][header.index + 1];
        return renderCell(cell, data[cellProps.index], cellProps, componentProps);
      },
    });
  });

  return columns;
};

const createRows = (props) => {
  const { evolution: { header, properties, data } } = props;

  return properties.map((prop, rowIndex) => {
    const attr = Attributes.find(a => a.key === prop);
    const row = {
      attribute: attr.title,
    };

    header.forEach((header, columnIndex) => {
      const id = `revision-${header.version}`;
      const cell = data[rowIndex][columnIndex + 1];
      row[id] = header.showUpdates ? cell.value : cell.initial;
    });

    return row;
  });
};

const isLink = (value) => {
  const urlRegEx = /^(http:\/\/www\.|https:\/\/www\.|http:\/\/|https:\/\/)?[a-z0-9]+([-.]{1}[a-z0-9]+)*\.[a-z]{2,5}(:[0-9]{1,5})?(\/.*)?$/gi;

  return urlRegEx.test(value);
};


const renderCell = (cell, row, cellProps, componentProps) => {
  const { evolution: { header, geometryVersion }, onEvolutionGeometryChange, onEvolutionUpdatesToggle } = componentProps;

  const revision = header.find(h => h.version === cell.version);

  const previousCells = row.filter(c => c.version != null && c.version < cell.version);
  const previousRevisions = header.filter(h => h.version < cell.version);

  const value = revision.showUpdates ? cell.value : cell.initial;
  const previousValue = previousCells.length === 0 ?
    value :
    previousRevisions[previousRevisions.length - 1].showUpdates ?
      previousCells[previousCells.length - 1].value : previousCells[previousCells.length - 1].initial;

  // Handle property updates
  if (cell.property === ATTRIBUTE_PROPERTIES) {
    return (
      <div
        className={classnames({
          'badge-pill p-1 m-auto w-50 text-center slipo-action-icon': true,
          'updates-show': !revision.showUpdates,
          'updates-hide': revision.showUpdates,
        })}
        onClick={() => onEvolutionUpdatesToggle(revision.version)}
      >
        <i className={`fa fa-${revision.showUpdates ? 'remove' : 'plus'} pr-2`} />{revision.showUpdates ? 'Hide' : 'Show'}
      </div>
    );
  }

  // Handle geometry property
  if (cell.property === ATTRIBUTE_GEOMETRY) {
    if (cell.version === geometryVersion) {
      return (
        <div className="badge-pill geometry-active p-1 m-auto w-50 text-center">Selected</div>
      );
    }
    return (
      <div
        className="badge-pill geometry-inactive p-1 m-auto w-50 text-center slipo-action-icon"
        onClick={() => onEvolutionGeometryChange(cell.version)}
      >
        <i className="fa fa-search pr-2" />View
      </div>
    );
  }

  // Other properties
  const propertyType = PropertyTypeMapping.find(p => p.id === cell.property);

  if (propertyType && isLink(value)) {
    switch (propertyType.type) {
      case EnumCellValueType.Link:
        return (
          <span
            className={classnames({
              'slipo-tl-modified': value !== previousValue,
            })}
          >
            <a href={value} target="_blank">{value}</a>
          </span>
        );
      case EnumCellValueType.Image:
        return (
          <div
            className={classnames({
              'slipo-tl-image-container': true,
              'slipo-tl-modified': value !== previousValue,
            })}
          >
            <img className="slipo-tl-image" src={value} />
          </div>
        );
    }
  }

  return (
    <span
      className={classnames({
        'slipo-tl-modified': value !== previousValue,
      })}
    >
      {value}
    </span>
  );
};

class FeatureEvolutionViewer extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      filterable: false,
    };
  }

  static propTypes = {
    evolution: PropTypes.object,
  }

  toggleFilterable() {
    this.setState({
      filterable: !this.state.filterable,
    });
  }

  render() {
    const { filterable } = this.state;
    const { evolution } = this.props;

    if (!evolution) {
      return null;
    }

    return (
      <Card>
        <CardHeader className="handle">
          <div style={{ display: 'flex' }}>
            <div style={{ flex: '0 0 25px' }}><i className="fa fa-map-marker"></i></div>
            <div style={{ flex: '1 1 100%' }}>{`${evolution.layer} - ${evolution.featureId}`}</div>
            <div style={{ cursor: 'pointer', opacity: filterable ? 1 : 0.5 }}>
              <i className="fa fa-filter pr-2 " onClick={() => this.toggleFilterable()} title="Filter attributes"></i>
            </div>
            <div style={{ cursor: 'pointer' }}>
              <i className="fa fa-remove" onClick={(e) => this.props.close(e)} title="Close"></i>
            </div>
          </div>
        </CardHeader>
        <CardBody>
          <Table
            id={'evolution'}
            name={'evolution'}
            className="slipo-evolution-table"
            columns={createColumns(this.state, this.props)}
            data={createRows(this.props)}
            defaultPageSize={Number.MAX_VALUE}
            minRows={evolution.properties.length}
            noDataText="No data found"
            showPagination={false}
            style={{ maxHeight: 480 }}
          />
        </CardBody>
      </Card>
    );
  }

}

export default FeatureEvolutionViewer;
