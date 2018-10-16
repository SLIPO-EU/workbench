import React from 'react';

import semver from 'semver-compare';

import {
  EnumFileSelectMode,
  FileSelectField,
  SelectField,
  TextField,
} from '../../../../helpers/forms/form-fields';

import {
  crs,
  defaultReverseValues as defaultTripleGeoValues,
  encodings,
  languages,
  outputFormats,
} from '../../../../../model/process-designer/configuration/triplegeo';

import {
  EnumDataFormat,
  EnumTool,
} from '../../../../../model/process-designer';

import {
  readConfiguration,
} from '../../../../../service/toolkit/triplegeo-reverse';

class TripleGeoReverseConfiguration extends React.Component {

  constructor(props) {
    super(props);


    this.profiles = [{
      value: null,
      label: 'Custom Profile',
      config: {
        ...defaultTripleGeoValues,
      },
    }];

    const tripleGeoProfiles = this.props.appConfiguration.profiles[EnumTool.ReverseTripleGeo] || [];
    Object.keys(tripleGeoProfiles).map(key => {
      this.profiles.push({
        value: key,
        label: key.replace('_', ' '),
        config: {
          ...readConfiguration(tripleGeoProfiles[key]),
          profile: key,
        },
      });
    });

    this.state = {
      displayQueryField: this.isProfileModified || this.profiles.length === 0,
    };
  }

  get isProfileModified() {
    return !this.props.value['profile'] || !!this.props.value['sparqlFile'];
  }

  toggleAdvancedQuery(e) {
    e.preventDefault();

    this.setState({
      displayQueryField: !this.state.displayQueryField,
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
    const { errors, readOnly, setValue, value, appConfiguration, filesystem, } = props;
    const { createFolder, deletePath, uploadFile } = props;

    const inject = {
      errors,
      readOnly,
      setValue,
      value,
    };

    return (
      <div>

        {semver(value.version, '1.2') === 1 &&
          <div>
            <div>
              <h4>Profile {
                this.profiles.length !== 0 &&
                <span>
                  {
                    this.state.displayQueryField ?
                      <i className="fa fa-caret-up font-xs pr-1 pl-2"></i> :
                      <i className="fa fa-caret-down font-xs pr-1 pl-2"></i>
                  }
                  <a className="btn text-primary font-xs p-0" onClick={(e) => this.toggleAdvancedQuery(e)}>Advanced</a>
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
              help="Specify a default SPARQL query"
              options={this.profiles}
              clearable={false}
              onChange={(value) => {
                this.changeProfile(value);
              }}
            />

            {this.state.displayQueryField &&
              <div>
                <div className="row">
                  <div className="col">
                    <FileSelectField
                      {...inject}
                      id="sparqlFile"
                      label="SELECT query"
                      help="File containing a user-specified SELECT query (in SPARQL) that will retrieve results from the input RDF triples. This query should conform with the underlying ontology of the input RDF triples."
                      filesystem={filesystem}
                      defaultMode={EnumFileSelectMode.FIELD}
                      allowDelete
                      allowUpload
                      allowNewFolder
                      createFolder={createFolder}
                      deletePath={deletePath}
                      uploadFile={uploadFile}
                      placeHolder={!this.props.value['profile'] ? 'Select mapping file...' : 'Using default query ...'}
                    />
                  </div>
                </div>
              </div>
            }
          </div>
        }

        < div >
          <h4>Output  parameters</h4>
          <hr />
        </div>

        <div className="row">
          <div className="col">
            <SelectField
              {...inject}
              id="outputFormat"
              label="Output format"
              help="Specify format for the output geographical file(s)"
              options={outputFormats}
            />
          </div>
          <div className="col">
            <SelectField
              {...inject}
              id="encoding"
              label="Encoding"
              help="The encoding (character set) for strings in the output data. If not specified, UTF-8 encoding is assumed."
              options={encodings}
            />
          </div>
        </div>

        {props.value && props.value.outputFormat === EnumDataFormat.CSV &&
          <div>
            <div>
              <h4>Data parameters</h4>
              <hr />
            </div>

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
              clearable={true}
            />
          </div>
          <div className="col">
            <SelectField
              {...inject}
              id="targetCRS"
              label="Target CRS"
              help=""
              options={crs}
              clearable={true}
            />
          </div>
        </div>

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
      </div>
    );
  }

}

export default TripleGeoReverseConfiguration;
