import * as React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';

import {
  Card,
  CardBody,
  CardHeader,
} from 'reactstrap';

import {
  Table,
} from '../../helpers';

import {
  Attributes,
  EnumCellValueType
} from '../../../model/map-viewer';

import {
  EnumTool
} from '../../../model/process-designer';

const PropertyTypeMapping = [{
  id: 'homepage',
  type: EnumCellValueType.Link
}, {
  id: 'image',
  type: EnumCellValueType.Image,
}];

const createColumns = (state, props) => {
  const { filterable } = state;
  const { provenance: { inputRow, properties, steps, dataRows: data } } = props;
  const columns = [];

  // Column to index map
  let columnIndex = 0;
  const mappings = {
    'attribute': columnIndex,
  };

  // Attribute column
  columns.push({
    id: 'attribute',
    accessor: 'attribute',
    filterable,
    maxWidth: 120,
    Header: () => {
      return (<span />);
    },
    Cell: (cell) => {
      const attr = Attributes.find(a => a.key === properties[cell.index]);
      return (<span>{attr ? attr.title : properties[cell.index]}</span>);
    },
  });

  // Add steps
  steps.forEach((step, stepIndex) => {
    columns.push({
      id: `Step-${step.index}`,
      Header: () => {
        return (<span><i className={`${step.iconClass || ''} pr-2`} />{step.name}</span>);
      },
      headerStyle: {
        textAlign: 'left',
      },
      headerClassName: 'slipo-th-level-1',
      // Add step input
      columns: inputRow.filter(i => i.step === step.index).map((input, inputIndex) => {
        const id = `Input-${step.index}-${inputIndex}`;
        mappings[id] = ++columnIndex;
        return {
          id,
          accessor: id,
          minWidth: 250,
          Header: () => {
            return (<span className={input.selected ? 'slipo-tl-selected' : ''}>{input.value}</span>);
          },
          headerStyle: {
            textAlign: 'left',
          },
          headerClassName: inputIndex === 0 ? 'slipo-th-level-2' : '',
          className: classnames({
            'slipo-tl-cell-default': step.index % 2 === 1,
            'slipo-tl-cell-fuse-input-1': step.tool === EnumTool.FAGI && inputIndex % 4 === 0,
            'slipo-tl-cell-fuse-input-2': step.tool === EnumTool.FAGI && inputIndex % 4 === 1,
            'slipo-tl-cell-fuse-action': step.tool === EnumTool.FAGI && inputIndex % 4 === 2,
            'slipo-tl-cell-fuse-value': step.tool === EnumTool.FAGI && inputIndex % 4 === 3,
            'slipo-tl-cell-enrich':
              (step.tool === EnumTool.DEER) &&
              (steps.length !== 1 || inputIndex !== 0),
            'slipo-tl-step-separator':
              // FAGI first input
              (step.tool === EnumTool.FAGI && inputIndex % 4 === 0) ||
              // Single DEER operation
              (steps.length === 1 && step.tool === EnumTool.DEER && inputIndex === 0) ||
              // DEER operation
              (steps.length !== 1 && step.tool === EnumTool.DEER),

          }),
          Cell: (props) => {
            const cell = data[props.index][mappings[props.column.id]];
            return renderCell(cell);
          }
        };
      })
    });
  });

  return columns;
};

const createRows = (props) => {
  const { provenance: { steps, inputRow, properties, dataRows: data } } = props;

  return properties.map((prop, rowIndex) => {
    let columnIndex = 0;
    const attr = Attributes.find(a => a.key === prop);
    const row = {
      'attribute': attr.title
    };
    steps.forEach((step, index) => {
      inputRow.filter(i => i.step === step.index).map((input, inputIndex) => {
        const id = `Input-${step.index}-${inputIndex}`;
        const cell = data[rowIndex][++columnIndex];
        row[id] = cell.value;
      });
    });
    return row;
  });
};

const isLink = (value) => {
  const urlRegEx = /^(http:\/\/www\.|https:\/\/www\.|http:\/\/|https:\/\/)?[a-z0-9]+([-.]{1}[a-z0-9]+)*\.[a-z]{2,5}(:[0-9]{1,5})?(\/.*)?$/gi;

  return urlRegEx.test(value);
};

const renderCell = (cell) => {
  const propertyType = PropertyTypeMapping.find(p => p.id === cell.property);

  if (propertyType && isLink(cell.value)) {
    switch (propertyType.type) {
      case EnumCellValueType.Link:
        return (
          <span className={cell.selected || cell.modified ? 'slipo-tl-selected' : ''}>
            <a href={cell.value} target="_blank">{cell.value}</a>
          </span>
        );
      case EnumCellValueType.Image:
        return (
          <img className="slipo-tl-image" src={cell.value} />
        );
    }
  }

  return (
    <span className={cell.selected || cell.modified ? 'slipo-tl-selected' : ''}>{cell.value}</span>
  );
};

class FeatureProvenanceViewer extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      filterable: false,
    };
  }

  static propTypes = {
    provenance: PropTypes.object,
  }

  handleRowAction(rowInfo, e, handleOriginal) {
    switch (e.target.getAttribute('data-action')) {
      default:
        if (handleOriginal) {
          handleOriginal();
        }
        break;
    }
  }

  toggleFilterable() {
    this.setState({
      filterable: !this.state.filterable,
    });
  }

  render() {
    const { filterable } = this.state;
    const { editActive, provenance } = this.props;

    if (!provenance) {
      return null;
    }

    return (
      <Card>
        <CardHeader className="handle">
          <div style={{ display: 'flex' }}>
            <div style={{ flex: '0 0 25px' }}><i className="fa fa-map-marker"></i></div>
            <div style={{ flex: '1 1 100%' }}>{`${provenance.layer} - ${provenance.featureId}`}</div>
            {!editActive &&
              <div style={{ cursor: 'pointer' }}>
                <i className="fa fa-pencil pr-2 " onClick={() => this.props.toggleEditor()} title="Edit feature"></i>
              </div>
            }
            <div style={{ cursor: 'pointer', opacity: filterable ? 1 : 0.5 }}>
              <i className="fa fa-filter pr-2 " onClick={() => this.toggleFilterable()} title="Filter attributes"></i>
            </div>
            {!editActive &&
              <div style={{ cursor: 'pointer' }}>
                <i className="fa fa-remove" onClick={(e) => this.props.close(e)} title="Close"></i>
              </div>
            }
          </div>
        </CardHeader>
        <CardBody>
          <Table
            id={'provenance'}
            name={'provenance'}
            className="slipo-provenance-table"
            columns={createColumns(this.state, this.props)}
            data={createRows(this.props)}
            defaultPageSize={Number.MAX_VALUE}
            minRows={provenance.properties.length}
            noDataText="No data found"
            showPagination={false}
            style={{ maxHeight: 480 }}
          />
        </CardBody>
      </Card>
    );
  }

}

export default FeatureProvenanceViewer;
