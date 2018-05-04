import React from 'react';
import PropTypes from 'prop-types';
import { Input } from 'reactstrap';

import decorateField from './form-field';

export class ValuePairList extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      prefix: '',
      namespace: '',
      status: '',
    };
  }

  static propTypes = {
    id: PropTypes.string.isRequired,
    label: PropTypes.string,
    help: PropTypes.string,
    value: PropTypes.arrayOf(PropTypes.shape({
      prefix: PropTypes.string.isRequired,
      namespace: PropTypes.string.isRequired,
    })).isRequired,
    onChange: PropTypes.func,
    readOnly: PropTypes.oneOfType([PropTypes.bool, PropTypes.func]),
  }

  static defaultProps = {
    value: [],
  }

  isReadOnly() {
    if (typeof this.props.readOnly === 'function') {
      return this.props.readOnly(this.props.id);
    }
    return this.props.readOnly;
  }

  removeItem(index) {
    const newValue = [...this.props.value];
    newValue.splice(index, 1);

    this.props.onChange(newValue);
  }

  addItem() {
    if ((!this.state.prefix) || (!this.state.namespace)) {
      this.setState({
        status: 'danger',
      });
    } else {
      let newValue = [];
      const existing = this.props.value.find(item => item.prefix === this.state.prefix);
      if (existing) {
        existing.namespace = this.state.namespace;
        newValue = [...this.props.value];
      } else {
        newValue = [...this.props.value, {
          prefix: this.state.prefix,
          namespace: this.state.namespace,
        }];
      }

      this.setState({
        prefix: '',
        namespace: '',
        status: '',
      });
      this.props.onChange(newValue);
    }
  }

  editItem(index) {
    this.setState({
      prefix: this.props.value[index].prefix,
      namespace: this.props.value[index].namespace,
      status: '',
    });
  }

  renderItem(value, index) {
    return (
      <div key={index} className="row">
        <div className="col-3" style={{ padding: '0.5rem 1rem' }}>
          <div className="alert-info" style={{ borderRadius: 4, padding: 6 }}>
            {value.prefix}
          </div>
        </div>
        <div className="col-5" style={{ padding: '0.5rem 1rem' }}>
          <div className="alert-info" style={{ borderRadius: 4, padding: 6 }}>
            {value.namespace}
          </div>
        </div>
        {!this.isReadOnly() &&
          <div className="col" style={{ paddingTop: 2, fontSize: '1.2rem' }}>
            <i className="fa fa-trash mr-2" style={{ cursor: 'pointer' }} onClick={(e) => this.removeItem(index)} />
            <i className="fa fa-pencil" style={{ cursor: 'pointer' }} onClick={(e) => this.editItem(index)} />
          </div>
        }
      </div>
    );
  }

  render() {
    const props = this.props;

    return (
      <div id={props.id}>
        <div>
          {this.props.value &&
            this.props.value.map((value, index) => this.renderItem(value, index))
          }
        </div>
        {!this.isReadOnly() &&
          <div className="row">
            <div className="col-3">
              <Input
                type="text"
                name={`${props.id}-prefix`}
                id={`${props.id}-prefix`}
                state={this.state.status}
                value={this.state.prefix || ''}
                onChange={e => this.setState({ prefix: e.target.value })}
                autoComplete="off"
                readOnly={this.isReadOnly()}
              />
            </div>
            <div className="col-5">
              <Input
                type="text"
                name={`${props.id}-namespace`}
                id={`${props.id}-namespace`}
                state={this.state.status}
                value={this.state.namespace || ''}
                onChange={e => this.setState({ namespace: e.target.value })}
                autoComplete="off"
                readOnly={this.isReadOnly()}
              />
            </div>
            <div className="col" style={{ paddingTop: 2, fontSize: '1.2rem' }}>
              <i className="fa fa-plus" style={{ cursor: 'pointer' }} onClick={(e) => this.addItem()} />
            </div>
          </div>
        }
      </div>
    );
  }
}
export default decorateField(ValuePairList);
