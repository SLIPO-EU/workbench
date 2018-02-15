import * as React from 'react';
import PropTypes from 'prop-types';

import {
  formatFileSize
} from '../../../../util';

import {
  EnumKpiViewMode,
} from '../../../../model';

import {
  Card,
  CardBody,
  Col,
  Row,
} from 'reactstrap';

import {
  FormattedTime
} from 'react-intl';

import {
  Table,
} from '../../../helpers';

import {
  EnumStepFileType,
  EnumTool,
  stepFileTypeToText,
  ToolIcons,
} from '../../process/designer';

function isMapSupported(tool, type, table) {
  if (!table) {
    return false;
  }
  switch (tool) {
    case EnumTool.TripleGeo:
      return (type === EnumStepFileType.OUTPUT);

    case EnumTool.LIMES:
      return (type === EnumStepFileType.INPUT);

    case EnumTool.FAGI: case EnumTool.DEER:
      return ((type === EnumStepFileType.INPUT) || (type === EnumStepFileType.OUTPUT));

    default:
      return false;

  }
}

function buildFileColumns(step) {
  return [{
    Header: 'id',
    accessor: 'id',
    show: false,
  }, {
    Header: () => <span><i className='fa fa-map-o' /></span>,
    id: 'map',
    width: 40,
    Cell: props => {
      if (!isMapSupported(step.component, props.original.type, props.original.tableName)) {
        return null;
      }
      if (props.original.showInMap) {
        return (
          <i data-action="remove-from-map" className='fa fa-check-square-o slipo-table-row-action'></i>
        );
      } else {
        return (
          <i data-action="add-to-map" className='fa fa-square-o slipo-table-row-action'></i>
        );
      }
    },
    style: { 'textAlign': 'center' },
  }, {
    Header: 'Actions',
    id: 'actions',
    width: 80,
    Cell: props => {
      switch (props.original.type) {
        case EnumStepFileType.CONFIGURATION: case EnumStepFileType.QA:
          return (
            <span>
              <i data-action="config-download" className='fa fa-cloud-download slipo-table-row-action p-1'></i>
            </span>
          );

        case EnumStepFileType.KPI:
          return (
            <span>
              <i data-action="config-download" className='fa fa-cloud-download slipo-table-row-action p-1'></i>
              <i data-action="kpi-view-grid" className='fa fa-th slipo-table-row-action p-1'></i>
              <i data-action="kpi-view-chart" className='fa fa-bar-chart slipo-table-row-action p-1'></i>
            </span>
          );

        default:
          return null;

      }
    },
  }, {
    Header: 'Type',
    accessor: 'type',
    maxWidth: 120,
    Cell: row => {
      return stepFileTypeToText(row.value);
    },
    headerStyle: { 'textAlign': 'center' },
    style: { 'textAlign': 'center' },
  }, {
    Header: 'Name',
    id: 'name',
    accessor: r => r.fileName,
    headerStyle: { 'textAlign': 'left' },
  }, {
    Header: 'Size',
    id: 'size',
    accessor: f => (formatFileSize(f.fileSize)),
    headerStyle: { 'textAlign': 'center' },
    style: { 'textAlign': 'center' },
  }];
}

export default class ExecutionFiles extends React.Component {

  constructor(props) {
    super(props);
  }

  static propTypes = {
    addToMap: PropTypes.func.isRequired,
    files: PropTypes.arrayOf(PropTypes.object).isRequired,
    removeFromMap: PropTypes.func.isRequired,
    selectedFile: PropTypes.number,
    selectFile: PropTypes.func.isRequired,
    selectKpi: PropTypes.func.isRequired,
    step: PropTypes.object.isRequired,
  };

  /**
   * Resolve step icon class
   *
   * @returns a CSS class
   * @memberof ExecutionStep
   */
  getIconClassName() {
    return (ToolIcons[this.props.step.component] || 'fa fa-cogs') + ' fa-2x pr-2';
  }


  /**
   * Handles row actions
   *
   * @param {any} rowInfo the rowInfo object for the selected row
   * @param {any} e react synthetic event instance
   * @param {any} handleOriginal the table's original event handler
   * @memberof Resources
   */
  handleRowAction(rowInfo, e, handleOriginal) {
    this.props.selectFile(rowInfo.row.id);

    switch (e.target.getAttribute('data-action')) {
      case 'add-to-map':
        this.props.addToMap(rowInfo.row.id);
        break;

      case 'remove-from-map':
        this.props.removeFromMap(rowInfo.row.id);
        break;

      case 'kpi-view-grid':
        this.props.selectKpi(rowInfo.row.id, EnumKpiViewMode.GRID);
        break;

      case 'kpi-view-chart':
        this.props.selectKpi(rowInfo.row.id, EnumKpiViewMode.CHART);
        break;

      default:
        if (handleOriginal) {
          handleOriginal();
        }
        break;
    }
  }

  isSelected(rowInfo) {
    return (rowInfo && this.props.selectedFile === rowInfo.row.id);
  }

  render() {
    return (
      <Card>
        <CardBody>
          <Row className="mb-4">
            <Col>
              <i className={this.getIconClassName()}></i>
              <span className="font-2xl">{this.props.step.name + ' - Files'}</span>
            </Col>
          </Row>
          <Row>
            <Col>
              <Table
                name="Step Files"
                id="step-files"
                noDataText="No files generated by this step"
                columns={buildFileColumns(this.props.step)}
                data={this.props.files}
                defaultPageSize={10}
                showPageSizeOptions={false}
                manual
                getTrProps={(state, rowInfo) => ({
                  className: (this.isSelected(rowInfo) ? 'slipo-react-table-selected' : null),
                })}
                getTdProps={(state, rowInfo, column) => ({
                  onClick: this.handleRowAction.bind(this, rowInfo)
                })}
                showPagination={false}
              />
            </Col>
          </Row>
        </CardBody>
      </Card>
    );
  }
}
