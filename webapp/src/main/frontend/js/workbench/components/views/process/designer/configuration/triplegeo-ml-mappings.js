import React from 'react';
import PropTypes from 'prop-types';

import CodeMirror from 'react-codemirror';
import 'codemirror/mode/yaml/yaml';

import {
  Button, Modal, ModalHeader, ModalBody, ModalFooter, Row, Col,
} from 'reactstrap';

import {
  message,
} from '../../../../../service';

import {
  getMappings,
} from '../../../../../service/toolkit/triplegeo';

import {
  predicateTypes,
  surrogatePredicates,
} from '../../../../../model/process-designer/configuration/triplegeo';

import TripleGeoMapping from './triplegeo-mapping';

const createPredicateGroups = (predicates) => {
  return [{
    label: 'Predicates',
    options: predicates.map(p => ({
      label: p.predicate,
      value: p.predicate,
      score: p.score,
      custom: false,
    })),
  }, {
    label: 'Custom Mappings',
    options: [{
      label: 'Geometry',
      value: surrogatePredicates.WKT,
      custom: true,
    }],
  }];
};

const createRows = (mappings = {}, selection = []) => {
  const rows = [];

  if (!mappings) {
    return rows;
  }

  Object.keys(mappings).map(field => {
    const value = selection ? selection.find(m => m.field === field) || {} : {};

    const { predicate = '', type = '', language = '', expanded = false } = value;

    const row = {
      field,
      predicate,
      predicates: createPredicateGroups(mappings[field]),
      type,
      types: predicateTypes,
      language,
      expanded,
    };

    rows.push(row);
  });

  return rows;
};

class TripleGeoMLMappings extends React.Component {

  constructor(props) {
    super(props);

    const { configuration: { autoMappings: mappings, userMappings: selection, mappingSpecText } } = props;

    this.state = {
      mappings: mappings ? { ...mappings, } : null,
      selection: selection ? [...selection] : [],
      mappingSpecText,
      yaml: mappingSpecText || null,
    };

    this.updateYaml = this.updateYaml.bind(this);
  }

  static propTypes = {
    className: PropTypes.string,
    configuration: PropTypes.object.isRequired,
    hide: PropTypes.func.isRequired,
    path: PropTypes.string.isRequired,
    setValue: PropTypes.func.isRequired,
    visible: PropTypes.bool,
  }

  static defaultProps = {
    visible: false,
    className: '',
  }

  componentDidMount() {
    const { mappings } = this.state;

    if (!mappings) {
      this.refreshMappings(false);
    }
  }

  refreshMappings(resetSelection = true) {
    const { path } = this.props;
    const { selection, mappingSpecText, yaml } = this.state;

    getMappings(path)
      .then(mappings => {
        this.setState({
          mappings,
          selection: resetSelection ?
            Object.keys(mappings).map(field => {
              return {
                field,
                predicate: mappings[field][0].predicate,
                type: '',
                language: '',
                expanded: false,
              };
            }) :
            Object.keys(mappings).map(field => {
              const selected = selection.find(s => s.field === field) || null;

              return {
                field,
                predicate: selected ? selected.predicate : '',
                type: selected ? selected.type : '',
                language: selected ? selected.language : '',
                expanded: selected && (selected.type || selected.language),
              };
            }),
          mappingSpecText: resetSelection ? null : mappingSpecText,
          yaml: resetSelection ? null : yaml,
        });
      })
      .catch((err) => {
        message.error('Failed to compute ML mappings', 'fa-exclamation  ');
      });
  }

  saveMappings() {
    const { configuration, setValue } = this.props;
    const { mappings, selection } = this.state;

    setValue({
      ...configuration,
      mappings: { ...mappings },
      userMappings: selection.map(s => {
        // Remove custom properties required only by the UI
        const { expanded, ...rest } = s;
        return { ...rest };
      }),
      mappingSpecText: null,
    });

    this.props.hide();
  }

  saveMappingsText() {
    const { configuration, setValue } = this.props;
    const { mappings, mappingSpecText } = this.state;

    setValue({
      ...configuration,
      mappings: { ...mappings },
      mappingSpecText,
    });

    this.props.hide();
  }

  cancel() {
    this.props.hide();
  }

