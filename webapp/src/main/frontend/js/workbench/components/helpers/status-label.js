import * as React from 'react';
import * as PropTypes from 'prop-types';
import * as ReactRedux from 'react-redux';

/**
 * A presentational component for displaying labels
 *
 * @class StatusLabel
 * @extends {React.Component}
 */
class StatusLabel extends React.Component {

  constructor(props) {
    super(props);
  }

  static propTypes = {
    mappings: PropTypes.arrayOf(PropTypes.shape({
      value: PropTypes.string.isRequired,
      className: PropTypes.string.isRequired,
    })).isRequired,
    value: function (props, propName, componentName) {
      if (!props['mappings'].find((m) => m.value === props[propName])) {
        return new Error(`Mapping for value ${props[propName]} was not found.`);
      }
    },
    label: PropTypes.string,
  };

  mapValueToClass() {
    const mapping = this.props.mappings.find((m) => m.value === this.props.value);
    return (mapping ? mapping.className : '');
  }

  render() {
    return (
      <div className={this.mapValueToClass()}>{this.props.label || this.props.value}</div>
    );
  }
}

export default StatusLabel;
