import * as React from 'react';
import * as PropTypes from 'prop-types';

import { FormattedMessage } from 'react-intl';

/**
 * A toast template
 *
 * @class ToastTemplate
 * @extends {React.Component}
 */
class ToastTemplate extends React.Component {

  constructor(props) {
    super(props);
  }

  render() {
    let iconClass = null;
    if (this.props.iconClass) {
      iconClass = 'fa fa-2x ' + this.props.iconClass;
    }
    return (
      <div>
        <table>
          <tbody>
            <tr style={{ verticalAlign: 'top' }}>
              {iconClass &&
                <td style={{ padding: this.props.padding }}><i className={iconClass}></i></td>
              }
              <td style={{ padding: this.props.padding }}><FormattedMessage id={this.props.text} defaultMessage={this.props.text} /></td>
            </tr>
          </tbody>
        </table>
      </div >
    );
  }
}

ToastTemplate.propTypes = {
  text: PropTypes.string.isRequired,
  iconClass: PropTypes.string,
  padding: PropTypes.number,
};

ToastTemplate.defaultProps = {
  padding: 4
};

export default ToastTemplate;
