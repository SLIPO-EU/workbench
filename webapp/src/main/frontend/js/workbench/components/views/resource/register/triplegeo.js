import React from 'react';

import {
  CheckboxField,
  EnumFileSelectMode,
  FileSelectField,
  SelectField,
  TextField,
  ValuePairListField,
} from '../../../helpers/forms/form-fields/';

import {
  crs,
  defaultTripleGeoValues,
  encodings,
  inputFormats,
  languages,
  modes,
  ontologies,
  serializations,
} from '../../../../model/process-designer/configuration/triplegeo';

import {
  EnumTool,
} from '../../../../model/process-designer';


import {
  readConfigurationTripleGeo,
} from '../../../../service/triplegeo';

export {
  validator,
} from '../../../../service/triplegeo';

export class Component extends React.Component {

  constructor(props) {
    super(props);


    this.profiles = [{
      value: null,
      label: 'Custom Profile',
      config: {
        ...defaultTripleGeoValues,
      },
    }];

    const tripleGeoProfiles = this.props.appConfiguration.profiles[EnumTool.TripleGeo];
    Object.keys(tripleGeoProfiles).map(key => {
      this.profiles.push({
        value: key,
        label: key.replace('_', ' '),
        config: {
          ...readConfigurationTripleGeo(tripleGeoProfiles[key]),
          profile: key,
        },
      });
    });

    this.state = {
      advancedMappings: this.isProfileModified || this.profiles.length === 0,
    };
  }

  get isProfileModified() {
    return !this.props.value['profile'] || !!this.props.value['mappingSpec'] || !!this.props.value['classificationSpec'];
  }

  toggleAdvancedMappings(e) {
    e.preventDefault();

    this.setState({
      advancedMappings: !this.state.advancedMappings,
    });
  }

  changeProfile(name) {
    const profile = this.profiles.find((p) => p.value === name);
    if (profile) {
      const { setValue, value } = this.props;
      const newValue = {
        ...value,
        ...profile.config,
      };
      setValue(newValue);
    }
  }

  render() {
    const props = this.props;
    const { errors, readOnly, setValue, value, appConfiguration, filesystem, ...rest } = props;
    const { allowDelete, allowNewFolder, allowUpload, createFolder, deletePath, uploadFile } = props;

    const inject = {
      errors,
      readOnly,
      setValue,
      value,
    };

    return (
      <div>

        {value.version === '1.4' &&
          <div>
            <div>
              <h4>Profile {
                this.profiles.length !== 0 &&
                <span>
                  {
                    this.state.advancedMappings ?
                      <i className="fa fa-caret-up font-xs pr-1 pl-2"></i> :
                      <i className="fa fa-caret-down font-xs pr-1 pl-2"></i>
                  }
                  <a className="btn text-primary font-xs p-0" onClick={(e) => this.toggleAdvancedMappings(e)}>Advanced</a>
                  {
                    this.isProfileModified &&
                    <i className="fa fa-exclamation font-xs pl-1"></i>
                  }
                </span>
              }
              </h4>
              <hr />
            </div>

            <SelectField
              {...inject}
              id="profile"
              label="Selected Profile"
              help="Specify a default mapping and classification profile"
              options={this.profiles}
              clearable={false}
              onChange={(value) => {
                this.changeProfile(value);
              }}
            />

            {this.state.advancedMappings &&
              <div>
                <div className="row">
                  <div className="col">
                    <FileSelectField
                      {...inject}
                      id="mappingSpec"
                      label="Mapping specification file"
                      help="File containing RML or XSLT mappings from input schema to RDF"
                      filesystem={filesystem}
                      defaultMode={EnumFileSelectMode.FIELD}
                      allowDelete
                      allowUpload
                      allowNewFolder
                      createFolder={createFolder}
                      deletePath={deletePath}
                      uploadFile={uploadFile}
                      placeHolder={!this.props.value['profile'] ? 'Select mapping file...' : 'Using default mapping file...'}
                    />
                  </div>
                  <div className="col">
                    <div className="row">
                      <div className="col">
                        <FileSelectField
                          {...inject}
                          id="classificationSpec"
                          label="Classification specification file"
                          help="File (in YML or CSV format) containing classification hierarchy of categories"
                          filesystem={filesystem}
                          defaultMode={EnumFileSelectMode.FIELD}
                          allowDelete
                          allowUpload
                          allowNewFolder
                          createFolder={createFolder}
                          deletePath={deletePath}
                          uploadFile={uploadFile}
                          placeHolder={!this.props.value['profile'] ? 'Select classification file...' : 'Using default classification file...'}
                        />
                      </div>
                    </div>
                    <div className="row">
                      <div className="col">
                        <CheckboxField
                          {...inject}
                          id="classifyByName"
                          text="Classify By Name"
                          help="Check if features specify their category based on the actual name of the category"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            }
          </div>
        }

        < div >
          <h4>Input parameters</h4>
          <hr />
        </div>

        <div className="row">
          <div className="col">
            <SelectField
              {...inject}
              id="inputFormat"
              label="Input format"
              help="Specify format for the input geographical file(s)"
              options={inputFormats}
            />
          </div>
          <div className="col">
            <SelectField
              {...inject}
              id="mode"
              label="Mode"
              help="Conversion mode"
              options={modes}
            />
          </div>
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

        {props.value && props.value.inputFormat === 'CSV' &&
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

        {props.value && (props.value.mode === 'GRAPH' || props.value.mode === 'STREAM') &&
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
            <SelectField
              {...inject}
              id="sourceCRS"
              label="Source CRS"
              help=""
              options={crs}
            />
          </div>
          <div className="col">
            <SelectField
              {...inject}
              id="targetCRS"
              label="Target CRS"
              help=""
              options={crs}
            />
          </div>
        </div>

        {props.value && (props.value.mode === 'GRAPH' || props.value.mode === 'STREAM') &&
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
      </div>
    );
  }

}
