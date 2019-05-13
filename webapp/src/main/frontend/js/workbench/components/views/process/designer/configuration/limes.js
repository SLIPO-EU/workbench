import React from 'react';

import {
  ButtonGroup,
  ButtonToolbar,
  FormGroup,
  FormText,
  Input,
  Label,
} from 'reactstrap';

import {
  configurationLevels,
  configurationLevelOptions,
  defaultValues as defaultValuesAdvanced,
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
} from '../../../../helpers';

import {
  SelectField,
} from '../../../../helpers/forms/form-fields';

import {
  FileDrop,
} from '../../../../helpers/forms/fields/file-drop';

import {
  message,
} from '../../../../../service';

class LimesConfiguration extends React.Component {

  constructor(props) {
    super(props);


    this.profiles = [{
      value: null,
      label: 'Custom Profile',
      config: {
        ...defaultValuesAdvanced,
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

    // If only a single configuration level is enabled, reset level to Advanced
    const { enabledLevels } = props;
    if (enabledLevels.length === 1) {
      this.changeConfigurationLevel(configurationLevels.ADVANCED);
    }
  }

  getLevelDefaults(level) {
    switch (level) {
      case configurationLevels.ADVANCED:
        return {
          ...defaultValuesAdvanced,
          // Do not override level
          level,
        };
      case configurationLevels.AUTO: {
        const defaultProfileName = this.props.appConfiguration.limes.defaultProfile;
        const profile = this.profiles.find((p) => p.value === defaultProfileName) || null;

        if (profile) {
          return {
            ...profile.config,
            // Do not override level
            level,
          };
        } else {
          throw new Error(`Default profile is not set`);
        }
      }
    }

    throw new Error(`Configuration level ${level} is not supported`);
  }

  changeProfile(name) {
    const profile = this.profiles.find((p) => p.value === name);

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
    const { setValue, value } = this.props;

    // Update configuration level
    const defaults = this.getLevelDefaults(level);

    const newValue = {
      ...value,
      // Reset options that are not supported by the selected mode
      ...defaults,
    };
    setValue(newValue);
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
        message.success('Configuration has been loaded successfully', 'fa-wrench');
      })
      .catch((reason) => {
        message.error('Failed to read configuration file. Reason: ' + reason, 'fa-warning');
      });
  }

  render() {
    const props = this.props;
    const { enabledLevels, errors, readOnly, setValue, value } = props;

    const inject = {
      errors,
      readOnly,
      setValue,
      value,
    };

    return (
      <div>

        <div>
          <div style={{ position: 'relative' }}>
            <h4 style={{ paddingTop: 6 }}>{value.level === configurationLevels.AUTO ? 'Mode' : 'Profile'}</h4>
            {this.profiles.length !== 0 && enabledLevels.length > 1 &&
              <div>
                <ButtonToolbar style={{ position: 'absolute', right: 0, top: 2 }}>
                  <ButtonGroup data-toggle="buttons" aria-label="First group">
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
                            hidden={readOnly && l.value !== value.level}
                          >
                            <Input type="radio" name="level" id={`config-mode-${l.value}`} onClick={() => this.changeConfigurationLevel(l.value)} />
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
              label="Selected Profile"
              help="Specify a default rules specification profile"
              options={this.profiles}
              clearable={false}
              onChange={(value) => {
                this.changeProfile(value);
              }}
              showLabel={value.level === configurationLevels.ADVANCED}
            />
          }

        </div>

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
