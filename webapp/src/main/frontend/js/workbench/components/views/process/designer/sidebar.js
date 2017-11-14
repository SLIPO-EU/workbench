import * as React from 'react';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';
import {
  Row, Col,
} from 'reactstrap';
import { removeResourceFromBag } from '../../../../ducks/ui/views/process-designer';
import ProcessInput from './process-input';

/**
 * A connected component for rendering resources available to the process
 * designer.
 *
 * @class Sidebar
 * @extends {React.Component}
 */
class Sidebar extends React.Component {

  /**
   * Renders a single {@link ProcessInput}.
   *
   * @param {any} resource
   * @param {any} index
   * @returns
   * @memberof Sidebar
   */
  renderResource(resource, index) {
    return (
      <ProcessInput
        key={index}
        resource={resource}
        remove={this.props.removeResourceFromBag} />
    );
  }

  render() {
    return (
      <div style={{ height: '100%' }}>
        <Row className="mb-2">
          <Col>
            <div style={{ borderBottom: '1px solid #cfd8dc', padding: 11 }}>Resources</div>
            <div className="slipo-pd-sidebar-resource-list">
              {this.props.resources.map((r, index) => this.renderResource(r, index))}
            </div>
          </Col>
        </Row>
      </div >
    );
  }

}

const mapStateToProps = (state) => ({
  resources: state.ui.views.process.designer.resources,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  removeResourceFromBag,
}, dispatch);

const mergeProps = (stateProps, dispatchProps, ownProps) => {
  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
  };
};

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(Sidebar);
