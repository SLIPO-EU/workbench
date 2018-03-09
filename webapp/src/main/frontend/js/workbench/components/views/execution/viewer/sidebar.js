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
  FeaturePropertyViewer,
  Layer,
} from './';

import {
  selectLayer,
  toggleLayer,
} from '../../../../ducks/ui/views/process-designer';

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
        key={layer.tableName}
        layer={layer}
        toggle={this.props.toggleLayer}
        select={this.props.selectLayer}
        selected={this.props.selectedLayer === layer.tableName}
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
                "slipo-pd-sidebar-layer-list": true,
                "slipo-pd-sidebar-layer-list-empty": (this.props.layers.length === 0),
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
        <Row>
          <Col>
            <div className="slipo-pd-sidebar-feature-list">
              {this.props.selectedFeatures.length > 0 &&
                <FeaturePropertyViewer
                  features={this.props.selectedFeatures}
                />
              }
            </div>
          </Col>
        </Row>
      </div >
    );
  }

}

const mapStateToProps = (state) => ({
  layers: state.ui.views.process.designer.execution.layers,
  selectedLayer: state.ui.views.process.designer.execution.selectedLayer,
  selectedFeatures: state.ui.views.process.designer.execution.selectedFeatures,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  selectLayer,
  toggleLayer,
}, dispatch);

const mergeProps = (stateProps, dispatchProps, ownProps) => {
  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
  };
};

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(Sidebar);
