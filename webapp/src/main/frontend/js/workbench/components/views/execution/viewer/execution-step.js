import * as React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import moment from 'moment';

import {
  FormattedTime
} from 'react-intl';

import {
  Card,
  CardBody,
  Col,
  Row,
} from 'reactstrap';

import {
  JobStatus,
} from '../../../helpers';

import {
  ToolIcons,
} from '../../process/designer';

class ExecutionStep extends React.Component {

  constructor(props) {
    super(props);
  }

  static propTypes = {
    step: PropTypes.object.isRequired,
    selected: PropTypes.bool.isRequired,

    // Action creators
    selectStep: PropTypes.func.isRequired,
  };

  /**
   * Resolve step icon class
   *
   * @returns a CSS class
   * @memberof ExecutionStep
   */
  getIconClassName() {
    return (ToolIcons[this.props.step.component] || 'fa fa-cogs') + ' fa-2x pr-2';
  }

  /**
   * Set referenced step as the active one
   *
   * @memberof ExecutionStep
   */
  select(e) {
    e.stopPropagation();
    if (!this.props.selected) {
      this.props.selectStep(this.props.step.id);
    }
  }

  render() {
    const s = this.props.step;

    return (
      <Card
        className={
          classnames({
            "slipo-card-selected": this.props.selected
          })

        }
        onClick={(e) => this.select(e)}
      >
        <CardBody>
          <Row className="mb-4">
            <Col xs="8">
              <i className={this.getIconClassName()}></i>
              <span className="font-2xl">{s.name}</span>
            </Col>
            <Col xs="4">
              <div className="font-weight-bold mb-1">Status</div>
              <JobStatus status={s.status} />
            </Col>
          </Row>
          {this.props.selected &&
            <Row className="mb-4">
              <Col>
                <div className="font-weight-bold mb-1">Started On</div>
                <div className="font-weight-italic">
                  {s.startedOn ?
                    <FormattedTime value={s.startedOn} day='numeric' month='numeric' year='numeric' />
                    :
                    '-'
                  }
                </div>
              </Col>
              <Col>
                <div className="font-weight-bold mb-1">Completed On</div>
                <div className="font-weight-italic">
                  {s.completedOn ?
                    <FormattedTime value={s.completedOn} day='numeric' month='numeric' year='numeric' />
                    :
                    '-'
                  }
                </div>
              </Col>
              <Col>
                <div className="font-weight-bold mb-1">Duration</div>
                <div className="font-weight-italic">
                  {s.completedOn ?
                    moment.duration(s.completedOn - s.startedOn).humanize()
                    :
                    moment.duration(Date.now() - s.startedOn).humanize()
                  }
                </div>
              </Col>
            </Row>
          }
          {this.props.selected && s.errorMessage &&
            <Row className="mb-4">
              <Col>
                <div className="font-weight-bold mb-1">Error message</div>
                {s.errorMessage}
              </Col>
            </Row>
          }

        </CardBody>
      </Card >
    );
  }

}

export default ExecutionStep;
