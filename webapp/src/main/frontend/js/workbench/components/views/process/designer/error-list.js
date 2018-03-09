import * as React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';

class ErrorList extends React.Component {

  constructor(props) {
    super(props);

  }

  static propTypes = {
    errors: PropTypes.arrayOf(PropTypes.shape({
      text: PropTypes.string.isRequired,
    })).isRequired,
  };

  render() {
    return (
      <div className="slipo-pd-error-list-wrapper">
        <ol className="slipo-pd-error-list">
          {this.props.errors.map((e, index) => {
            return (
              <li key={index} className="slipo-pd-error-item">{e.text}</li>
            );
          })}
        </ol>
      </div>
    );
  }

}

export default ErrorList;
