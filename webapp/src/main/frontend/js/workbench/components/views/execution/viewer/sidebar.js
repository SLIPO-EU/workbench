import * as React from 'react';
import * as ReactRedux from 'react-redux';

import classnames from 'classnames';

import {
  bindActionCreators
} from 'redux';

import {
  Button,
  Col,
  Nav,
  NavItem,
  NavLink,
  Row,
  TabContent,
  TabPane,
} from 'reactstrap';

import {
  FeaturePropertyViewer,
  Layer,
} from './';

import {
  SelectField,
} from '../../../helpers/forms/fields';

import {
  buildPath,
  DynamicRoutes,
} from '../../../../model';

import {
  selectLayer,
  setBaseLayer,
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

  constructor(props) {
    super(props);

    this.toggle = this.toggle.bind(this);

    this.state = {
      activeTab: '1'
    };
  }

  get supportedBaseLayers() {
    const baseLayers = [
      { value: 'OSM', label: 'Open Street Maps' },
    ];

    if (this.props.bingMaps.applicationKey) {
      baseLayers.push({ value: 'BingMaps-Road', label: 'Bing Maps (Road)' });
      baseLayers.push({ value: 'BingMaps-Aerial', label: 'Bing Maps (Aerial)' });
    }

    return baseLayers;
  }

  toggle(tab) {
    if (this.state.activeTab !== tab) {
      this.setState({
        activeTab: tab
      });
    }
  }

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
        <Nav tabs style={{ height: '44px' }}>
          <NavItem>
            <NavLink className={classnames({ active: this.state.activeTab === '1' })}
              onClick={() => { this.toggle('1'); }}>
              <i className="icon-list"></i>
              {this.state.activeTab === '1' &&
                <div style={{ margin: '-2px 0px 0px 6px', float: 'right' }}> Layers</div>
              }
            </NavLink>
          </NavItem>
          <NavItem>
            <NavLink className={classnames({ active: this.state.activeTab === '2' })}
              onClick={() => { this.toggle('2'); }}>
              <i className="icon-settings"></i>
              {this.state.activeTab === '2' &&
                <div style={{ margin: '-2px 0px 0px 6px', float: 'right' }}> Settings</div>
              }
            </NavLink>
          </NavItem>
        </Nav>
        <TabContent activeTab={this.state.activeTab}>
          <TabPane tabId="1">
            <Row>
              <Col>
                <div className={
                  classnames({
                    "slipo-pd-sidebar-map-layer-list": true,
                    "slipo-pd-sidebar-map-layer-list-empty": (this.props.layers.length === 0),
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
                <div className="small text-muted" style={{ padding: '0px 10px 10px' }}>Hold Shift to select multiple features</div>
                <div className="slipo-pd-sidebar-feature-list">
                  {this.props.selectedFeatures.length > 0 &&
                    <FeaturePropertyViewer
                      features={this.props.selectedFeatures}
                    />
                  }
                </div>
              </Col>
            </Row>
          </TabPane>
          <TabPane tabId="2">
            <Row>
              <Col>
                <div className="slipo-pd-sidebar-map-settings">
                  <SelectField
                    id="baseLayer"
                    label="Base Layer"
                    value={this.props.baseLayer || 'OSM'}
                    onChange={(value) => this.props.setBaseLayer(value)}
                    options={this.supportedBaseLayers}
                  />
                </div>
              </Col>
            </Row>
          </TabPane>
        </TabContent>
      </div >
    );
  }

}

const mapStateToProps = (state) => ({
  baseLayer: state.ui.views.process.designer.execution.baseLayer,
  bingMaps: state.config.bingMaps,
  layers: state.ui.views.process.designer.execution.layers,
  osm: state.config.osm,
  selectedLayer: state.ui.views.process.designer.execution.selectedLayer,
  selectedFeatures: state.ui.views.process.designer.execution.selectedFeatures,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  selectLayer,
  setBaseLayer,
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
