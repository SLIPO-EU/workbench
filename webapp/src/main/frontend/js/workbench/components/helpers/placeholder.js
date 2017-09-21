import * as React from 'react';
import * as PropTypes from 'prop-types';
import * as ReactRedux from 'react-redux';

/**
 * A simple placeholder
 *
 * @class Placeholder
 * @extends {React.Component}
 */
class Placeholder extends React.Component {

  constructor(props) {
    super(props);
  }

  render() {
    return (
      <h1>{this.props.text || 'This is a placeholder'}</h1>
    );
  }
}

Placeholder.propTypes = {
  text: PropTypes.string,
};


export default Placeholder;