  onChange(data) {
    const { selection } = this.state;

    this.setState({
      selection: [...selection.filter(m => m.field !== data.field), {
        field: data.field,
        predicate: data.predicate,
        type: data.type,
        language: data.language,
        expanded: data.expanded,
      }]
    });
  }

  showYamlFile() {
    const { selection = [] } = this.state;
    this.props.getTripleGeoMappingFileAsText(selection)
      .then(yaml => {
        this.setState({
          yaml,
        });
      });
  }

  hideYamlFile() {
    this.setState({
      yaml: null,
    });
  }

  editYamlFile() {
    const { yaml } = this.state;

    this.setState({
      mappingSpecText: yaml,
    });
  }

  updateYaml(text) {
    this.setState({
      yaml: text,
      mappingSpecText: text,
    });
  }

  render() {
    const { errors, readOnly } = this.props;
    const { mappings, selection, mappingSpecText, yaml } = this.state;
    const rows = createRows(mappings, selection);

    return (
      <Modal
        centered={true}
        isOpen={this.props.visible}
        toggle={() => this.props.hide()}
        className={this.props.className}
      >
        <ModalHeader toggle={() => this.props.hide()}>
          ML Mappings Configuration
        </ModalHeader>
        <ModalBody style={{ minWidth: 960 }}>
          {yaml &&
            <CodeMirror
              value={mappingSpecText ? mappingSpecText : yaml}
              options={{
                mode: 'yaml',
                lineNumbers: true,
                readOnly: readOnly || !mappingSpecText,
              }}
              onChange={this.updateYaml}
            />
          }
          {!yaml &&
            <React.Fragment>
              <Row style={{ fontWeight: 500 }}>
                <Col style={{ maxWidth: 100, paddingLeft: 12, marginTop: -8 }}>
                  Original
                  <br />
                  Attribute
                </Col>
                <Col>
                  Predicate
                </Col>
                <Col style={{ flex: '0 0 80px' }}>
                </Col>
                <Col>
                  Type
                </Col>
                <Col>
                  Language
                </Col>
              </Row>

              {rows.map(row => {
                return (
                  <TripleGeoMapping
                    key={row.field}
                    expanded={row.expanded}
                    field={row.field}
                    predicate={row.predicate}
                    predicates={row.predicates}
                    type={row.type}
                    types={row.types}
                    language={row.language}
                    onChange={(data) => this.onChange(data)}
                    readOnly={readOnly}
                  />
                );
              })}
            </React.Fragment>
          }
        </ModalBody>
        {errors && errors.length !== 0 &&
          <ModalFooter style={{ justifyContent: 'flex-start', paddingLeft: 0 }}>
            <ul style={{ marginLeft: -28 }}>
              {errors.map(err => (
                <li key={err.key} className="list-unstyled text-danger">{err.text}</li>
              ))}
            </ul>
          </ModalFooter>
        }
        <ModalFooter>
          {yaml &&
            <React.Fragment>
              {!readOnly && !mappingSpecText &&
                <Button color="secondary" onClick={() => this.editYamlFile()}>Edit</Button>
              }
              {!readOnly && mappingSpecText &&
                <React.Fragment>
                  <Button color="secondary" onClick={() => this.refreshMappings(true)} title="Reset mappings">Reset</Button>
                  <Button color="primary" onClick={() => this.saveMappingsText()}>Apply</Button>
                  <Button color="danger" onClick={() => this.cancel()} className="float-left">Cancel</Button>
                </React.Fragment>
              }
              {!mappingSpecText &&
                <Button color="secondary" onClick={() => this.hideYamlFile()}>Back</Button>
              }
            </React.Fragment>
          }
          {!yaml &&
            <React.Fragment>
              {rows.length !== 0 &&
                <Button color="secondary" onClick={() => this.showYamlFile()}>View mapping file</Button>
              }
              {!readOnly &&
                <React.Fragment>
                  <Button color="secondary" onClick={() => this.refreshMappings(true)} title="Reset mappings">Reset</Button>
                  <Button color="primary" onClick={() => this.saveMappings()}>Apply</Button>
                  <Button color="danger" onClick={() => this.cancel()} className="float-left">Cancel</Button>
                </React.Fragment>
              }
              {readOnly &&
                <Button color="secondary" onClick={() => this.cancel()} className="float-left">Back</Button>
              }
            </React.Fragment>
          }
        </ModalFooter>
      </Modal>
    );
  }
}

export default TripleGeoMLMappings;
