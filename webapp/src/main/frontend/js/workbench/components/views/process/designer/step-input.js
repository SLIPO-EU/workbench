import * as React from 'react';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import {
  DynamicRoutes,
  buildPath,
} from '../../../../model/routes';
import {
  EnumProcessInput
} from './constants';

/**
 * A presentational component for rendering a process resource input.
 *
 * @class StepInput
 * @extends {React.Component}
 */
class StepInput extends React.Component {

  constructor(props) {
    super();
  }

  /**
   * Remove resource from the step
   *
   * @memberof StepInput
   */
  remove() {
    this.props.remove(this.props.step, this.props.resource);
  }

  /**
   * Set referenced resource as the active one
   *
   * @param {any} e
   * @memberof StepInput
   */
  select(e) {
    // Prevent parent step from being selected
    e.stopPropagation();

    this.props.setActiveStepInput(this.props.step, this.props.resource);
  }

  render() {
    return (
      <div className="slipo-pd-step-input"
        className={
          classnames({
            "slipo-pd-step-input": true,
            "slipo-pd-step-input-active": this.props.active,
          })
        }
        onClick={(e) => this.select(e)}
      >
        <div className="slipo-pd-step-resource-actions">
          <i className="slipo-pd-step-resource-action slipo-pd-step-resource-delete fa fa-trash" onClick={() => { this.remove(); }}></i>
          {this.props.resource.inputType === EnumProcessInput.CATALOG &&
            <Link to={buildPath(DynamicRoutes.ResourceViewer, [this.props.resource.id])}>
              <i className="slipo-pd-step-resource-action slipo-pd-step-resource-view fa fa-search"></i>
            </Link>
          }
        </div>
        <div className="slipo-pd-step-input-icon">
          <i className={this.props.resource.iconClass}></i>
        </div>
        <div className="slipo-pd-step-input-label">
          {this.props.resource.title}
        </div>
      </div>
    );
  }

}

export default StepInput;
