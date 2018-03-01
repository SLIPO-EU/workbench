import * as React from 'react';
import * as PropTypes from 'prop-types';

import { Link } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';

/**
 * A presentational component for displaying multiple counters over a time interval.
 * All text properties are rendered using {@link FormattedNumber}. If resource strings exist,
 * the are rendered. Otherwise, the property values are rendered as default values.
 *
 * @class Card
 * @extends {React.Component}
 */
class Card extends React.Component {

  constructor(props) {
    super(props);
  }

  render() {
    const items = [];

    // Render counters
    if (this.props.items) {
      for (let index in this.props.items) {
        let item = this.props.items[index];
        items.push(
          <tr key={index} className="slipo-card-item">
            <td>
              {Array.isArray(item.value) ? item.value.join(" / ") : item.value}
            </td>
            <td>
              <FormattedMessage id={item.label} defaultMessage={item.label} />
            </td>
          </tr>
        );
      }
    }

    return (
      <div className="slipo-card-container" style={{
        background: this.props.background,
        color: this.props.color,
      }}>
        <div className="d-none d-sm-block slipo-card-icon">
          {this.props.iconClass &&
            <i className={this.props.iconClass}></i>
          }
        </div>
        <div className="slipo-card-content">
          <h3 className="slipo-card-title">
            <FormattedMessage
              id={this.props.title}
            />
          </h3>
          {items.length !== 0 ?
            <h4>
              <table>
                <tbody>
                  {items}
                </tbody>
              </table>
            </h4>
            :
            <div className="mt-3">No data available</div>
          }
          <h6 className="slipo-card-footer">
            {this.props.footer &&
              <span className="slipo-card-footer-label">{this.props.footer}</span>
            }
            {this.props.link &&
              <Link className="slipo-card-footer-link" style={{ color: this.props.color }} to={this.props.link.path}>{this.props.link.label}</Link>
            }
          </h6>
        </div>
      </div>
    );
  }
}

Card.propTypes = {
  // Card title
  title: PropTypes.string.isRequired,
  // Counter values and labels
  items: PropTypes.arrayOf(
    PropTypes.shape({
      value: PropTypes.oneOfType([
        PropTypes.number,
        PropTypes.arrayOf(PropTypes.number)
      ]).isRequired,
      label: PropTypes.string.isRequired,
    })
  ),
  // Optional image to display at the top right corner
  iconClass: PropTypes.string,
  // Background color
  background: PropTypes.string,
  // Text color
  color: PropTypes.string,
  // Footer text
  footer: PropTypes.string,
  // Optional path for rendering a link
  link: PropTypes.shape({
    path: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
  })
};

export default Card;
