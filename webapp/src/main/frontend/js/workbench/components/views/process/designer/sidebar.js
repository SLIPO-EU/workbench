import * as React from 'react';
import * as ReactRedux from 'react-redux';
import classnames from 'classnames';

import {
  bindActionCreators
} from 'redux';

import {
  ButtonGroup,
  ButtonToolbar,
  Col,
  Input,
  Label,
  Nav,
  NavItem,
  NavLink,
  Row,
  TabContent,
  TabPane,
} from 'reactstrap';

import {
  ErrorList,
} from './';

import {
  EnumSelection,
  EnumInputType,
  EnumResourceType,
  ProcessInputIcons,
  ResourceTypeIcons
} from '../../../../model/process-designer';

import {
  filterResource,
  filteredResources,
  removeResourceFromBag,
  setActiveResource,
  processValidate,
  processUpdate,
} from '../../../../ducks/ui/views/process-designer';

import ProcessInput from './process-input';
import PropertyViewer from './property-viewer';
import ProcessDetails from './process-details';

/**
 * Resource filter options
 */
const filters = [{
  id: EnumInputType.CATALOG,
  iconClass: ProcessInputIcons[EnumInputType.CATALOG],
  title: 'Catalog resources',
  description: 'Displaying catalog resources',
}, {
  id: EnumInputType.OUTPUT,
  iconClass: ProcessInputIcons[EnumInputType.OUTPUT],
  title: 'Output resources',
  description: 'Displaying step output',
}, {
  id: EnumResourceType.POI,
  iconClass: ResourceTypeIcons[EnumResourceType.POI],
  title: 'POI dataset',
  description: 'Displaying POI data',
}, {
  id: EnumResourceType.LINKED,
  iconClass: ResourceTypeIcons[EnumResourceType.LINKED],
  title: 'Links',
  description: 'Displaying Links',
}];

/**
 * A connected component for rendering resources available to the process
 * designer.
 *
 * @class Sidebar
 * @extends {React.Component}
 */
class Sidebar extends React.Component {

  constructor(props) {
    super(props);

    this.toggle = this.toggle.bind(this);
    this.isFieldReadOnly = this.isFieldReadOnly.bind(this);

    this.state = {
      activeTab: '2',
    };
  }

  get selectedStepItem() {
    const step = this.props.steps.find((step) => {
      return (step.key === this.props.active.step);
    }) || null;

    const stepExecution = (step && this.props.execution ? this.props.execution.steps.find((e) => e.key === step.key) : null) || null;

    return {
      step,
      stepExecution,
    };
  }

  get selectedItem() {
    switch (this.props.active.type) {
      case EnumSelection.Process:
        return this.props.active.item;

      case EnumSelection.Step:
        return this.selectedStepItem;

      case EnumSelection.Resource:
        return this.props.resources.find((resource) => {
          return ((resource.key === this.props.active.item) && (resource.inputType === EnumInputType.CATALOG));
        }) || null;

      case EnumSelection.Input:
        return this.props.resources.find((resource) => {
          return ((resource.key === this.props.active.item) && (resource.inputType === EnumInputType.CATALOG));

        }) || null;
      default:
        return null;
    }
  }

  toggle(tab) {
    if (this.state.activeTab !== tab) {
      this.setState({
        activeTab: tab
      });
    }
  }

  isFieldReadOnly(id) {
    switch (id) {
      case 'name':
        if (this.props.process.id) {
          return true;
        }
        return this.props.readOnly;

      default:
        return this.props.readOnly;
    }
  }

  /**
   * Renders a single {@link ProcessInput}.
   *
   * @param {any} resource
   * @returns a {@link ProcessInput} component instance
   * @memberof Sidebar
   */
  renderResource(resource) {
    return (
      <ProcessInput
        key={resource.key}
        resource={resource}
        remove={this.props.removeResourceFromBag}
        setActiveResource={this.props.setActiveResource}
        active={this.props.active.type === EnumSelection.Resource && this.props.active.item === resource.key}
        readOnly={this.props.readOnly}
      />
    );
  }

