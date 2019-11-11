import _ from 'lodash';
import React from 'react';

import semver from 'semver-compare';

import {
  Button,
  ButtonGroup,
  ButtonToolbar,
  Input,
  Label,
} from 'reactstrap';

import GeometryType from 'ol/geom/GeometryType';

import {
  CheckboxField,
  EnumFileSelectMode,
  FileSelectField,
  GeometryField,
  SelectField,
  TextField,
  ValuePairListField,
} from '../../../../helpers/forms/form-fields';

import TripleGeoMLMappings from './triplegeo-ml-mappings';

import {
  langs,
} from '../../../../../util/i18n';

import {
  configurationLevels,
  configurationLevelOptions,
  defaultValues as defaultValuesAdvanced,
  defaultValuesAuto,
  defaultValuesSimple,
  encodings,
  inputFormats,
  modes,
  ontologies,
  serializations,
} from '../../../../../model/process-designer/configuration/triplegeo';

import {
  EnumDataFormat,
  EnumTool,
} from '../../../../../model/process-designer';

import {
  readConfiguration,
} from '../../../../../service/toolkit/triplegeo';

import ProfileOption from './profile-option';

const languages = _.orderBy(langs.map(l => ({ value: l.alpha2, label: l.English })), ['label'], ['asc']);

const getLevelDefaults = (level) => {
  switch (level) {
    case configurationLevels.ADVANCED:
      return {
        // Override only the level value
        level,
      };
    case configurationLevels.SIMPLE:
      return defaultValuesSimple;
    case configurationLevels.AUTO:
      return defaultValuesAuto;
  }

  throw new Error(`Configuration level ${level} is not supported`);
};

class TripleGeoConfiguration extends React.Component {

  constructor(props) {
    super(props);

    // Get available profiles
    const profiles = [{
      value: null,
      label: 'Custom Profile',
      config: {
        ...defaultValuesAdvanced,
      },
      comments: null,
    }];

    const { appConfiguration: config } = this.props;
    const tripleGeoProfiles = config.profiles[EnumTool.TripleGeo] || [];
    const tripleGeoProfileComments = config.profileComments[EnumTool.TripleGeo] || null;

    Object.keys(tripleGeoProfiles).map(key => {
      profiles.push({
        value: key,
        label: key.replace('_', ' '),
        config: {
          ...readConfiguration(tripleGeoProfiles[key]),
          profile: key,
        },
        comments: tripleGeoProfileComments ? tripleGeoProfileComments[key] : null || null,
      });
    });

    // Set state
    this.state = {
      profiles,
      dialog: false,
    };

    // If data source is changed, reset level to Advanced if needed
    const { enabledLevels } = props;
    if (enabledLevels.length === 1) {
      this.changeConfigurationLevel(configurationLevels.ADVANCED);
    }
  }

  changeProfile(name) {
    const { profiles } = this.state;
    const profile = profiles.find((p) => p.value === name);

    if (profile) {
      const { setValue, value } = this.props;
      const newValue = {
        ...value,
        ...profile.config,
        // Preserve configuration level
        level: value.level,
      };
      setValue(newValue);
    }
  }

  changeConfigurationLevel(level) {
    const { profiles } = this.state;
    const { setValue, value } = this.props;

    // Update configuration level
    const defaults = getLevelDefaults(level);
    const newValue = {
      ...value,
      // Reset options that are not supported by the selected mode
      ...defaults,
    };
    setValue(newValue);

    // Update profile prompt
    this.setState({
      profiles: profiles.map(p => {
        if (p.value === null) {
          if (level === configurationLevels.SIMPLE) {
            p.label = 'Select Profile ...';
          } else {
            p.label = 'Custom Profile';
          }
        }
        return p;
      })
    });
  }

  showDialog() {
    this.setState({
      dialog: true,
    });
  }

  hideDialog() {
    this.setState({
      dialog: false,
    });
  }

