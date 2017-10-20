import * as React from 'react';
import * as PropTypes from 'prop-types';

class Placeholder extends React.Component {

  constructor() {
    super();
  }

  render() {
    return (
      <div className="slipo-placeholder-container">
        <div className="slipo-placeholder-label">
          <div><i className={this.props.iconClass + ' mr-2'}></i><span>{this.props.label}</span></div>
        </div>
      </div>
    );
  }
}

Placeholder.propTypes = {
  label: PropTypes.string,
  iconClass: PropTypes.string.isRequired,
};

export default Placeholder;
