import React from 'react';

import { FormGroup, FormText, Label } from 'reactstrap';

import {
  toast,
} from 'react-toastify';

import {
  defaultValues as defaultFagiValues,
} from '../../../../../model/process-designer/configuration/fagi';

import {
  Roles,
} from '../../../../../model';

import {
  EnumTool,
} from '../../../../../model/process-designer';

import {
  readConfiguration,
  validateConfiguration,
} from '../../../../../service/toolkit/fagi';

import {
  SecureContent,
  ToastTemplate,
} from '../../../../helpers';

import {
  FileDrop,
} from '../../../../helpers/forms/fields/file-drop';

import {
  EnumFileSelectMode,
  FileSelectField,
  SelectField,
} from '../../../../helpers/forms/form-fields';

class FagiConfiguration extends React.Component {

  constructor(props) {
    super(props);


    this.profiles = [{
      value: null,
      label: 'Custom Profile',
      config: {
        ...defaultFagiValues,
      },
    }];

    const fagiProfiles = this.props.appConfiguration.profiles[EnumTool.FAGI] || [];
    Object.keys(fagiProfiles).map(key => {
      this.profiles.push({
        value: key,
        label: key.replace('_', ' '),
        config: {
          ...readConfiguration(fagiProfiles[key]),
          profile: key,
        },
      });
    });

    this.state = {
      advancedMappings: this.isProfileModified || this.profiles.length === 0,
    };
  }

  get isProfileModified() {
    return !this.props.value['profile'] || !!this.props.value['rulesSpec'];
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

  loadFile(file) {
    const reader = new FileReader();
    const promise = new Promise((resolve, reject) => {
      reader.onload = () => {
        try {
          const data = JSON.parse(reader.result);
          resolve(data);
        } catch (err) {
          reject(err.message);
        }
      };
    });
    reader.readAsText(file);
    promise
      .then((config) => {
        const sanitizedConfig = { ...readConfiguration(config), profile: null, };
        try {
          validateConfiguration(sanitizedConfig);
          this.props.setConfiguration(this.props.step, sanitizedConfig, {});
        } catch (errors) {
          this.props.setConfiguration(this.props.step, sanitizedConfig, errors);
        }
        toast.success(
          <ToastTemplate iconClass='fa-wrench' text={'Configuration has been loaded successfully'} />
        );
      })
      .catch((reason) => {
        toast.error(
          <ToastTemplate iconClass='fa-warning' text={'Failed to read configuration file. Reason: ' + reason} />
        );
      });
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
          help="Specify a default rules specification profile"
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
                  id="rulesSpec"
                  label="Rules file"
                  help="File containing FAGI rules"
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

        <SecureContent roles={[Roles.DEVELOPER]}>
          <div>
            <FormGroup color={props.error ? 'danger' : null}>
              <Label for="debug">Debug Configuration</Label>
              <FileDrop
                id="debug"
                value={null}
                onChange={(data) => {
                  this.loadFile(data);
                }}
              />
              <FormText color="muted">Drop a JSON file to load a custom configuration</FormText>
            </FormGroup>
          </div>
        </SecureContent>

      </div>
    );
  }

}

export default FagiConfiguration;
