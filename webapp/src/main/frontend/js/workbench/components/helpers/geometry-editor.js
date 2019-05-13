import * as React from 'react';

import { Input } from 'reactstrap';

import Feature from 'ol/Feature';
import { fromLonLat } from 'ol/proj';
import { default as GeometryType } from 'ol/geom/GeometryType';

import {
  OpenLayers,
} from '../helpers';

import {
  Colors,
} from '../../model/constants';

import {
  EnumSymbol,
} from '../../model/map-viewer';

import {
  compareGeometry,
  fromWKT,
  toWKT,
} from '../../util/geometry';


const defaultStyle = {
  symbol: EnumSymbol.Square,
  stroke: {
    color: '#424242',
    width: 2,
  },
  fill: {
    color: '#212121',
  },
  size: 10,
  opacity: 50,
};

const selectStyle = {
  symbol: EnumSymbol.Square,
  stroke: {
    color: '#d50000',
    width: 2,
  },
  fill: {
    color: '#b71c1c',
  },
  size: 10,
  opacity: 50,
};

const createFeature = (wkt) => {
  const geometry = wkt ? fromWKT(wkt) : null;
  const feature = new Feature({
    id: -1,
    geometry,
  });

  return feature;
};

const cloneFeature = (feature) => {
  const clone = feature.clone();
  clone.setId(feature.getId());
  return clone;
};

const EnumMode = {
  TEXT: 'TEXT',
  MAP: 'MAP',
};

class GeometryEditor extends React.Component {

  constructor(props) {
    super(props);

    const { value } = props;

    const initialFeature = createFeature(value);
    const editableFeature = cloneFeature(initialFeature);

    this.state = {
      // True if geometry is modified
      modified: false,
      // Initial feature as an OpenLayers Feature instance
      initialFeature,
      // Current feature as an OpenLayers Feature instance
      editableFeature,
      // Geometry type
      type: props.type ? props.type : GeometryType.POINT,
      // Enable draw interaction
      draw: !editableFeature.getGeometry(),
      // Editor mode
      mode: EnumMode.MAP,
      // Invalid WKT
      invalid: value && !initialFeature.getGeometry(),
    };
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.value !== nextProps.value) {
      const { initialFeature } = this.state;

      const editableFeature = createFeature(nextProps.value);

      this.setState({
        editableFeature,
        modified: !compareGeometry(initialFeature.getGeometry(), editableFeature.getGeometry()),
        draw: !editableFeature.getGeometry(),
        invalid: nextProps.value && !editableFeature.getGeometry(),
      });
    }
  }

  getLayerStyle() {
    return {
      symbol: EnumSymbol.Square,
      fill: {
        color: Colors[0],
      },
      stroke: {
        color: Colors[0],
        width: 2,
      },
      size: 10,
      opacity: 50,
    };
  }

  onGeometryChange(features) {
    const { initialFeature: initial = null } = this.state;

    if ((features) && (features.getLength() === 1)) {
      const updated = features.item(0);

      this.setState({
        editableFeature: updated,
        modified: !compareGeometry(initial.getGeometry(), updated.getGeometry()),
        draw: false,
      });

      if (typeof this.props.onChange === 'function') {
        this.props.onChange(toWKT(updated));
      }
    }
  }

  onDeleteGeometry(e) {
    e.preventDefault();

    const { initialFeature, editableFeature } = this.state;
    editableFeature.setGeometry(null);

    this.setState({
      editableFeature,
      modified: compareGeometry(initialFeature.getGeometry(), editableFeature.getGeometry()),
      draw: true,
    });

    if (typeof this.props.onChange === 'function') {
      this.props.onChange(null);
    }
  }

  onRestoreGeometry(e) {
    e.preventDefault();

    const { initialFeature } = this.state;

    const editableFeature = cloneFeature(initialFeature);

    this.setState({
      editableFeature,
      modified: false,
      draw: !editableFeature.getGeometry(),
    });
  }

  getAsWKT() {
    const { editableFeature: feature } = this.state;

    if (feature.getGeometry()) {
      return toWKT(feature);
    } else {
      return null;
    }
  }

  setFromWKT(wkt) {
    const { initialFeature, editableFeature } = this.state;

    const geometry = fromWKT(wkt);

    editableFeature.setGeometry(geometry);

    this.setState({
      editableFeature,
      modified: compareGeometry(initialFeature.getGeometry(), editableFeature.getGeometry()),
      draw: !editableFeature.getGeometry(),
      invalid: wkt && !editableFeature.getGeometry(),
    });
  }

  onModeChange(e, mode) {
    e.preventDefault();

    this.setState({
      mode,
    });
  }

  render() {
    const {
      draw,
      editableFeature,
      mode,
      type,
      invalid,
    } = this.state;

    const { config: { osm: { url } }, readOnly } = this.props;

    if (mode === EnumMode.MAP) {
      return (
        <div style={{ position: 'relative' }}>
          <div
            className="map-button"
            style={{
              top: 10,
            }}
            onClick={(e) => this.onModeChange(e, EnumMode.TEXT)}>
            <i className="mdi mdi-18px mdi-text-subject" />
          </div>
          {!readOnly &&
            <React.Fragment>
              <div
                className="map-button"
                style={{
                  top: 45,
                }}
                onClick={(e) => this.onDeleteGeometry(e)}>
                <i className="mdi mdi-18px mdi-trash-can-outline" />
              </div>
              <div
                className="map-button"
                style={{
                  top: 80,
                }}
                onClick={(e) => this.onRestoreGeometry(e)}>
                <i className="mdi mdi-18px mdi-restore" />
              </div>
            </React.Fragment>
          }
          <OpenLayers.Map
            minZoom={4}
            maxZoom={19}
            zoom={4}
            center={fromLonLat([16.03, 48.76])}
          >
            <OpenLayers.Layers>
              <OpenLayers.Layer.OSM
                url={url}
              />
            </OpenLayers.Layers>
            <OpenLayers.Interactions>
              <OpenLayers.Interaction.Select
                active={false}
                selected={editableFeature || null}
                style={selectStyle}
                fitToExtent={true}
              />
              <OpenLayers.Interaction.Modify
                active={!draw && !readOnly}
                feature={editableFeature || null}
                onGeometryChange={(features) => this.onGeometryChange(features)}
                style={defaultStyle}
              />
              <OpenLayers.Interaction.Draw
                active={draw && !readOnly}
                feature={editableFeature || null}
                onDrawEnd={(features) => this.onGeometryChange(features)}
                style={defaultStyle}
                type={type}
              />
            </OpenLayers.Interactions>
          </OpenLayers.Map>
        </div>
      );
    }

    return (
      <div style={{ position: 'relative' }}>
        <div
          className="map-button"
          style={{
            top: 10,
          }}
          onClick={(e) => this.onModeChange(e, EnumMode.MAP)}>
          <i className="mdi mdi-18px mdi-map-outline" />
        </div>
        {!readOnly &&
          <div
            className="map-button"
            style={{
              top: 45,
            }}
            onClick={(e) => this.onDeleteGeometry(e)}>
            <i className="mdi mdi-18px mdi-trash-can-outline" />
          </div>
        }
        <Input
          type="textarea"
          rows={this.props.rows || 20}
          name={this.props.id}
          id={this.props.id}
          state={this.props.state}
          value={this.props.value || ''}
          onChange={e => typeof this.props.onChange === 'function' ? this.props.onChange(e.target.value) : null}
          readOnly={readOnly}
          invalid={invalid}
        />
      </div>
    );
  }

}

export default GeometryEditor;
