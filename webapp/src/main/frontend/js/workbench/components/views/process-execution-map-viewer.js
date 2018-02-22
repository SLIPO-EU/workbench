import * as React from 'react';
import * as ReactRedux from 'react-redux';

import {
  bindActionCreators
} from 'redux';

import {
  Card,
  CardBody,
  Col,
  Row,
} from 'reactstrap';

import {
  OpenLayers,
} from '../../components/helpers';

/**
 * Browse POI data and POI data integration workflow results
 *
 * @class ProcessExecutionMapViewer
 * @extends {React.Component}
 */
class ProcessExecutionMapViewer extends React.Component {

  render() {
    return (
      <div className="animated fadeIn">
        <Card>
          <CardBody>
            <OpenLayers.Map minZoom={12} maxZoom={18} zoom={15} center={[1073480.33, 5993186.68]}>
              <OpenLayers.Layers>
                <OpenLayers.Layer.OSM
                  url={this.props.osm.url}
                />
                {/* <OpenLayers.Layer.BingMaps
                  applicationKey={this.props.bingMaps.applicationKey}
                  imagerySet={this.props.bingMaps.imagerySet}
                /> */}
                <OpenLayers.Layer.WFS
                  url="/action/proxy/service/wfs"
                  version="1.1.0"
                  typename="slipo_eu:34acae4f-f648-4692-837f-5870e26eb8c6"
                />
              </OpenLayers.Layers>
              <OpenLayers.Interactions>
                <OpenLayers.Interaction.Select
                  onFeatureSelect={this.onFeatureSelect}
                />
              </OpenLayers.Interactions>
            </OpenLayers.Map>
            <div className="small text-muted">Hold Shift to select multiple features</div>
          </CardBody>
        </Card>
      </div>
    );
  }

}

const mapStateToProps = (state) => ({
  osm: state.config.osm,
  bingMaps: state.config.bingMaps,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
});

const mergeProps = (stateProps, dispatchProps, ownProps) => {
  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
  };
};

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(ProcessExecutionMapViewer);
