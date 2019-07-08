import * as React from 'react';
import PropTypes from 'prop-types';

import ReactSelect from 'react-select';

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
  SelectDefaultStyle,
  StackedBarChart,
  Table,
} from '../../../helpers';

const EnumGroup = {
  PERCENT: 'PERCENT',
  ABSOLUTE: 'ABSOLUTE',
};

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
      groupOptions: [],
      items: [],
      mode: EnumChartMode.Configuration,
      selectedGroup: null,
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
    const groups = [];

    const items = Object.keys(data)
      .reduce((result, key) => {
        const value = data[key];

        // Ignore previous versions of FAGI
        if (!Object.prototype.hasOwnProperty.call(value, 'items')) {
          return result;
        }

        result.push({
          key,
          ...value,
          selected: false,
        });

        if (!groups.includes(value.type)) {
          groups.push(value.type);
        }

        return result;
      }, []);

    this.setState({
      groupOptions: groups.sort().map(g => ({ label: g, value: g })),
      items: items.sort((i1, i2) => i1.label > i2.label ? 1 : -1),
      mode: EnumChartMode.Configuration,
    });
  }

  onGroupSelected(selectedGroup) {
    this.setState({
      selectedGroup,
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
    const { items, selectedGroup } = this.state;

    const selectedItems = items.filter(i => i.selected && i.type === selectedGroup);

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
    const { items, groupOptions, selectedGroup, mode } = this.state;

    if (!data) {
      return null;
    }

    const selectedGroupOption = selectedGroup ? groupOptions.find(opt => opt.value === selectedGroup) : null;
    const visibleItems = selectedGroup ? items.filter(i => i.type === selectedGroup) : [];

    const selectedItems = visibleItems.filter(i => i.selected) || [];

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
                  <Row>
                    <Col>
                      <ReactSelect
                        name="group"
                        id="group"
                        value={selectedGroupOption || ''}
                        onChange={(option) => this.onGroupSelected(option ? option.value : '')}
                        options={groupOptions}
                        styles={SelectDefaultStyle}
                        isClearable={true}
                        placeholder="Select value type ..."
                      />
                    </Col>
                  </Row>
                  {items.length !== 0 &&
                    <Row>
                      <Col>
                        <div style={{ maxHeight: 400, overflowY: 'auto' }}>
                          <Table
                            className="mt-2"
                            name="Chart Items"
                            id="chart-items"
                            columns={itemColumns}
                            data={visibleItems}
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
                      indexBy={'label'}
                      keys={keys}
                      maxValue={selectedGroup === EnumGroup.PERCENT ? 1 : 'auto'}
                      tooltip={(datum) => {
                        const { id, value } = datum;
                        const computedValue = Math.floor(value) === value ? value : value.toFixed(2);

                        return (
                          <div>
                            <div>{id}</div>
                            <div>{selectedGroup === EnumGroup.PERCENT ? `${computedValue * 100} %` : computedValue}</div>
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
