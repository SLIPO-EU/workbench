import _ from 'lodash';
import * as React from 'react';

import {
  Button, Modal, ModalHeader, ModalBody, ModalFooter, Row, Col,
} from 'reactstrap';

import {
  FilterRow,
} from './';

const MAX_FILTERS = 5;

const defaultFilter = {
  layer: null,
  attribute: null,
  type: 'null',
  value: '',
};

class FilterForm extends React.Component {

  constructor(props) {
    super(props);

    const { filters: allFilters = [], layers } = props;

    const activeFilters = allFilters.filter(f => layers.find(l => (!f.layer) || (l.tableName === f.layer && !l.hidden)));

    if (activeFilters.length === 0 && allFilters.length < MAX_FILTERS) {
      const index = this.getBaseIndex(allFilters);
      this.state = {
        filters: [{ ...defaultFilter, index }],
        countOffset: allFilters.length,
      };
    } else {
      this.state = {
        filters: _.cloneDeep(activeFilters),
        countOffset: allFilters.length - activeFilters.length,
      };
    }
  }

  static defaultProps = {
    filters: [],
  }

  getBaseIndex(filters) {
    return filters.reduce((result, filter) => {
      return (filter.index >= result) ? filter.index + 1 : result;
    }, 0);
  }

  componentWillReceiveProps(nextProps) {
    const { filters: allFilters = [], layers } = nextProps;

    const activeFilters = allFilters.filter(f => layers.find(l => (!f.layer) || (l.tableName === f.layer && !l.hidden)));

    if (activeFilters.length === 0 && allFilters.length < MAX_FILTERS) {
      const index = this.getBaseIndex(allFilters);
      this.setState({
        filters: [{ ...defaultFilter, index }],
        countOffset: allFilters.length,
      });
    } else {
      this.setState({
        filters: _.cloneDeep(activeFilters),
        countOffset: allFilters.length - activeFilters.length,
      });
    }
  }

  search() {
    const { filters: allFilters = [], layers } = this.props;

    const inactiveFilters = allFilters.filter(f => layers.find(l => l.tableName === f.layer && l.hidden));

    this.props.search([...inactiveFilters, ...this.state.filters]);
  }

  clear() {
    this.props.search([]);
  }

  onFilterChange(filter) {
    this.setState({
      filters: this.state.filters.map(f => {
        if (f.index === filter.index) {
          return { ...filter };
        }
        return f;
      }),
    });
  }

  addFilter() {
    const { filters, countOffset } = this.state;

    if (filters.length + countOffset < MAX_FILTERS) {
      const index = this.getBaseIndex(filters);

      this.setState({
        filters: [...filters, { ...defaultFilter, index, }],
      });
    }
  }

  removeFilter(index) {
    const { filters } = this.state;

    if (filters.length > 1) {
      this.setState({
        filters: filters.filter(f => f.index !== index),
      });
    }
  }

  get isValid() {
    const { filters } = this.state;

    for (const filter of filters) {
      if (!filter.layer) {
        return false;
      }
      if (!filter.attribute) {
        return false;
      }
      if (!filter.type) {
        return false;
      }
      if ((filter.type !== 'null') && (filter.type !== 'notNull') && (!filter.value)) {
        return false;
      }
    }
    return true;
  }

  render() {
    const { filters, countOffset } = this.state;
    const { layers, visible } = this.props;
    const activeFilters = filters.filter(f => layers.find(l => (!f.layer) || (l.tableName === f.layer && !l.hidden)));

    return (
      <Modal
        centered={true}
        isOpen={visible}
        toggle={() => this.props.toggle()}
      >
        <ModalHeader toggle={() => this.props.toggle()}>Filter Data</ModalHeader>
        <ModalBody>
          <div style={{ minWidth: '750px' }}>
            <Row className="pb-2">
              <Col style={{ flex: '0 0 40px' }}>{' '}</Col>
              <Col>Layer</Col>
              <Col>Attribute</Col>
              <Col>Type</Col>
              <Col>Value</Col>
            </Row>

            {activeFilters.map((f, index) => (
              <FilterRow
                key={f.index}
                filter={f}
                layers={layers}
                onChange={(filter) => this.onFilterChange(filter)}
                allowAdd={(index + 1) === activeFilters.length && activeFilters.length + countOffset < MAX_FILTERS}
                allowDelete={activeFilters.length > 1}
                add={() => this.addFilter()}
                remove={(index) => this.removeFilter(index)}
              />
            ))}

          </div>
        </ModalBody>
        <ModalFooter>
          <Button color="primary" onClick={() => this.search()} disabled={!this.isValid}>Search</Button>
          <Button color="secondary" onClick={() => this.clear()}>Clear</Button>
        </ModalFooter>
      </Modal >
    );
  }

}

export default FilterForm;
