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
  EnumCellValueType
} from '../../../model/map-viewer';

const PropertyTypeMapping = [{
  id: 'homepage',
  type: EnumCellValueType.Link
}, {
  id: 'image',
  type: EnumCellValueType.Image,
}];

const createColumns = (props) => {
  const { provenance: { stepRow, inputRow, properties, dataRows: data } } = props;
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
    maxWidth: 120,
    Header: (cell) => {
      return (<span />);
    },
    Cell: (cell) => {
      return (<span>{properties[cell.index]}</span>);
    },
  });

  // Add steps
  stepRow.forEach((step, stepIndex) => {
    // Ignore attribute column
    if (stepIndex === 0) {
      return;
    }
    columns.push({
      id: `Step-${step.index}`,
      Header: () => {
        return (<span><i className={`${step.iconClass || ''} pr-2`} />{step.value}</span>);
      },
      headerStyle: {
        textAlign: 'left',
      },
      // Add step input
      columns: inputRow.filter(i => i.step === step.index).map((input, inputIndex) => {
        const id = `Input-${step.index}-${inputIndex}`;
        mappings[id] = ++columnIndex;
        return {
          id,
          accessor: id,
          minWidth: 200,
          Header: () => {
            return (<span className={input.selected ? 'slipo-tl-selected' : ''}>{input.value}</span>);
          },
          headerStyle: {
            textAlign: 'left',
          },
          className: classnames({
            'slipo-tl-cell-odd': step.index % 2 === 1,
            'slipo-tl-cell-even': step.index % 2 === 0,
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
  const { provenance: { stepRow, inputRow, properties, dataRows: data } } = props;

  return properties.map((attr, rowIndex) => {
    let columnIndex = 0;
    const row = {
      'attribute': attr
    };
    stepRow.forEach((step, index) => {
      // Ignore attribute column
      if (index === 0) {
        return;
      }
      inputRow.filter(i => i.step === step.index).map((input, inputIndex) => {
        const id = `Input-${step.index}-${inputIndex}`;
        const cell = data[rowIndex][++columnIndex];
        row[id] = cell.value
      });
    });
    return row;
  });
}

const isLink = (value) => {
  const urlRegEx = /^(http:\/\/www\.|https:\/\/www\.|http:\/\/|https:\/\/)?[a-z0-9]+([-.]{1}[a-z0-9]+)*\.[a-z]{2,5}(:[0-9]{1,5})?(\/.*)?$/gi;

  return urlRegEx.test(value);;
}

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

  renderTable() {
    const { provenance } = this.props;

    return (
      <div style={{ overflow: 'auto', height: 480 }}>
        <table className="slipo-tl-table">
          <tbody>
            {/* Steps */}
            <tr key="pivot-row">
              {
                provenance.stepRow.map((cell, index) => {
                  return (
                    <td
                      key={`step-${index}`}
                      rowSpan={cell.rowSpan || 1}
                      colSpan={cell.colSpan || 1}
                      className={classnames({
                        'slipo-tl-step': true,
                        'slipo-tl-cell': index !== 0,
                        'slipo-tl-cell-odd': index !== 0 && index % 2 === 1,
                        'slipo-tl-cell-even': index !== 0 && index % 2 === 0,
                        'slipo-tl-selected': cell.selected === true,
                      })}
                    >
                      <i className={`${cell.iconClass || ''} pr-2`} />{cell.value}
                    </td>
                  );
                })
              }
            </tr>
            {/* Inputs */}
            <tr key="input-row">
              {
                provenance.inputRow.map((cell, index) => {
                  return (
                    <td
                      key={`input-${index}`}
                      rowSpan={cell.rowSpan || 1}
                      colSpan={cell.colSpan || 1}
                      className={classnames({
                        'slipo-tl-cell': true,
                        'slipo-tl-cell-odd': cell.step % 2 === 1,
                        'slipo-tl-cell-even': cell.step % 2 === 0,
                        'slipo-tl-selected': cell.selected === true,
                      })}
                    >
                      {cell.value}
                    </td>
                  );
                })
              }
            </tr>
            {/* Values */}
            {
              provenance.dataRows.map((row, rowIndex) => {
                return (
                  <tr key={`row-${rowIndex}`}>
                    {
                      row.map((cell, cellIndex) => {
                        return (
                          <td
                            key={`cell-${rowIndex}-${cellIndex}`}
                            rowSpan={cell.rowSpan || 1}
                            colSpan={cell.colSpan || 1}
                            className={classnames({
                              'slipo-tl-cell': true,
                              'slipo-tl-cell-odd': cellIndex !== 0 && cell.step % 2 === 1,
                              'slipo-tl-cell-even': cellIndex !== 0 && cell.step % 2 === 0,
                              'slipo-tl-selected': cell.selected || cell.modified === true,
                            })}
                          >
                            {renderCell(cell)}
                          </td>
                        );
                      })
                    }
                  </tr>
                );
              })
            }
          </tbody>
        </table>
      </div>
    );
  }

  render() {
    const { provenance } = this.props;

    if (!provenance) {
      return null;
    }

    return (
      <Card>
        <CardHeader className="handle">
          <div style={{ display: 'flex' }}>
            <div style={{ flex: '0 0 25px' }}><i className="fa fa-map-marker"></i></div>
            <div style={{ flex: '1 1 100%' }}>{`${provenance.layer} - ${provenance.featureId}`}</div>
            <div style={{ cursor: 'pointer' }}><i className="fa fa-remove" onClick={(e) => this.props.close(e)}></i></div>
          </div>
        </CardHeader>
        <CardBody>
          <Table
            className="slipo-provenance-table"
            id={'provenance'}
            name={'provenance'}
            minRows={provenance.properties.length}
            columns={createColumns(this.props)}
            data={createRows(this.props)}
            noDataText="No data found"
            showPagination={false}
            defaultPageSize={Number.MAX_VALUE}
            style={{ maxHeight: 480 }}
          />
          {/* {this.renderTable()} */}
        </CardBody>
      </Card>
    );
  }

}

export default FeatureProvenanceViewer;
