import * as React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';

/**
 * A presentational component for rendering a data source or harvester designer
 * item.
 *
 * @class StepDataSource
 * @extends {React.Component}
 */
class StepDataSource extends React.Component {

  constructor(props) {
    super();
  }

  static propTypes = {
    active: PropTypes.bool.isRequired,
    step: PropTypes.object.isRequired,
    dataSource: PropTypes.object.isRequired,
    removeStepDataSource: PropTypes.func.isRequired,
    configureStepDataSourceBegin: PropTypes.func.isRequired,
    setActiveStepDataSource: PropTypes.func.isRequired,
  }

  /**
   * Remove data source from the step
   *
   * @memberof StepDataSource
   */
  remove() {
    this.props.removeStepDataSource(this.props.step, this.props.dataSource);
  }

  /**
   * Set referenced data source or harvester as the active one
   *
   * @param {any} e
   * @memberof StepDataSource
   */
  select(e) {
    e.stopPropagation();

    this.props.setActiveStepDataSource(this.props.step, this.props.dataSource);
  }

  /**
   * Initialize the configuration of the data source
   *
   * @param {any} e
   * @memberof StepDataSource
   */
  configure(e) {
    e.stopPropagation();

    this.props.configureStepDataSourceBegin(this.props.step, this.props.dataSource, this.props.dataSource.configuration);
  }

  render() {
    return (
      <div className="slipo-pd-step-input"
        className={
          classnames({
            "slipo-pd-step-input": true,
            "slipo-pd-step-input-active": this.props.active,
            "slipo-pd-step-input-invalid": (!this.props.dataSource.configuration)
          })
        }
        onClick={(e) => this.select(e)}
      >
        <div className="slipo-pd-step-data-source-actions">
          <i className="slipo-pd-step-data-source-action slipo-pd-step-data-source-delete fa fa-trash" onClick={() => { this.remove(); }}></i>
          <i className="slipo-pd-step-data-source-action slipo-pd-step-data-source-config fa fa-wrench" onClick={(e) => { this.configure(e); }}></i>
        </div>
        <div className="slipo-pd-step-input-icon">
          <i className={this.props.dataSource.iconClass}></i>
        </div>
        <div className="slipo-pd-step-input-label">
          {this.props.dataSource.title}
        </div>
      </div>
    );
  }

}

export default StepDataSource;
