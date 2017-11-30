import * as React from 'react';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';
import {
  Row, Col,
  ButtonToolbar, Button, ButtonGroup, Label, Input
} from 'reactstrap';
import {
  filterResource,
  filteredResources,
  removeResourceFromBag,
  setActiveResource
} from '../../../../ducks/ui/views/process-designer';
import classnames from 'classnames';
import {
  ProcessInputIcons,
  ResourceTypeIcons
} from './config';
import {
  EnumSelection,
  EnumProcessInput,
  EnumResourceType
} from './constants';
import ProcessInput from './process-input';
import Details from './details';

/**
 * Resource filter options
 */
const filters = [{
  id: EnumProcessInput.CATALOG,
  iconClass: ProcessInputIcons[EnumProcessInput.CATALOG],
  title: 'Catalog resources',
  description: 'Displaying catalog resources',
}, {
  id: EnumProcessInput.OUTPUT,
  iconClass: ProcessInputIcons[EnumProcessInput.OUTPUT],
  title: 'Output resources',
  description: 'Displaying step output resources',
}, {
  id: EnumResourceType.POI,
  iconClass: ResourceTypeIcons[EnumResourceType.POI],
  title: 'POI dataset',
  description: 'Displaying POI data',
}, {
  id: EnumResourceType.LINKED,
  iconClass: ResourceTypeIcons[EnumResourceType.LINKED],
  title: 'POI Linked Data',
  description: 'Displaying POI Linked data',
}];

/**
 * A connected component for rendering resources available to the process
 * designer.
 *
 * @class Sidebar
 * @extends {React.Component}
 */
class Sidebar extends React.Component {

  /**
   * Resolves selected item
   *
   * @returns the selected item
   * @memberof Sidebar
   */
  getSelectedItem() {
    switch (this.props.active.type) {
      case EnumSelection.Resource:
        return this.props.resources.find((resource) => {
          return (resource.index === this.props.active.item);
        }) || null;
      case EnumSelection.Input:
        return this.props.resources.find((resource) => {
          return ((resource.index === this.props.active.item) && (resource.inputType === EnumProcessInput.CATALOG));
        }) || null;
      default:
        return null;
    }
  }

  /**
   * Renders a single {@link ProcessInput}.
   *
   * @param {any} resource
   * @param {any} index
   * @returns a {@link ProcessInput} component instance
   * @memberof Sidebar
   */
  renderResource(resource) {
    return (
      <ProcessInput
        key={resource.index}
        resource={resource}
        remove={this.props.removeResourceFromBag}
        setActiveResource={this.props.setActiveResource}
        active={this.props.active.type === EnumSelection.Resource && this.props.active.item === resource.index}
      />
    );
  }

  render() {
    const filter = filters.find((f) => f.id === this.props.filters.resource);
    return (
      <div style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
        <Row>
          <Col>
            <div style={{ borderBottom: '1px solid #cfd8dc', padding: 11 }}>
              Resources
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
              {this.props.resources.map((r, index) => this.renderResource(r))}
            </div>
          </Col>
        </Row>
        <Row className="mb-2" style={{ flex: '1 1 auto' }}>
          <Col>
            <Details item={this.getSelectedItem()} type={this.props.active.type} />
          </Col>
        </Row>
      </div >
    );
  }

}

const mapStateToProps = (state) => ({
  active: state.ui.views.process.designer.active,
  steps: state.ui.views.process.designer.steps,
  resources: filteredResources(state.ui.views.process.designer),
  filters: state.ui.views.process.designer.filters,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  filterResource,
  removeResourceFromBag,
  setActiveResource,
}, dispatch);

const mergeProps = (stateProps, dispatchProps, ownProps) => {
  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
  };
};

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(Sidebar);
