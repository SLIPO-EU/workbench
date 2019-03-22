import _ from 'lodash';
import React from 'react';
import PropTypes from 'prop-types';

import {
  Row, Col
} from 'reactstrap';

import ReactSelect from 'react-select';

import {
  SelectDefaultStyle,
} from '../../../../helpers';

import {
  langs,
} from '../../../../../util/i18n';

const languageOptions = _.orderBy(langs.map(l => ({ value: l.alpha2, label: l.English })), ['label'], ['asc']);

const groupStyles = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
};

const groupBadgeStyles = {
  backgroundColor: '#EBECF0',
  borderRadius: '2em',
  color: '#172B4D',
  display: 'inline-block',
  fontSize: 12,
  fontWeight: 'normal',
  lineHeight: '1',
  minWidth: 1,
  padding: '0.17em 0.5em',
  textAlign: 'center',
};

const formatGroupLabel = data => (
  <div style={groupStyles}>
    <span>{data.label}</span>
    <span style={groupBadgeStyles}>{data.options.length}</span>
  </div>
);

class TripleGeoMapping extends React.Component {

  constructor(props) {
    super(props);

  }

  static propTypes = {
    field: PropTypes.string.isRequired,
    predicate: PropTypes.string,
    predicates: PropTypes.arrayOf(PropTypes.object).isRequired,
    type: PropTypes.string,
    types: PropTypes.object.isRequired,
    language: PropTypes.string,
    onChange: PropTypes.func.isRequired,
    readOnly: PropTypes.bool,
  }

  static defaultProps = {
    predicate: '',
    type: '',
    language: '',
    readOnly: false,
  }

  toggle(field, predicate, expanded) {
    this.props.onChange({
      field,
      predicate,
      type: '',
      language: '',
      expanded,
    });
  }

  render() {
    const { expanded, field, predicate, predicates, type, types, language, readOnly } = this.props;

    const allPredicates = predicates.reduce((result, group) => {
      return [...result, ...group.options];
    }, []);

    const selectedPredicate = allPredicates.find(opt => opt.value === predicate) || null;

    const predicateOptions = predicates.map((group, index) => {
      switch (index) {
        case 0:
          // ML Predicates
          return {
            label: group.label,
            options: group.options.map(p => ({
              ...p,
              label: (
                <div className="clearfix">
                  <div className="float-left">{p.value}</div>
                  <div className={`predicate-score float-right font-xs ${p.score > 0.8 ? 'text-success' : 'text-danger'}`}>
                    {(p.score * 100).toFixed(2)} %
                </div>
                </div>
              ),
            })),
          };
        default:
          // Custom groups
          return group;
      }
    });

    const typeOptions = types[predicate] || [];

    return (
      <Row className="mb-1">
        <Col
          className="text-muted"
          style={{ maxWidth: 100, textOverflow: 'ellipsis', overflow: 'hidden', padding: '7px 0px 7px 12px' }}
        >
          {field}
        </Col>
        <Col>
          <ReactSelect
            name="predicate"
            id="predicate"
            value={selectedPredicate || ''}
            onChange={(option) => this.props.onChange({
              field,
              predicate: option ? option.value : '',
              type: '',
              language: '',
              expanded: false,
            })}
            options={predicateOptions}
            styles={SelectDefaultStyle}
            isClearable={true}
            placeholder="Select predicate ..."
            classNamePrefix="predicate"
            isDisabled={readOnly}
            formatGroupLabel={formatGroupLabel}
          />
        </Col>
        {(readOnly || !selectedPredicate || selectedPredicate.custom) &&
          <Col style={{ flex: '0 0 80px', paddingTop: 6 }}>
          </Col>
        }
        {!readOnly && selectedPredicate && !selectedPredicate.custom &&
          <Col style={{ flex: '0 0 80px', paddingTop: 6 }}>
            {expanded ?
              <span className="font-xs slipo-action-icon" onClick={() => this.toggle(field, predicate, false)}>Hide</span> :
              <span className="font-xs slipo-action-icon" onClick={() => this.toggle(field, predicate, true)}>More ...</span>
            }
          </Col>
        }
        <Col>
          {expanded && typeOptions.length !== 0 &&
            <ReactSelect
              name="type"
              id="type"
              value={typeOptions.find(opt => opt.value === type) || ''}
              onChange={(option) => this.props.onChange({
                field,
                predicate,
                type: option ? option.value : '',
                language,
                expanded,
              })}
              options={typeOptions}
              styles={SelectDefaultStyle}
              isClearable={true}
              placeholder="Select type ..."
              isDisabled={readOnly}
            />
          }
        </Col>
        <Col>
          {expanded &&
            <ReactSelect
              name="language"
              id="language"
              value={languageOptions.find(opt => opt.value === language) || ''}
              onChange={(option) => this.props.onChange({
                field,
                predicate,
                type,
                language: option ? option.value : '',
                expanded,
              })}
              options={languageOptions}
              styles={SelectDefaultStyle}
              isClearable={true}
              placeholder="Select language ..."
              isDisabled={readOnly}
            />
          }
        </Col>
      </Row>
    );
  }

}

export default TripleGeoMapping;
