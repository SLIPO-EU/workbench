import React from 'react';
import PropTypes from 'prop-types';
import { Input } from 'reactstrap';

import decorateField from './form-field';

/**
 * Simple text component
 *
 * @export
 * @class Text
 * @extends {React.Component}
 */
export class Text extends React.Component {

  constructor(props) {
    super(props);
  }

  static propTypes = {
    id: PropTypes.string.isRequired,
    label: PropTypes.string,
    help: PropTypes.string,
    value: PropTypes.string,
    state: PropTypes.oneOf(['success', 'warning', 'danger']),
    onChange: PropTypes.func,
    onKeyDown: PropTypes.func,
    readOnly: PropTypes.oneOfType([PropTypes.bool, PropTypes.func]),
    maxLength: PropTypes.number,
    type: PropTypes.oneOf(['text', 'number',]),
  }

  static defaultProps = {
    type: 'text',
  }

  /**
   * Returns true if the component is read-only; Otherwise false
   *
   * @readonly
   * @memberof Text
   */
  get isReadOnly() {
    if (typeof this.props.readOnly === 'function') {
      return this.props.readOnly(this.props.id);
    }
    return this.props.readOnly;
  }

  render() {
    const props = this.props;
    return (
      <Input
        type={props.type}
        name={props.id}
        id={props.id}
        state={props.state}
        value={props.value || ''}
        autoComplete="off"
        onKeyDown={e => typeof props.onKeyDown === 'function' ? props.onKeyDown(e) : null}
        onChange={e => typeof props.onChange === 'function' ? props.onChange(e.target.value) : null}
        readOnly={this.isReadOnly}
        maxLength={this.props.maxLength || ''}
      />
    );
  }
}

export default decorateField(Text);
