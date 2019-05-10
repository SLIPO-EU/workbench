import React from 'react';
import PropTypes from 'prop-types';

import decorateField from './form-field';
import GeometryType from 'ol/geom/GeometryType';
import GeometryEditor from '../../geometry-editor';

/**
 * Simple geometry editor
 *
 * @export
 * @class Geometry
 * @extends {React.Component}
 */
export class Geometry extends React.Component {

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
    readOnly: PropTypes.oneOfType([PropTypes.bool, PropTypes.func]),
    maxLength: PropTypes.number,
    type: PropTypes.oneOf([GeometryType.POINT, GeometryType.LINE_STRING, GeometryType.POLYGON]),
  }

  static defaultProps = {
    type: GeometryType.POINT,
    maxLength: 5000,
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
      <GeometryEditor
        config={props.config}
        type={props.type}
        name={props.id}
        id={props.id}
        state={props.state}
        value={props.value || ''}
        onChange={wkt => this.props.onChange(wkt)}
        readOnly={this.isReadOnly}
        maxLength={this.props.maxLength}
      />
    );
  }
}

export default decorateField(Geometry);
