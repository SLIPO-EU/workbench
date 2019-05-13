import _ from 'lodash';
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
  accessor: i => i.title,
  headerStyle: { 'textAlign': 'left' },
}];

class KpiFagiView extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      groups: [],
      groupOptions: [],
      selectedGroup: null,
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
    original: PropTypes.object.isRequired,
  }

  componentDidUpdate(prevProps) {
    if ((this.props.original) && (this.props.original !== prevProps.original)) {

      const { original } = this.props;

      const groups = Object.keys(original)
        // Group items by type
        .reduce((result, key, index) => {
          const value = original[key];
          if (!_.isObject(value)) {
            return;
          }

          let group = result.find(g => g.key === value.group.enumGroup);
          if (!group) {
            group = {
              key: value.group.enumGroup,
              title: value.group.title,
              legendA: value.group.legendA,
              legendB: value.group.legendB,
              legendTotal: value.group.legendTotal,
              items: [],
              count: 0,
            };
            result.push(group);
          }

          const { group: valueGroup, ...rest } = value;
          if ((rest['valueA'] !== undefined) && (rest['valueB'] !== undefined)) {
            group.items.push({
              key,
              selected: false,
              ...rest,
            });
          }

          return result;
        }, [])
        // Remove empty groups
        .filter(g => g.key);

      groups.forEach(g => {
        g.items.sort((i1, i2) => i1.title > i2.title ? 1 : -1);
      });

      this.setState({
        groups,
        groupOptions: groups.map(g => ({ label: g.title, value: g.key })),
      });
    }
  }

  onGroupSelected(key) {
    const { groups = [] } = this.state;

    const selectedGroup = groups.find(g => g.key === key) || null;

    this.setState({
      selectedGroup: {
        ...selectedGroup,
        count: 0,
      },
    });
  }

  onItemSelected(selected, checked) {
    const { selectedGroup } = this.state;

    const updatedGroup = {
      ...selectedGroup,
      items: selectedGroup.items.map(item => ({
        ...item,
        selected: item.key === selected.key ? checked : item.selected,
      })),
      count: selectedGroup.count + (checked ? 1 : -1),
    };

    this.setState({
      selectedGroup: updatedGroup,
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

    const { selectedGroup } = this.state;

    const updatedGroup = {
      ...selectedGroup,
      items: selectedGroup.items.map(item => ({
        ...item,
        selected: false,
      })),
      count: 0,
    };

    this.setState({
      selectedGroup: updatedGroup,
    });
  }

  getSeries() {
    const { selectedGroup } = this.state;

    const selectedItems = selectedGroup.items.filter(i => i.selected);

    switch (selectedItems.length) {
      case 0:
        return null;
      default:
        return selectedItems.map(item => ({
          title: item.title,
          valueA: +item.valueA,
          valueAColor: 'hsl(327, 70%, 50%)',
          legendA: item.legendA,
          valueB: +item.valueB,
          valueBColor: 'hsl(236, 70%, 50%)',
          legendB: item.legendB,
        }));
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
    const { groups, groupOptions, selectedGroup, mode } = this.state;

    if (!data) {
      return null;
    }

    const selectedGroupOption = selectedGroup ? groupOptions.find(opt => opt.value === selectedGroup.key) : null;
    const selectedItems = selectedGroup ? selectedGroup.items.filter(i => i.selected) : [];

    const series = mode === EnumChartMode.View ? this.getSeries() : null;

    return (
      <div>
        {groups.length !== 0 &&
          <Card>
            <CardBody>
              <Row className="mb-4">
                <Col>
                  <i className="fa fa-bar-chart pr-1"></i>
                  <span>{`Charts [${selectedGroup ? selectedGroup.count : 0}]`}</span>
                  {mode === EnumChartMode.Configuration &&
                    <React.Fragment>
                      <span className="p-2">Configuration</span>
                      {selectedGroup && selectedGroup.count !== 0 &&
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
                  {selectedGroup && mode === EnumChartMode.Configuration &&
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
                        placeholder="Select group ..."
                      />
                    </Col>
                  </Row>
                  {selectedGroup &&
                    <Row>
                      <Col>
                        <div style={{ maxHeight: 400, overflowY: 'auto' }}>
                          <Table
                            className="mt-2"
                            name="Chart Items"
                            id="chart-items"
                            columns={itemColumns}
                            data={selectedGroup.items}
                            defaultPageSize={selectedGroup.items.length}
                            pageSize={selectedGroup.items.length}
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
                      keys={[
                        'valueA',
                        'valueB',
                      ]}
                      indexBy={'title'}
                      tooltip={(datum) => {
                        const { id, data } = datum;
                        const value = id === 'valueA' ? data.valueA : data.valueB;

                        return (
                          <div>
                            <div>{id === 'valueA' ? data.legendA : data.legendB}</div>
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
