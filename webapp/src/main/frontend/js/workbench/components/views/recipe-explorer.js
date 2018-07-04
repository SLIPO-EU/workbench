import * as React from 'react';
import * as ReactRedux from 'react-redux';

import {
  bindActionCreators
} from 'redux';

import {
  toast
} from 'react-toastify';

import {
  Card,
  CardBody,
  Col,
  Row,
} from 'reactstrap';

import {
  FormattedTime
} from 'react-intl';

import {
  DynamicRoutes,
  buildPath
} from '../../model/routes';

import {
  ToastTemplate,
} from '../helpers';

import {
  Filters,
  Templates,
} from "./process/template-explorer";

import {
  reset,
  cloneTemplate,
} from '../../ducks/ui/views/process-designer';

import {
  fetchTemplates,
  resetFilters,
  setFilter,
  setPager,
  setSelected,
} from '../../ducks/ui/views/process-template-explorer';

/**
 * Browse and manage process templates
 *
 * @class TemplateExplorer
 * @extends {React.Component}
 */
class TemplateExplorer extends React.Component {

  constructor(props) {
    super(props);

    this.editTemplate = this.editTemplate.bind(this);
    this.cloneTemplate = this.cloneTemplate.bind(this);
  }

  /**
   * Initializes a request for fetching template data, optionally using any
   * existing search criteria
   *
   * @memberof TemplateExplorer
   */
  componentWillMount() {
    this.props.fetchTemplates({
      query: { ...this.props.filters },
    });
  }

  /**
   * Navigates to the {@link ProcessDesigner} component for editing the current
   * version of the selected template
   *
   * @param {*} id
   */
  editTemplate(id) {
    this.props.reset();

    const path = buildPath(DynamicRoutes.ProcessDesignerEditTemplate, [id]);
    this.props.history.push(path);
  }

  /**
   * Navigates to the {@link ProcessDesigner} component for creating a new workflow
   * using the current version of the selected template
   *
   * @param {*} id
   */
  cloneTemplate(id, version) {
    this.props.cloneTemplate(id, version)
      .then(() => {
        const path = buildPath(DynamicRoutes.ProcessDesignerCreate);

        this.props.history.push(path);
      })
      .catch(err => {
        this.error(err.message);
      });
  }

  error(message) {
    toast.dismiss();

    toast.error(
      <ToastTemplate iconClass='fa-warning' text={message} />
    );

  }

  render() {
    return (
      <div className="animated fadeIn">
        <Row>
          <Col className="col-12">
            <Card>
              <CardBody className="card-body">
                {this.props.lastUpdate &&
                  <Row className="mb-2">
                    <Col >
                      <div className="small text-muted">
                        Last Update: <FormattedTime value={this.props.lastUpdate} day='numeric' month='numeric' year='numeric' />
                      </div>
                    </Col>
                  </Row>
                }
                <Row>
                  <Col>
                    <Filters
                      filters={this.props.filters}
                      setFilter={this.props.setFilter}
                      resetFilters={this.props.resetFilters}
                      fetchTemplates={this.props.fetchTemplates}
                    />
                  </Col>
                </Row>
              </CardBody>
            </Card>
            <Card>
              <CardBody className="card-body">
                <Row className="mb-2">
                  <Col>
                    <Templates
                      cloneTemplate={this.cloneTemplate}
                      editTemplate={this.editTemplate}
                      fetchTemplates={this.props.fetchTemplates}
                      filters={this.props.filters}
                      items={this.props.items}
                      pager={this.props.pager}
                      selected={this.props.selected}
                      setPager={this.props.setPager}
                      setSelected={this.props.setSelected}
                    />
                  </Col>
                </Row>
              </CardBody>
            </Card>
          </Col>
        </Row >
      </div>
    );
  }

}

const mapStateToProps = (state) => ({
  filters: state.ui.views.template.explorer.filters,
  items: state.ui.views.template.explorer.items,
  lastUpdate: state.ui.views.template.explorer.lastUpdate,
  pager: state.ui.views.template.explorer.pager,
  selected: state.ui.views.template.explorer.selected,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  // Designer
  cloneTemplate,
  reset,
  // Templates
  fetchTemplates,
  resetFilters,
  setFilter,
  setPager,
  setSelected,
}, dispatch);

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps)(TemplateExplorer);
