import * as React from 'react';
import * as ReactRedux from 'react-redux';

import classnames from 'classnames';

import {
  bindActionCreators
} from 'redux';

import {
  Col,
  Row,
} from 'reactstrap';


import {
  Layer
} from './';

import {
  removeFromMap,
  selectLayer,
} from '../../../../ducks/ui/views/process-execution-viewer';

/**
 * A connected component for rendering execution selected files available to map
 * viewer
 *
 * @class Sidebar
 * @extends {React.Component}
 */
class Sidebar extends React.Component {

  /**
   * Renders a single {@link Layer}.
   *
   * @param {any} layer
   * @returns a {@link Layer} component instance
   * @memberof Sidebar
   */
  renderLayer(layer) {
    return (
      <Layer
        key={layer.id}
        layer={layer}
        remove={this.props.removeFromMap}
        select={this.props.selectLayer}
        selected={this.props.selectedLayer === layer.id}
      />
    );
  }

  render() {
    return (
      <div style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
        <Row>
          <Col>
            <div className={
              classnames({
                "slipo-pd-sidebar-resource-list": true,
                "slipo-pd-sidebar-resource-list-empty": (this.props.layers.length === 0),
              })
            }>
              {this.props.layers.length > 0 &&
                this.props.layers.map((l) => this.renderLayer(l))
              }
              {this.props.layers.length === 0 &&
                <div className="text-muted slipo-pd-tip" style={{ paddingLeft: 1 }}>No layers selected</div>
              }
            </div>
          </Col>
        </Row>
      </div >
    );
  }

}

const mapStateToProps = (state) => ({
  layers: state.ui.views.execution.viewer.layers,
  selectedLayer: state.ui.views.execution.viewer.selectedLayer,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  removeFromMap,
  selectLayer,
}, dispatch);

const mergeProps = (stateProps, dispatchProps, ownProps) => {
  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
  };
};

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(Sidebar);
