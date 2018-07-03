import * as React from 'react';
import PropTypes from 'prop-types';

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
        <table className="slipo-pd-error-list">
          <tbody>
            {this.props.errors.map((e, index) => {
              return (
                <tr key={index} className="slipo-pd-error">
                  <td className="slipo-pd-error-item slipo-pd-error-icon error"><li className="fa fa-warning" /></td>
                  <td className="slipo-pd-error-item slipo-pd-error-text">{e.text}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    );
  }

}

export default ErrorList;
