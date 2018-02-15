import * as React from 'react';
import PropTypes from 'prop-types';
import * as ReactRedux from 'react-redux';
import classnames from 'classnames';
import moment from 'moment';

import {
  FormattedTime
} from 'react-intl';

import {
  Col,
  Row,
} from 'reactstrap';

import {
  EnumSelection,
  EnumInputType,
  EnumResourceType,
  ToolIcons,
} from './';

import {
  JobStatus,
} from '../../../helpers';

import {
  TextField,
  TextAreaField,
} from '../../../helpers/forms/form-fields';

/**
 * A presentational component for displaying selected item properties
 *
 * @class PropertyViewer
 * @extends {React.Component}
 */
class PropertyViewer extends React.Component {

  constructor(props) {
    super(props);
  }

  static propTypes = {
    item: PropTypes.object,
    type: PropTypes.string,
  };

  renderEmpty() {
    return (
      <div className="text-muted slipo-pd-tip" style={{ paddingLeft: 11 }}>No item selected</div>
    );
  }

  renderResource(resource) {
    if (resource.inputType === EnumInputType.CATALOG) {
      const values = {
        name: resource.name.toString(),
        description: resource.description,
      };

      return (
        <div style={{ marginRight: 28 }}>
          <TextField
            id="name"
            label="Title"
            value={values}
            readonly
          />
          <TextAreaField
            rows={5}
            id="description"
            label="Description"
            value={values}
            readonly
          />
        </div>
      );
    }
    return null;
  }

  getStepIconClassName(component) {
    return (ToolIcons[component] || 'fa fa-cogs') + ' pr-2';
  }

  renderStep(step, execution) {
    if (!execution) {
      return this.renderEmpty();
    }
    return (
      <div style={{ marginRight: 28 }}>
        <Row className="mb-2">
          <Col>
            <div className="form-control-label mb-2">Step</div>
            <i className={this.getStepIconClassName(step.tool)}></i>
            <span className="font-weight-bold">{step.name}</span>
          </Col>
        </Row>
        <Row className="mb-2">
          <Col>
            <div className="form-control-label mb-2">Started On</div>
            <div className="font-weight-bold">
              {execution.startedOn ?
                <FormattedTime value={execution.startedOn} day='numeric' month='numeric' year='numeric' />
                :
                '-'
              }
            </div>
          </Col>
        </Row>
        <Row className="mb-2">
          <Col>
            <div className="form-control-label mb-2">Completed On</div>
            <div className="font-weight-bold">
              {execution.completedOn ?
                <FormattedTime value={execution.completedOn} day='numeric' month='numeric' year='numeric' />
                :
                '-'
              }
            </div>
          </Col>
        </Row>
        <Row className="mb-2">
          <Col>
            <div className="form-control-label mb-2">Duration</div>
            <div className="font-weight-bold">
              {execution.completedOn ?
                moment.duration(execution.completedOn - execution.startedOn).humanize()
                :
                moment.duration(Date.now() - execution.startedOn).humanize()
              }
            </div>
          </Col>
        </Row>
        {execution.errorMessage &&
          <Row className="mb-2">
            <Col>
              <div className="form-control-label mb-2">Error message</div>
              {execution.errorMessage}
            </Col>
          </Row>
        }
      </div>
    );
  }

  render() {
    const { type, item, ...rest } = this.props;

    if (!item) {
      return this.renderEmpty();
    }

    switch (type) {
      case EnumSelection.Resource:
      case EnumSelection.Input:
        return this.renderResource(item);

      case EnumSelection.Step:
        return this.renderStep(item.step, item.stepExecution);

      default:
        return null;
    }
  }
}

export default PropertyViewer;
