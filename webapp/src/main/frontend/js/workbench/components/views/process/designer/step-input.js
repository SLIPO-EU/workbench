import * as React from 'react';
import { Link } from 'react-router-dom';
import { Popover, PopoverBody, } from 'reactstrap';

import classnames from 'classnames';

import {
  DynamicRoutes,
  buildPath,
} from '../../../../model/routes';

import {
  DEFAULT_OUTPUT_PART,
  EnumInputType,
  ToolConfigurationSettings
} from '../../../../model/process-designer';

import {
  Checkbox,
} from '../../../helpers';

/**
 * A presentational component for rendering a process resource input.
 *
 * @class StepInput
 * @extends {React.Component}
 */
class StepInput extends React.Component {

  constructor(props) {
    super();

    this.state = {
      popoverOpen: false,
    };
  }

  /**
   * Remove resource from the step
   *
   * @memberof StepInput
   */
  onRemove() {
    this.props.remove(this.props.step, this.props.resource);
  }

  /**
   * Set referenced resource as the active one
   *
   * @param {any} e
   * @memberof StepInput
   */
  onSelect(e) {
    e.stopPropagation();

    this.props.setActiveStepInput(this.props.step, this.props.resource);
  }

  onToggleOutputPartSelection(e) {
    if (this.props.readOnly) {
      return;
    }
    this.setState({
      popoverOpen: !this.state.popoverOpen,
    });

  }

  onSelectOutputPart(checked, partKey) {
    if (checked) {
      this.props.selectOutputPart(this.props.step, this.props.resource, partKey);
    }
  }

  render() {
    const { step, resource } = this.props;
    const input = step.input.find((i) => i.inputKey === resource.key);
    const popoverId = `popover-${step.key}-${resource.key}`;
    const partKey = input.partKey;

    let icon = 0;

    return (
      <div
        id={popoverId}
        className="slipo-pd-step-input"
        className={
          classnames({
            "slipo-pd-step-input": true,
            "slipo-pd-step-input-active": this.props.active,
          })
        }
        onClick={(e) => this.onSelect(e)}
      >
        <div className="slipo-pd-step-resource-actions">
          {this.props.resource.inputType === EnumInputType.CATALOG &&
            <Link to={buildPath(DynamicRoutes.ResourceViewer, [this.props.resource.id, this.props.resource.version])}>
              <i className={`slipo-pd-step-resource-action slipo-pd-step-resource-view slipo-pd-step-resource-${icon++} fa fa-search`}></i>
            </Link>
          }
          {!this.props.readOnly &&
            <i className={`slipo-pd-step-resource-action slipo-pd-step-resource-delete slipo-pd-step-resource-${icon++} fa fa-trash`} onClick={() => { this.onRemove(); }}></i>
          }
        </div>
        <div className="slipo-pd-step-input-icon">
          <i className={this.props.resource.iconClass}></i>
        </div>
        <p className="slipo-pd-step-input-label">
          {this.props.resource.name}
        </p>
        <p className={
          classnames({
            "slipo-pd-step-input-part-key": true,
            "slipo-pd-step-input-part-key-enabled": !this.props.readOnly,
            "d-none": this.props.resource.inputType === EnumInputType.CATALOG,
          })
        }>
          <a
            onClick={(e) => this.onToggleOutputPartSelection(e)}
          >{partKey || DEFAULT_OUTPUT_PART}</a>
          {this.props.resource.inputType !== EnumInputType.CATALOG &&
            <Popover
              placement="bottom"
              isOpen={this.state.popoverOpen}
              target={popoverId}
              toggle={(e) => this.onToggleOutputPartSelection(e)}
              className="slipo-pd-step-input-partial-output-popover"
            >
              <PopoverBody>
                {this.renderOutputPartList(partKey)}
              </PopoverBody>
            </Popover>
          }
        </p>
      </div>
    );
  }

  renderOutputPartList(partKey) {
    const tool = this.props.resource.tool;
    const parts = ToolConfigurationSettings[tool].output;

    return parts.map((value) =>
      <Checkbox
        key={value || DEFAULT_OUTPUT_PART}
        id={value}
        text={value}
        value={value === partKey || value === DEFAULT_OUTPUT_PART && partKey === null}
        state="success"
        readOnly={false}
        onChange={(checked) => this.onSelectOutputPart(checked, value)}
      />
    );
  }
}

export default StepInput;
