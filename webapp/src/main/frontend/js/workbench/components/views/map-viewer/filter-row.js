import _ from 'lodash';
import React from 'react';
import PropTypes from 'prop-types';

import ReactSelect from 'react-select';

import {
  FormGroup, Input, Row, Col
} from 'reactstrap';

const selectStyle = {
  control: (base) => {
    return {
      ...base,
      borderRadius: '0px',
      borderColor: '#cccccc',
      ':focus': {
        borderColor: '#8ad4ee',
      },
      boxShadow: 'none',
    };
  },
  container: (base) => ({
    ...base,
  }),
  menu: (base) => ({
    ...base,
    borderRadius: '0px',
    boxShadow: 'none',
    marginTop: '-1px',
    border: '1px solid #cccccc',
  }),
};

const selectInvalidStyle = {
  control: (base) => {
    return {
      ...base,
      borderRadius: '0px',
      borderColor: 'red',
      ':focus': {
        borderColor: 'red',
      },
      ':hover': {
        borderColor: 'red',
      },
      boxShadow: 'none',
    };
  },
  container: (base) => ({
    ...base,
  }),
  menu: (base) => ({
    ...base,
    borderRadius: '0px',
    boxShadow: 'none',
    marginTop: '-1px',
    border: '1px solid #cccccc',
  }),
};

// TODO : Replace with attributes from SLIPO ontology
const attributes = _.orderBy([{
  label: 'Country',
  value: 'country',
}, {
  label: 'Description',
  value: 'description',
}, {
  label: 'Email',
  value: 'email',
}, {
  label: 'Fax',
  value: 'fax',
}, {
  label: 'Name',
  value: 'name',
}, {
  label: 'Phone',
  value: 'phone',
}, {
  label: 'Postcode',
  value: 'postcode',
}, {
  label: 'Type',
  value: 'type',
}, {
  label: 'Image',
  value: 'image',
}], ['label'], ['asc']);

const types = [{
  label: 'Empty',
  value: 'null'
}, {
  label: 'Not Empty',
  value: 'notNull'
}, {
  label: 'Starts With',
  value: 'startsWith'
}, {
  label: 'Ends With',
  value: 'endsWith'
}, {
  label: 'Contains',
  value: 'contains'
}, {
  label: '=',
  value: 'equal'
}, {
  label: '<',
  value: 'less'
}, {
  label: '<=',
  value: 'lessOrEqual'
}, {
  label: '>',
  value: 'greater'
}, {
  label: '>=',
  value: 'greaterOrEqual'
}];

class FilterRow extends React.Component {

  constructor(props) {
    super();

    this.layerOptions = props.layers
      .filter(l => !l.hidden)
      .map(l => ({
        label: l.title,
        value: l.tableName,
      }));
  }

  static propTypes = {
    allowAdd: PropTypes.bool.isRequired,
    allowDelete: PropTypes.bool.isRequired,
    filter: PropTypes.object.isRequired,
    layers: PropTypes.arrayOf(PropTypes.object).isRequired,
    onChange: PropTypes.func.isRequired,
  }

  render() {
    const { allowAdd, allowDelete, filter } = this.props;

    return (
      <Row>
        <Col style={{ flex: '0 0 40px', padding: '7px 0px 7px 12px' }}>
          {allowDelete &&
            <i className="fa fa-trash pr-1" style={{ cursor: 'pointer' }} onClick={() => this.props.remove(filter.index)}></i>
          }
          {allowAdd &&
            <i className="fa fa-plus" style={{ cursor: 'pointer' }} onClick={() => this.props.add()}></i>
          }
        </Col>
        <Col>
          <FormGroup>
            <ReactSelect
              name="layer"
              id="layer"
              value={this.layerOptions.find(opt => opt.value === filter.layer) || null}
              onChange={(option) => this.props.onChange({ ...filter, layer: option.value })}
              options={this.layerOptions}
              styles={filter.layer === null ? selectInvalidStyle : selectStyle}
            />
          </FormGroup>
        </Col>
        <Col>
          <FormGroup>
            <ReactSelect
              name="attribute"
              id="attribute"
              value={attributes.find(opt => opt.value === filter.attribute) || null}
              onChange={(option) => this.props.onChange({ ...filter, attribute: option.value })}
              options={attributes}
              styles={filter.attribute === null ? selectInvalidStyle : selectStyle}
            />
          </FormGroup>
        </Col>
        <Col>
          <FormGroup>
            <ReactSelect
              name="type"
              id="type"
              value={types.find(opt => opt.value === filter.type) || null}
              onChange={(option) => this.props.onChange({
                ...filter,
                type: option.value,
                value: option.value === 'null' || option.value === 'notNull' ? '' : filter.value
              })}
              options={types}
              styles={filter.type === null ? selectInvalidStyle : selectStyle}
            />
          </FormGroup>
        </Col>
        <Col>
          <FormGroup>
            <Input
              type="text"
              maxLength="50"
              step={1}
              name="value"
              id="value"
              value={filter.value}
              onChange={(e) => this.props.onChange({ ...filter, value: e.target.value })}
              disabled={filter.type === 'null' || filter.type === 'notNull'}
              invalid={filter.type !== 'null' && filter.type !== 'notNull' && !filter.value}
            />
          </FormGroup>
        </Col>
      </Row>
    );
  }

}

export default FilterRow;
