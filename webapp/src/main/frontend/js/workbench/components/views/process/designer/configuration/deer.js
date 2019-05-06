import React from 'react';

import {
  ButtonGroup,
  ButtonToolbar,
  Input,
  Label,
} from 'reactstrap';

import {
  EnumFileSelectMode,
  FileSelectField,
  SelectField,
} from '../../../../helpers/forms/form-fields';

import {
  configurationLevels,
  configurationLevelOptions,
  defaultValues as defaultValuesAdvanced,
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
        ...defaultValuesAdvanced,
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
        const defaultProfileName = this.props.appConfiguration.deer.defaultProfile;
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
    console.log(defaults);
    const newValue = {
      ...value,
      // Reset options that are not supported by the selected mode
      ...defaults,
    };
    setValue(newValue);
  }

  render() {
    const props = this.props;
    const { enabledLevels, errors, readOnly, setValue, value, filesystem } = props;
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
              help="Specify a default specification profile"
              options={this.profiles}
              clearable={false}
              onChange={(value) => {
                this.changeProfile(value);
              }}
              showLabel={value.level === configurationLevels.ADVANCED}
            />
          }

          {value.level === configurationLevels.ADVANCED &&
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

      </div>
    );
  }

}

export default DeerConfiguration;
