import React from 'react';
import PropTypes from 'prop-types';
import { Input } from 'reactstrap';

import decorateField from './form-field';

/**
 * A text area component
 *
 * @export
 * @class TextArea
 * @extends {React.Component}
 */
export class TextArea extends React.Component {

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
    rows: PropTypes.number,
    readOnly: PropTypes.oneOfType([PropTypes.bool, PropTypes.func]),
  }

  /**
   * Returns true if the component is read-only; Otherwise false
   *
   * @readonly
   * @memberof TextArea
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
        type="textarea"
        name={props.id}
        id={props.id}
        state={props.state}
        value={props.value || ''}
        onChange={e => typeof props.onChange === 'function' ? props.onChange(e.target.value) : null}
        readOnly={this.isReadOnly}
        rows={props.rows || 1}
      />
    );
  }
}
export default decorateField(TextArea);