  render() {
    const props = this.props;
    const { appConfiguration: config, errors = {}, readOnly, setValue, value, filesystem, inputFile, } = props;
    const { createFolder, deletePath, uploadFile } = props;
    const { enabledLevels = [] } = props;
    const { profiles } = this.state;

    const inject = {
      errors,
      readOnly,
      setValue,
      value,
    };

    const mappingErrors = Object.keys(errors)
      .filter(key => key.startsWith('mapping-'))
      .map(key => ({ key, text: errors[key] }));

    return (
      <React.Fragment>
        {this.state.dialog &&
          <TripleGeoMLMappings
            configuration={value}
            errors={mappingErrors}
            getTripleGeoMappingFileAsText={this.props.getTripleGeoMappingFileAsText}
            hide={() => this.hideDialog()}
            path={inputFile}
            readOnly={readOnly}
            setValue={this.props.setValue}
            visible={this.state.dialog}
          />
        }
        <div>
          {semver(value.version, '1.2') === 1 &&
            <div>
              <div style={{ position: 'relative' }}>
                <h4 style={{ paddingTop: 6 }}>{value.level === configurationLevels.AUTO ? 'Mappings' : 'Profile'}</h4>
                {profiles.length !== 0 && enabledLevels.length > 1 &&
                  <div>
                    <ButtonToolbar style={{ position: 'absolute', right: 0, top: 2 }}>
                      <ButtonGroup data-toggle="buttons" aria-label="First group">
                        {value.level === configurationLevels.AUTO &&
                          <React.Fragment>
                            {
                              mappingErrors.length !== 0 &&
                              <i
                                className="mr-2 text-danger fa fa-exclamation"
                                title="One or more errors have been found in mappings configuration. Edit mappings to view errors."
                              />
                            }
                            < Button color="primary" onClick={() => this.showDialog()}><i className="fa fa-wrench pr-1" />Mappings</Button>
                          </React.Fragment>
                        }
                        {configurationLevelOptions
                          .filter(l => enabledLevels.indexOf(l.value) !== -1)
                          .map(l => {
                            if (readOnly && l.value !== value.level) {
                              return null;
                            }

                            return (
                              <Label
                                key={`config-mode-${l.value}`}
                                htmlFor={`config-mode-${l.value}`}
                                className={l.value === value.level ? "btn btn-outline-secondary active ml-2" : "btn btn-outline-secondary ml-2"}
                                check={l.value === value.level}
                                style={{ border: 'none', padding: '0.5rem 0.7rem' }}
                                title={l.label}
                              >
                                <Input type="radio" name="level" id={`config-mode-${l.value}`} onClick={() => this.changeConfigurationLevel(l.value)} disabled={readOnly} />
                                <span><i className={`pr-1 ${l.iconClass}`}></i>{l.label}</span>
                              </Label>);
                          })
                        }
                      </ButtonGroup>
                    </ButtonToolbar>
                  </div>
                }
                <hr />
              </div>

              {value.level !== configurationLevels.AUTO &&
                <SelectField
                  {...inject}
                  id="profile"
                  components={{ Option: ProfileOption }}
                  label="Selected Profile"
                  help="Specify a default mapping and classification profile"
                  options={profiles}
                  clearable={false}
                  onChange={(value) => {
                    this.changeProfile(value);
                  }}
                  showLabel={value.level === configurationLevels.ADVANCED}
                />
              }

              <div>
                {value.level === configurationLevels.ADVANCED &&
                  <div className="row">
                    <div className="col">
                      <FileSelectField
                        {...inject}
                        id="mappingSpec"
                        label="Mapping specification file"
                        help={
                          <span>File containing YAML mappings from input schema to RDF. Example mappings for TripleGeo can be found <a href="https://github.com/SLIPO-EU/TripleGeo/tree/master/test/conf" target="_blank">here</a>.
                          </span>
                        }
                        filesystem={filesystem}
                        defaultMode={EnumFileSelectMode.FIELD}
                        allowDelete
                        allowedFileTypes={config.tripleGeo.mappingFileTypes}
                        allowUpload
                        allowNewFolder
                        createFolder={createFolder}
                        deletePath={deletePath}
                        uploadFile={uploadFile}
                        placeHolder={!value['profile'] ? 'Select mapping file...' : 'Using default mapping file...'}
                      />
                    </div>
                  </div>
                }
                {(value.level === configurationLevels.ADVANCED || value.level === configurationLevels.AUTO) &&
                  <div className="row">
                    <div className="col">
                      <FileSelectField
                        {...inject}
                        id="classificationSpec"
                        label="Classification specification file"
                        help={
                          <span>File (in YML or CSV format) containing classification hierarchy of categories. Example classification for TripleGeo can be found <a href="https://github.com/SLIPO-EU/TripleGeo/tree/master/test/classification" target="_blank">here</a>.
                          </span>
                        }
                        filesystem={filesystem}
                        defaultMode={EnumFileSelectMode.FIELD}
                        allowDelete
                        allowedFileTypes={config.tripleGeo.classificationFileTypes}
                        allowUpload
                        allowNewFolder
                        createFolder={createFolder}
                        deletePath={deletePath}
                        uploadFile={uploadFile}
                        placeHolder={!value['profile'] ? 'Select classification file...' : 'Using default classification file...'}
                      />
                    </div>
                  </div>
                }
                {value.level === configurationLevels.ADVANCED &&
                  <div className="row">
                    <div className="col">
                      <CheckboxField
                        {...inject}
                        id="classifyByName"
                        text="Classify By Name"
                        help="Check if features specify their category based on the actual name of the category"
                        disabled={!value['profile']}
                      />
                    </div>
                  </div>
                }
              </div>

            </div>
          }

          < div >
            <h4>Input parameters</h4>
            <hr />
          </div>

          <div className="row">
            {value.level !== configurationLevels.AUTO &&
              <div className="col">
                <SelectField
                  {...inject}
                  id="inputFormat"
                  label="Input format"
                  help="Specify format for the input geographical file(s)"
                  options={inputFormats}
                />
              </div>
            }
            {value.level === configurationLevels.ADVANCED &&
              <div className="col">
                <SelectField
                  {...inject}
                  id="mode"
                  label="Mode"
                  help="Conversion mode"
                  options={modes}
                />
              </div>
            }
            <div className="col">
              <SelectField
                {...inject}
                id="encoding"
                label="Encoding"
                help="The encoding (character set) for strings in the input data. If not specified, UTF-8 encoding is assumed"
                options={encodings}
              />
            </div>
          </div>

          {value.level !== configurationLevels.AUTO &&
            <React.Fragment>
              <div>
                <h4>Data parameters</h4>
                <hr />
              </div>

              <div className="row">
                <div className="col">
                  <TextField
                    {...inject}
                    id="attrKey"
                    label="Attribute key"
                    help="Field name containing unique identifier for each entity (e.g., each record in the shapefile)"
                  />
                </div>
                <div className="col">
                  <TextField
                    {...inject}
                    id="attrName"
                    label="Attribute name"
                    help="Field name containing name literals (i.e., strings)"
                  />
                </div>
              </div>

              <div className="row">
                <div className="col">
                  <TextField
                    {...inject}
                    id="attrCategory"
                    label="Attribute category"
                    help="Field name containing literals regarding classification into categories (e.g., type of points, road classes etc.) for each feature"
                  />
                </div>
                <div className="col">
                  <TextField
                    {...inject}
                    id="attrGeometry"
                    label="Attribute geometry"
                    help="Parameter that specifies the name of the geometry column in the input dataset"
                  />
                </div>
              </div>

              {value && value.inputFormat === EnumDataFormat.CSV &&
                <div>
                  <div className="row">
                    <div className="col">
                      <TextField
                        {...inject}
                        id="delimiter"
                        label="Delimiter"
                        help="Specify the character delimiting attribute values"
                      />
                    </div>
                    <div className="col">
                      <TextField
                        {...inject}
                        id="quote"
                        label="Quote"
                        help="Specify quote character for string values"
                        maxLength={1}
                      />
                    </div>
                  </div>
                </div>
              }

              {value && (value.inputFormat === EnumDataFormat.CSV || value.inputFormat === EnumDataFormat.JSON) &&
                <div>
                  <div className="row">
                    <div className="col">
                      <TextField
                        {...inject}
                        id="attrX"
                        label="X-attribute"
                        help="Specify attribute holding X-coordinates of point locations"
                      />

                    </div>
                    <div className="col">
                      <TextField
                        {...inject}
                        id="attrY"
                        label="Y-attribute"
                        help="Specify attribute holding Y-coordinates of point locations"
                      />
                    </div>
                  </div>
                </div>
              }
            </React.Fragment>
          }

          <div className="d-none">
            <div>
              <h4>Output parameters</h4>
              <hr />
            </div>

            <div className="row">
              <div className="col">
                <SelectField
                  {...inject}
                  id="serialization"
                  label="Serialization format"
                  help="Specify export serialization for the output file"
                  options={serializations}
                />
              </div>
              <div className="col">
                <SelectField
                  {...inject}
                  id="targetGeoOntology"
                  label="Ontology Type"
                  help="Specify the spatial ontology for geometries in the exported data"
                  options={ontologies}
                />
              </div>
            </div>
          </div>

          {value && value.level !== configurationLevels.AUTO && (value.mode === 'GRAPH' || value.mode === 'STREAM') &&
            <div>
              <div>
                <h4>Namespace parameters</h4>
                <hr />
              </div>

              <TextField
                {...inject}
                id="featureSource"
                label="Feature source"
                help="Specifies the data source provider of the input features"
              />

              <div className="d-none">
                <TextField
                  {...inject}
                  id="nsOntology"
                  label="Ontology namespace"
                  help="Specify the namespace of the underlying ontology. Used in creating properties for the RDF triples"
                />

                <TextField
                  {...inject}
                  id="nsGeometry"
                  label="Geometry namespace"
                  help="Specify the namespace for the underlying geospatial ontology"
                />

                <TextField
                  {...inject}
                  id="nsFeatureURI"
                  label="Resource URI"
                  help="Specify the common URI namespace for all generated resources"
                />

                <TextField
                  {...inject}
                  id="nsClassificationURI"
                  label="Classification scheme URI"
                  help="Specify the common URI namespace for the classification scheme"
                />

                <TextField
                  {...inject}
                  id="nsClassURI"
                  label="Classification category URI"
                  help="Specify the common URI namespace for categories used in the classification scheme"
                />

                <TextField
                  {...inject}
                  id="nsDataSourceURI"
                  label="Data source provider"
                  help="Specify the common URI namespace for the data source provider"
                />

                <ValuePairListField
                  {...inject}
                  id="prefixes"
                  label="Namespace prefixes"
                  help="Specify a list of prefix and namespace pairs"
                />
              </div>
            </div>
          }

          <div>
            <h4>Spatial Reference parameters</h4>
            <hr />
          </div>

          <div className="row">
            <div className="col">
              <TextField
                {...inject}
                id="sourceCRS"
                label="Source CRS"
                help="Specify the EPSG numeric code for the source CRS"
                type="number"
                onKeyDown={(e) => {
                  // Prevent decimals
                  if (e.keyCode === 188 || e.keyCode === 190) {
                    e.preventDefault();
                  }
                }}
              />
            </div>
            <div className="col">
              <TextField
                {...inject}
                id="targetCRS"
                label="Target CRS"
                help="Specify the EPSG numeric code for the target CRS"
                type="number"
                onKeyDown={(e) => {
                  // Prevent decimals
                  if (e.keyCode === 188 || e.keyCode === 190) {
                    e.preventDefault();
                  }
                }}
              />
            </div>
          </div>

          {value && (value.mode === 'GRAPH' || value.mode === 'STREAM') &&
            <div>
              <div>
                <h4>Other parameters</h4>
                <hr />
              </div>

              <SelectField
                {...inject}
                id="defaultLang"
                label="Default language"
                help="Default lang for the labels created in the output RDF. By default, the value will be English-en"
                options={languages}
              />
            </div>
          }

          {value.level === configurationLevels.ADVANCED &&
            <div>
              <div>
                <h4>Spatial Extent</h4>
                <hr />
              </div>
              <div>
                <GeometryField
                  {...inject}
                  config={this.props.appConfiguration}
                  drawStyle={{
                    stroke: {
                      color: '#b71c1c',
                    },
                    fill: {
                      color: '#e0e0e0',
                    },
                  }}
                  help="Spatial filter to select input geometries contained within the specified polygon"
                  id="spatialExtent"
                  label="Spatial Extent"
                  showLabel={false}
                  type={GeometryType.POLYGON}
                />
              </div>
            </div>
          }
        </div>
      </React.Fragment>
    );
  }

}

export default TripleGeoConfiguration;
