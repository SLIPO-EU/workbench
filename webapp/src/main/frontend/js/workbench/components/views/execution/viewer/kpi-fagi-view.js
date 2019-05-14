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
  Table,
  StackedBarChart,
} from '../../../helpers';

const EnumChartMode = {
  Configuration: 'Configuration',
  View: 'View',
};

const itemColumns = [{
  Header: '',
  id: 'selection',
  Cell: props => {
    if (props.original.selected) {
      return (
        <i
          data-action="remove-from-chart"
          title="Remove from chart"
          className='fa fa-check-square-o slipo-table-row-action'>
        </i>
      );
    } else {
      return (
        <i
          data-action="add-to-chart"
          title="Add to chart"
          className='fa fa-square-o slipo-table-row-action'>
        </i>
      );
    }
  },
  style: { 'textAlign': 'center' },
  maxWidth: 60
}, {
  Header: 'Title',
  id: 'title',
  accessor: i => i.label,
  headerStyle: { 'textAlign': 'left' },
}];

class KpiFagiView extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      items: [],
      mode: EnumChartMode.Configuration,
    };
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

  componentDidMount() {
    const { original } = this.props;
    this.updateChartItems(original);
  }

  componentDidUpdate(prevProps) {
    if ((this.props.original) && (this.props.original !== prevProps.original)) {
      this.updateChartItems(this.props.original);
    }
  }

  updateChartItems(data) {
    if (!data) {
      return;
    }

    const items = Object.keys(data)
      .reduce((result, key) => {
        const value = data[key];

        // Ignore previous versions of FAGI
        if (!value.hasOwnProperty('items')) {
          return result;
        }

        result.push({
          key,
          ...value,
          selected: false,
        });

        return result;
      }, []);

    this.setState({
      items,
    });
  }

  onItemSelected(selected, checked) {
    const { items } = this.state;

    const updatedItems = items.map(item => ({
      ...item,
      selected: item.key === selected.key ? checked : item.selected,
    }));

    this.setState({
      items: updatedItems,
    });
  }

  onModeChanged(e, mode) {
    e.preventDefault();

    this.setState({
      mode,
    });
  }

  onClearSelection(e) {
    e.preventDefault();

    const { items } = this.state;

    const updatedItems = items.map(item => ({
      ...item,
      selected: false,
    }));

    this.setState({
      items: updatedItems,
    });
  }

  getSeries() {
    const { items } = this.state;

    const selectedItems = items.filter(i => i.selected);

    switch (selectedItems.length) {
      case 0:
        return [null, null];
      default: {
        const keys = [];
        const series = [];

        selectedItems.forEach(value => {
          const result = {
            label: value.label,
          };

          value.items.map(item => {
            if (!keys.includes(item.label)) {
              keys.push(item.label);
            }
            result[item.label] = +item.value;
            //result[item.label + 'Color']= 'hsl(327, 70%, 50%)';
            //legendA: item.legendA,
          });

          series.push(result);
        });

        return [keys, series];
      }
    }
  }

  handleRowAction(rowInfo, e, handleOriginal) {
    switch (e.target.getAttribute('data-action')) {
      case 'add-to-chart':
        this.onItemSelected(rowInfo.original, true);
        break;
      case 'remove-from-chart':
        this.onItemSelected(rowInfo.original, false);
        break;
      default:
        if (handleOriginal) {
          handleOriginal();
        }
        break;
    }
  }

  render() {
    const { data, file } = this.props;
    const { items, mode } = this.state;

    if (!data) {
      return null;
    }

    const selectedItems = items.filter(i => i.selected) || [];

    const [keys, series] = mode === EnumChartMode.View ? this.getSeries() : [null, null];

    return (
      <div>
        {items.length !== 0 &&
          <Card>
            <CardBody>
              <Row className="mb-4">
                <Col>
                  <i className="fa fa-bar-chart pr-1"></i>
                  <span>{`Charts [${selectedItems.length}]`}</span>
                  {mode === EnumChartMode.Configuration &&
                    <React.Fragment>
                      <span className="p-2">Configuration</span>
                      {selectedItems.length !== 0 &&
                        <a
                          className="p-2 slipo-action-link"
                          onClick={(e) => this.onModeChanged(e, EnumChartMode.View)}
                        >View</a>
                      }
                    </React.Fragment>
                  }
                  {mode === EnumChartMode.View &&
                    <React.Fragment>
                      <a
                        className="p-2 slipo-action-link"
                        onClick={(e) => this.onModeChanged(e, EnumChartMode.Configuration)}
                      >Configuration</a>
                      <span className="p-2">View</span>
                    </React.Fragment>
                  }
                  {selectedItems.length !== 0 && mode === EnumChartMode.Configuration &&
                    <a
                      className="p-2 slipo-action-link"
                      onClick={(e) => this.onClearSelection(e)}
                    >Reset</a>
                  }
                </Col>
              </Row>
              {mode === EnumChartMode.Configuration &&
                <React.Fragment>
                  {items.length !== 0 &&
                    <Row>
                      <Col>
                        <div style={{ maxHeight: 400, overflowY: 'auto' }}>
                          <Table
                            className="mt-2"
                            name="Chart Items"
                            id="chart-items"
                            columns={itemColumns}
                            data={items}
                            defaultPageSize={items.length}
                            pageSize={items.length}
                            showPageSizeOptions={false}
                            getTdProps={(state, rowInfo, column) => ({
                              onClick: this.handleRowAction.bind(this, rowInfo)
                            })}
                            getTbodyProps={() => ({
                              style: { maxHeight: 300 }
                            })}
                            showPagination={false}
                          />
                        </div>
                      </Col>
                    </Row>
                  }
                </React.Fragment>
              }
              {mode === EnumChartMode.View &&
                <Row>
                  <Col>
                    <StackedBarChart
                      data={series}
                      keys={keys}
                      indexBy={'label'}
                      tooltip={(datum) => {
                        const { id, value } = datum;

                        return (
                          <div>
                            <div>{id}</div>
                            <div>{Math.floor(value) === value ? value : value.toFixed(2)}</div>
                          </div>
                        );
                      }}
                    />
                  </Col>
                </Row>
              }
            </CardBody>
          </Card>
        }
        <Row>
          <Col>
            <Card>
              <CardBody>
                <KpiSharedView
                  data={selectedItems.length === 0 ? data : data.filter(d => selectedItems.some(s => d.key.startsWith(s.key + '.')))}
                  file={file}
                  original={this.props.original}
                />
              </CardBody>
            </Card>
          </Col>
        </Row>
      </div>
    );
  }
}

export default KpiFagiView;
