import * as React from 'react';
import * as ReactRedux from 'react-redux';

import classnames from 'classnames';

import {
  bindActionCreators
} from 'redux';

import {
  Button,
  Col,
  FormGroup,
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
  Layer,
} from './';

import {
  SelectField,
} from '../../helpers/forms/fields';

import {
  Attributes,
  EnumLayerType,
} from '../../../model/map-viewer';

import {
  cancelEdit,
  refreshFeatureProvenance,
  selectLayer,
  setBaseLayer,
  toggleFilter,
  toggleLayer,
  toggleLayerConfiguration,
  updateFeature,
  updateFeatureProperty,
} from '../../../ducks/ui/views/map-viewer';

import {
  RevisionHistory,
} from './';

import {
  message,
} from '../../../service';

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

    this.toggle = this.toggleTab.bind(this);

    this.state = {
      activeTab: '1',
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

  toggleTab(tab) {
    if (this.state.activeTab !== tab) {
      this.setState({
        activeTab: tab
      });
    }
  }

  updateFeature() {
    this.props.updateFeature()
      .then(() => {
        this.props.refreshFeatureProvenance();
      })
      .catch(err => {
        message.error(err.message);
      });
  }

  onPropertyChange(key, value) {
    this.props.updateFeatureProperty(key, value);
  }

  renderMapSettings() {
    return (
      <React.Fragment>
        {this.renderMapHeader()}
        {this.renderMapContent()}
        <RevisionHistory resource={this.props.data.resource} version={this.props.data.version} />
      </React.Fragment>
    );
  }

  renderMapHeader() {
    return (
      <Nav tabs style={{ height: '2.75rem' }}>
        <NavItem>
          <NavLink className={classnames({ active: this.state.activeTab === '1' })}
            onClick={() => { this.toggleTab('1'); }}>
            <i className="icon-layers"></i>
            {this.state.activeTab === '1' &&
              <div style={{ margin: '-2px 0px 0px 6px', float: 'right' }}> Layers</div>
            }
          </NavLink>
        </NavItem>
        <NavItem>
          <NavLink className={classnames({ active: this.state.activeTab === '2' })}
            onClick={() => { this.toggleTab('2'); }}>
            <i className="icon-settings"></i>
            {this.state.activeTab === '2' &&
              <div style={{ margin: '-2px 0px 0px 6px', float: 'right' }}> Settings</div>
            }
          </NavLink>
        </NavItem>
      </Nav>
    );
  }

  renderMapContent() {
    const { filters = [], layers } = this.props;

    const inputLayers = layers.filter((l) => l.type === EnumLayerType.Input);
    const outputLayers = layers.filter((l) => l.type === EnumLayerType.Output);

    const inputFiltered = inputLayers.find(l => filters.find(f => f.layer === l.tableName));
    const outputFiltered = outputLayers.find(l => filters.find(f => f.layer === l.tableName));


    return (
      <TabContent activeTab={this.state.activeTab}>
        <TabPane tabId="1">
          <Row>
            <Col>
              <div style={{ borderBottom: '1px solid #cfd8dc', padding: 11 }}>
                Input
                  {inputFiltered &&
                  <i
                    className="fa fa-filter pl-1 slipo-action-icon"
                    onClick={() => this.props.toggleFilter(true)}
                    title="One or more filters are set"
                  />
                }
              </div>
              <div className="slipo-pd-sidebar-map-layer-list">
                {inputLayers.length > 0 &&
                  inputLayers.map((l) => this.renderLayer(l))
                }
                {inputLayers.length === 0 &&
                  <div className="text-muted slipo-pd-tip" style={{ paddingLeft: 1 }}>No input layers found</div>
                }
              </div>
              <div style={{ borderBottom: '1px solid #cfd8dc', borderTop: '1px solid #cfd8dc', padding: 11 }}>
                Output
                  {outputFiltered &&
                  <i
                    className="fa fa-filter pl-1 slipo-action-icon"
                    onClick={() => this.props.toggleFilter(true)}
                    title="One or more filters are set"
                  />
                }
              </div>
              <div className="slipo-pd-sidebar-map-layer-list">
                {outputLayers.length > 0 &&
                  outputLayers.map((l) => this.renderLayer(l))
                }
                {outputLayers.length === 0 &&
                  <div className="text-muted slipo-pd-tip" style={{ paddingLeft: 1 }}>No output layers found</div>
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
    );
  }

  renderLayer(layer) {
    return (
      <Layer
        key={`${layer.tableName}-${layer.color}`}
        layer={layer}
        toggle={this.props.toggleLayer}
        select={this.props.selectLayer}
        selected={this.props.selectedLayer !== null && this.props.selectedLayer.tableName === layer.tableName}
        toggleLayerConfiguration={this.props.toggleLayerConfiguration}
      />
    );
  }

  renderEditor() {
    return (
      <div style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
        {this.renderEditorHeader()}
        {this.renderEditorContent()}
      </div>
    );
  }

  renderEditorHeader() {
    return (
      <React.Fragment>
        <div style={{ borderBottom: '1px solid #cfd8dc', padding: '0.7rem' }}>
          <span><i className="fa fa-pencil pr-1"></i>Edit</span>
        </div>
        <div style={{ position: 'absolute', right: 22, top: 8 }}>
          <Button color="primary" style={{ padding: '0.25rem 0.5rem' }} onClick={() => this.updateFeature()}>
            <i className="fa fa-save" />
          </Button>
          {' '}
          <Button color="danger" style={{ padding: '0.25rem 0.5rem' }} onClick={() => this.props.cancelEdit()}>
            <i className="fa fa-remove" />
          </Button>
        </div>
      </React.Fragment>
    );
  }

  renderEditorContent() {
    const { editActive, current, initial, } = this.props;

    if (!editActive) {
      return null;
    }

    const fields = Attributes
      .filter(a => a.editable)
      .sort((a1, a2) => a1.title > a2.title ? 1 : -1)
      .map(a => (
        <FormGroup key={a.key}>
          <Label for={a.key}>{a.title}</Label>
          <Input
            className={current[a.key] !== initial[a.key] ? 'slipo-map-property-modified' : ''}
            type="text"
            name={a.key}
            id={a.key}
            value={current[a.key]}
            onChange={(e) => this.onPropertyChange(a.key, e.target.value)}
          />
        </FormGroup>
      ));

    return (
      <div className="mb-5 p-2" style={{ overflowY: 'auto' }}>
        {fields}
      </div>
    );
  }

  render() {
    const { editActive } = this.props;

    return (
      <div style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
        {
          editActive ? this.renderEditor() : this.renderMapSettings()
        }
      </div >
    );
  }

}

const mapStateToProps = (state) => ({
  baseLayer: state.ui.views.map.config.baseLayer,
  bingMaps: state.config.bingMaps,
  current: state.ui.views.map.edit.current.properties,
  data: state.ui.views.map.data,
  editActive: state.ui.views.map.edit.active,
  filters: state.ui.views.map.search.filters,
  initial: state.ui.views.map.edit.initial.properties,
  layers: state.ui.views.map.config.layers,
  osm: state.config.osm,
  selectedFeatures: state.ui.views.map.config.selectedFeatures,
  selectedLayer: state.ui.views.map.config.selectedLayer,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  cancelEdit,
  refreshFeatureProvenance,
  selectLayer,
  setBaseLayer,
  toggleFilter,
  toggleLayer,
  toggleLayerConfiguration,
  updateFeature,
  updateFeatureProperty,
}, dispatch);

const mergeProps = (stateProps, dispatchProps, ownProps) => {
  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
  };
};

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(Sidebar);
