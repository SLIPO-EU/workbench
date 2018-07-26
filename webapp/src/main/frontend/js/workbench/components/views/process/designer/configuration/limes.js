import React from 'react';

import { FormGroup, FormText, Label } from 'reactstrap';

import {
  toast,
} from 'react-toastify';

import {
  defaultValues as defaultLimesValues,
} from '../../../../../model/process-designer/configuration/limes';

import {
  Roles,
} from '../../../../../model';

import {
  EnumTool,
} from '../../../../../model/process-designer';

import {
  readConfiguration,
  validateConfiguration,
} from '../../../../../service/toolkit/limes';

import {
  SecureContent,
  ToastTemplate,
} from '../../../../helpers';

import {
  SelectField,
} from '../../../../helpers/forms/form-fields';

import {
  FileDrop,
} from '../../../../helpers/forms/fields/file-drop';

class LimesConfiguration extends React.Component {

  constructor(props) {
    super(props);


    this.profiles = [{
      value: null,
      label: 'Custom Profile',
      config: {
        ...defaultLimesValues,
      },
    }];

    const limesProfiles = this.props.appConfiguration.profiles[EnumTool.LIMES] || [];
    Object.keys(limesProfiles).map(key => {
      this.profiles.push({
        value: key,
        label: key.replace('_', ' '),
        config: {
          ...readConfiguration(limesProfiles[key]),
          profile: key,
        },
      });
    });

    this.state = {
      advancedMappings: this.isProfileModified || this.profiles.length === 0,
    };
  }

  get isProfileModified() {
    return !this.props.value['profile'];
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
        const sanitizedConfig = { ...readConfiguration(config), profile: null };
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
    const { errors, readOnly, setValue, value } = props;

    const inject = {
      errors,
      readOnly,
      setValue,
      value,
    };

    return (
      <div>

        <div>
          <h4>Profile</h4>
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

export default LimesConfiguration;
