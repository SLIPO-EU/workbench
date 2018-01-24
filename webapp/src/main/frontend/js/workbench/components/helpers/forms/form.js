import React from 'react';
import PropTypes from 'prop-types';
import { Button, Row, Col, } from 'reactstrap';

export default class Form extends React.Component {

  constructor(props) {
    super(props);

    this._setValue = this._setValue.bind(this);
  }

  static propTypes = {
    header: PropTypes.bool,
    title: PropTypes.string,
    iconClass: PropTypes.string,
    discardButtonText: PropTypes.string,
    acceptButtonText: PropTypes.string,
    validate: PropTypes.func,
    setError: PropTypes.func,
    setValue: PropTypes.func.isRequired,
    cancel: PropTypes.func,
    save: PropTypes.func,
    values: PropTypes.object,
    errors: PropTypes.object,
    readOnly: PropTypes.bool,
  }

  static defaultProps = {
    header: true,
    readOnly: true,
  }

  componentDidMount() {
    this._validate(this.props.values);
  }

  _validate(values) {
    if ((typeof this.props.validate === 'function') && (typeof this.props.setError === 'function')) {
      try {
        this.props.validate(values || {});
        this.props.setError({});
      } catch (errors) {
        this.props.setError(errors);
      }
    }
  }

  _setValue(value) {
    this.props.setValue(value);
    this._validate(value);
  }

  render() {
    const children = this.props.children;
    return (
      <div>
        {this.props.header &&
          <Row className="mb-4">
            <Col>
              <span>
                <i className={this.props.iconClass + ' mr-2'}></i>
                <span>{this.props.title}</span>
              </span>
              <div
                style={{ position: 'absolute', right: 15, top: -5 }}
              >
                {!this.props.readOnly &&
                  <div className="mr-2" style={{ float: 'left' }}>
                    <Button color="danger" onClick={this.props.cancel} className="float-left">{this.props.discardButtonText || 'Cancel'}</Button>
                  </div>
                }
                {!this.props.readOnly &&
                  <div style={{ float: 'left' }}>
                    <Button color="primary" onClick={this.props.save} className="float-left">{this.props.acceptButtonText || 'Save'}</Button>
                  </div>
                }
                {this.props.readOnly &&
                  <div style={{ float: 'left' }}>
                    <Button color="primary" onClick={this.props.cancel} className="float-left">Return</Button>
                  </div>
                }
              </div>
            </Col>
          </Row>
        }
        <Row>
          <Col>
            <div
              style={{ maxHeight: '70vh', overflowY: 'auto', paddingRight: 28, paddingBottom: 50 }}
            >
              {
                React.Children.map(children, (child, index) => {
                  return React.cloneElement(child, {
                    setValue: this._setValue,
                    value: this.props.values,
                    errors: this.props.errors,
                    readOnly: this.props.readOnly,
                  });
                })
              }
            </div>
          </Col>
        </Row>
      </div >
    );
  }

}