  render() {
    const filter = filters.find((f) => f.id === this.props.filters.resource);
    return (
      <div style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
        <Nav tabs style={{ height: '2.75rem' }}>
          {!this.props.execution &&
            <NavItem>
              <NavLink
                className={classnames({ active: this.state.activeTab === '1' })}
                onClick={() => { this.toggle('1'); }}
              >
                <i className={this.props.resources.length === 0 ? 'icon-basket' : 'icon-basket-loaded'}></i>
                {this.state.activeTab === '1' &&
                  <div style={{ margin: '-2px 0px 0px 6px', float: 'right' }}> Resource Bag</div>
                }
              </NavLink>
            </NavItem>
          }
          <NavItem>
            <NavLink
              className={classnames({ active: this.state.activeTab === '2' })}
              onClick={() => { this.toggle('2'); }}
            >
              <i className="icon-note"></i>
              {this.state.activeTab === '2' &&
                <div style={{ margin: '-2px 0px 0px 6px', float: 'right' }}> Properties</div>
              }
            </NavLink>
          </NavItem>
          {!this.props.execution &&
            <NavItem>
              <NavLink
                className={classnames({ active: this.state.activeTab === '3' })}
                onClick={() => { this.toggle('3'); }}
                style={{ position: 'relative' }}
              >
                <i className="icon-bell"></i>
                {this.state.activeTab !== '3' && this.props.errors.length > 0 &&
                  <span className="badge badge-pill badge-danger slipo-pd-error-badge">{this.props.errors.length}</span>
                }
                {this.state.activeTab === '3' &&
                  <div style={{ margin: '-2px 0px 0px 6px', float: 'right' }}> Messages</div>
                }
              </NavLink>
            </NavItem>
          }
        </Nav>
        <TabContent activeTab={this.state.activeTab}>
          {!this.props.execution &&
            <TabPane tabId="1">
              <Row className="slipo-pd-sidebar-resource-list-wrapper">
                <Col>
                  <div style={{ borderBottom: '1px solid #cfd8dc', padding: 11 }}>
                    Filters
                    <ButtonToolbar style={{ position: 'absolute', right: 20, top: 4 }}>
                      <ButtonGroup data-toggle="buttons" aria-label="First group">
                        {filters.map((f) => (
                          <Label
                            key={f.id}
                            htmlFor={f.id}
                            className={f.id === this.props.filters.resource ? "btn btn-outline-secondary active" : "btn btn-outline-secondary "}
                            check={f.id === this.props.filters.resource}
                            style={{ border: 'none', padding: '0.5rem 0.7rem' }}
                            title={f.title}
                          >
                            <Input type="radio" name="resourceFilter" id={f.id} onClick={() => this.props.filterResource(f.id)} />
                            <i className={f.iconClass}></i>
                          </Label>))
                        }
                      </ButtonGroup>
                    </ButtonToolbar>
                  </div>
                  <div className="text-muted slipo-pd-tip" style={{ paddingLeft: 11 }}>{filter ? filter.description : 'Displaying all resources'}</div>
                  <div className={
                    classnames({
                      "slipo-pd-sidebar-resource-list": true,
                      "slipo-pd-sidebar-resource-list-empty": (this.props.resources.length === 0),
                    })
                  }>
                    {this.props.resources.length > 0 &&
                      this.props.resources.map((r) => this.renderResource(r))
                    }
                    {this.props.resources.length === 0 &&
                      <div className="text-muted slipo-pd-tip" style={{ paddingLeft: 1 }}>No resources selected</div>
                    }
                  </div>
                </Col>
              </Row>
            </TabPane>
          }
          <TabPane tabId="2">
            <Row className="mb-2" style={{ flex: '1 1 auto' }}>
              <Col>
                <div>
                  {this.selectedItem ?
                    <div className="slipo-pd-properties">
                      {this.props.active.type === EnumSelection.Process &&
                        <ProcessDetails
                          values={this.props.process.properties}
                          errors={this.props.process.errors}
                          processValidate={this.props.processValidate}
                          processUpdate={this.props.processUpdate}
                          readOnly={this.isFieldReadOnly}
                        />
                      }
                      {this.props.readOnly &&
                        <PropertyViewer
                          item={this.selectedItem}
                          type={this.props.active.type}
                        />
                      }
                    </div>
                    :
                    <div className="slipo-pd-properties">
                      <div className="text-muted slipo-pd-tip" style={{ paddingLeft: 11 }}>No item selected</div>
                    </div>
                  }
                </div>
              </Col>
            </Row>
          </TabPane>
          {!this.props.execution &&
            <TabPane tabId="3">
              <div>
                <ErrorList
                  errors={this.props.errors}
                >
                </ErrorList>
              </div>
            </TabPane>
          }
        </TabContent>
      </div >
    );
  }
}

const mapStateToProps = (state) => ({
  // Workflow properties
  active: state.ui.views.process.designer.active,
  process: state.ui.views.process.designer.process,
  readOnly: state.ui.views.process.designer.readOnly,
  steps: state.ui.views.process.designer.steps,
  resources: filteredResources(state.ui.views.process.designer),
  filters: state.ui.views.process.designer.filters,
  execution: state.ui.views.process.designer.execution.data,
  errors: state.ui.views.process.designer.errors,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  filterResource,
  processUpdate,
  processValidate,
  removeResourceFromBag,
  setActiveResource,
}, dispatch);

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps)(Sidebar);
