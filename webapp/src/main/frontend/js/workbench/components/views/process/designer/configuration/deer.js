import React from 'react';

import {
  EnumFileSelectMode,
  FileSelectField,
  SelectField,
} from '../../../../helpers/forms/form-fields';

import {
  defaultValues as defaultDeerValues,
} from '../../../../../model/process-designer/configuration/deer';

import {
  EnumTool,
} from '../../../../../model/process-designer';

import {
  readConfiguration,
} from '../../../../../service/toolkit/deer';

class DeerConfiguration extends React.Component {

  constructor(props) {
    super(props);


    this.profiles = [{
      value: null,
      label: 'Custom Profile',
      config: {
        ...defaultDeerValues,
      },
    }];

    const deerProfiles = this.props.appConfiguration.profiles[EnumTool.DEER] || [];
    Object.keys(deerProfiles).map(key => {
      this.profiles.push({
        value: key,
        label: key.replace('_', ' '),
        config: {
          ...readConfiguration(deerProfiles[key]),
          profile: key,
        },
      });
    });

    this.state = {
      advancedMappings: this.isProfileModified || this.profiles.length === 0,
    };
  }

  get isProfileModified() {
    return !this.props.value['profile'] || !!this.props.value['spec'];
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
    const { errors, readOnly, setValue, value, filesystem } = props;
    const { createFolder, deletePath, uploadFile } = props;

    const inject = {
      errors,
      readOnly,
      setValue,
      value,
    };

    return (
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
          help="Specify a default specification profile"
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
                  id="spec"
                  label="Specification file"
                  help="File containing DEER configuration settings"
                  filesystem={filesystem}
                  defaultMode={EnumFileSelectMode.FIELD}
                  allowDelete
                  allowUpload
                  allowNewFolder
                  createFolder={createFolder}
                  deletePath={deletePath}
                  uploadFile={uploadFile}
                  placeHolder={!this.props.value['profile'] ? 'Select specification file...' : 'Using default specification file...'}
                />
              </div>
            </div>
          </div>
        }

      </div>
    );
  }

}

export default DeerConfiguration;
