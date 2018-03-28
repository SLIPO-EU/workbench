import * as React from 'react';
import PropTypes from 'prop-types';

import {
  formatFileSize
} from '../../../../util';

import {
  EnumKpiViewMode,
} from '../../../../model/process-designer';

import {
  FormattedTime
} from 'react-intl';

import {
  Button,
  Card,
  CardBody,
  Col,
  Row,
} from 'reactstrap';

import {
  toast,
} from 'react-toastify';

import {
  Table,
  ToastTemplate,
} from '../../../helpers';

import {
  EnumStepFileType,
  EnumTool,
  ToolIcons,
} from '../../../../model/process-designer';

import {
  stepFileTypeToText,
} from '../../process/designer';

import {
  KpiChartView,
  KpiGridView,
} from './';

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

const tableColumns = [{
  Header: 'id',
  accessor: 'id',
  show: false,
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
            <i data-action="kpi-view" className='fa fa-bar-chart slipo-table-row-action p-1'></i>
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
  accessor: r => r.filePath,
  headerStyle: { 'textAlign': 'left' },
}, {
  Header: 'Size',
  id: 'size',
  accessor: f => (formatFileSize(f.fileSize)),
  headerStyle: { 'textAlign': 'center' },
  style: { 'textAlign': 'center' },
}];

export default class ExecutionStepDetails extends React.Component {

  constructor(props) {
    super(props);

    this.hideStepExecutionDetails = this.hideStepExecutionDetails.bind(this);
  }

  static propTypes = {
    hideStepExecutionDetails: PropTypes.func.isRequired,
    files: PropTypes.arrayOf(PropTypes.object).isRequired,
    resetSelectedFile: PropTypes.func.isRequired,
    resetSelectedKpi: PropTypes.func.isRequired,
    selectedFile: PropTypes.number,
    selectFile: PropTypes.func.isRequired,
    selectKpi: PropTypes.func.isRequired,
    selectedKpi: PropTypes.object,
    step: PropTypes.object.isRequired,
  };

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
      case 'config-download':
        this.downloadFile(rowInfo.row.id, rowInfo.row.name.split('/').reverse()[0]);
        break;

      case 'kpi-view':
        this.props.selectKpi(rowInfo.row.id);
        break;

      default:
        if (handleOriginal) {
          handleOriginal();
        }
        break;
    }
  }

  downloadFile(fileId, fileName) {
    this.props.downloadFile(this.props.process.id, this.props.process.version, this.props.execution.id, fileId, fileName)
      .catch(err => {
        toast.dismiss();

        toast.error(
          <ToastTemplate iconClass='fa-cloud-download' text='Failed to download file' />
        );
      });
  }

  hideStepExecutionDetails() {
    this.props.hideStepExecutionDetails();
    this.props.resetSelectedFile();
  }

  isSelected(rowInfo) {
    return (rowInfo && this.props.selectedFile === rowInfo.row.id);
  }

  render() {
    const iconClassName = (ToolIcons[this.props.step.component] || 'fa fa-folder-open') + ' pr-2';
    const data = this.props.selectedKpi && this.props.selectedKpi.data;
    const file = this.props.selectedFile && this.props.files.find((f) => f.id === this.props.selectedFile);

    return (
      <div>
        <Card>
          <CardBody>
            <Row className="mb-4">
              <Col>
                <i className={iconClassName}></i>
                <span>{this.props.step.name + ' - Files'}</span>
              </Col>
              <Col>
                <div className="float-right">
                  <Button color="primary" onClick={this.hideStepExecutionDetails}><i className="fa fa-undo" /></Button>
                </div>
              </Col>
            </Row>
            <Row>
              <Col>
                <Table
                  name="Step Files"
                  id="step-files"
                  noDataText="No files generated by this step"
                  columns={tableColumns}
                  data={this.props.files}
                  defaultPageSize={10}
                  showPageSizeOptions={false}
                  manual
                  minRows={1}
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
        {this.props.selectedKpi &&
          <Row className="mb-4">
            <Col>
              <Card>
                <CardBody>
                  <KpiGridView
                    data={data}
                    file={file}
                  />
                </CardBody>
              </Card>
            </Col>
          </Row>
        }
      </div>
    );
  }
}
