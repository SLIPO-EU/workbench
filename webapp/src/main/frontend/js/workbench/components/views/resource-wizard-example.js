import React from 'react';
import createWizard from 'react-wiz';
import { Button } from 'reactstrap';
import { toast } from 'react-toastify';

import { TextField, SelectField, MultiSelectField, FileDropField } from '../forms/';
import formatFileSize from '../../util/file-size';
import validateUrl from '../../util/validate-url';

const Wizard = createWizard(WizardItem);

function WizardItem(props) {
  const { id, title, description, children, hasPrevious, hasNext, isLast, onNextClicked, onPreviousClicked, reset, errors, completed, step, steps, onComplete, onGoToId } = props;
  return (
    <div>
      {
        steps.map((s) => (
          <Button
            key={s.id}
            color="link"
            disabled={!(s.cleared || s.active)}
            onClick={() => onGoToId(s.id)}
            size={s.active ? 'lg' : 'sm'}
          >
            Step {s.index+1}: {s.title}
          </Button>
        ))
      }
      <hr />
      <div className="wizard-child" style={{ minHeight: 340 }}>
        { children }
      </div>
      <div>
        { 
          hasPrevious ? 
            <Button className="prev" onClick={onPreviousClicked} style={{float: 'left'}}>Previous</Button>
            :
            <div />
        }
        {
          hasNext ?
            <Button className="next" onClick={onNextClicked} style={{float: 'right'}}>Next</Button>
            :
            <Button color="primary" className="complete" onClick={onComplete} style={{float: 'right'}}>Submit</Button>
        }
        <Button color="warning" className="reset" onClick={reset} style={{float: 'right', marginRight: 10}}>Start over</Button>
        <br />
      </div>
    </div>
  );
}

function ForkStep(props) {
  const { setValue, value, errors } = props;
  return (
    <div>
      <SelectField
        id="input"
        label="Data source"
        help="Choose one"
        onChange={(val) => setValue(val)}
        value={value}
        error={errors}
        options={[
          { value: 'file', label: 'File upload' },
          { value: 'resource', label: 'Resource list' },
          { value: 'url', label: 'External url' },
          { value: 'harvester', label: 'Harvester' },
        ]}
      />
    </div>
  );
}

function UrlSelectStep(props) {
  const { setValue, value, errors } = props;
  return (
    <div>
      <TextField
        id="url"
        label="Resource url"
        help="Enter an external url"
        onChange={(val) => setValue({ ...value, url: val })}
        value={value && value.url}
        error={errors && errors.url}
      />
    </div>
  );
}

function HarvesterSelectStep(props) {
  const { setValue, value, errors } = props;
  return (
    <div>
      <TextField
        id="url"
        label="Harvester url"
        help="Enter a url"
        onChange={(val) => setValue({ ...value, url: val })}
        value={value && value.url}
        error={errors && errors.url}
      />
      <SelectField
        id="type"
        label="Harvester type"
        help="Choose harvester type to determine config options"
        onChange={(val) => setValue({ ...value, type: val })}
        value={value && value.type}
        error={errors && errors.type}
        options={[
          { value: 'OSM', label: 'OSM Harvester' },
          { value: 'X', label: 'Harvester X' },
        ]}
      />
    </div>
  );
}

function HarvesterConfigStep(props) {
  const { setValue, value, values, errors } = props;
  const { type } = values.harvester;
  if (type.value === 'OSM') {
    return (
      <div>
        <TextField
          id="option1"
          label="OSM option1"
          help=""
          onChange={(val) => setValue({ ...value, option1: val })}
          value={value && value.option1}
          error={errors && errors.option1}
        />
        <TextField
          id="option2"
          label="OSM option2"
          help=""
          onChange={(val) => setValue({ ...value, option2: val })}
          value={value && value.option2}
          error={errors && errors.option2}
        />
      </div>
    );
  } else if (type.value === 'X') {
    return (
      <div>
        <TextField
          id="option1"
          label="X option1"
          help=""
          onChange={(val) => setValue({ ...value, option1: val })}
          value={value && value.option1}
          error={errors && errors.option1}
        />
        <TextField
          id="option2"
          label="X option2"
          help=""
          onChange={(val) => setValue({ ...value, option2: val })}
          value={value && value.option2}
          error={errors && errors.option2}
        />
      </div>
    );
  }
  return null;
}

class FileUploadStep extends React.Component {
  constructor() {
    super();
  }
  componentWillReceiveProps(nextProps) {
    if (nextProps.value && nextProps.value.file && nextProps.value.file.name && !this.props.value.name) {
      this.props.setValue({ ...this.props.value, ...nextProps.value, name: nextProps.value.file.name });
    }
  }
  render() {
    const { setValue, value, errors } = this.props;
    return (
      <div>
        <FileDropField
          id="upload"
          label="Upload file"
          help="Click to select or drop file"
          onChange={(val) => setValue({ ...value, file: val })}
          value={value && value.file}
          error={errors && errors.file}
        />
        <TextField
          id="name"
          label="Alias"
          help="File alias"
          onChange={(val) => setValue({ ...value, name: val })}
          value={value && value.name}
          error={errors && errors.name}
        />
      </div>
    );
  }
}

function ResourceMetadataStep(props) {
  const { setValue, value, errors } = props;
  return (
    <div>
      <TextField
        id="name"
        label="Resource name"
        help="Resource name"
        onChange={(val) => setValue({ ...value, name: val })}
        value={value && value.name}
        error={errors && errors.name}
      />
      <TextField
        id="description"
        label="Resource description"
        help="Resource description"
        onChange={(val) => setValue({ ...value, description: val })}
        value={value && value.description}
        error={errors && errors.description}
      />
      <SelectField
        id="format"
        label="Resource type"
        help="Upload file format"
        onChange={(val) => setValue({ ...value, format: val })}
        value={value && value.format}
        error={errors && errors.format}
        options={[
          { value: 'SHAPEFILE' },
          { value: 'CSV' },
          { value: 'GPX' },
          { value: 'GEOJSON' },
          { value: 'OSM' },
        ]}
      /> 
    </div>
  );
}

function ResourceSelectStep(props) {
  const { setValue, value, errors } = props;
  return (
    <div>
      <MultiSelectField
        id="resource"
        label="Resources"
        help="Select resources from list"
        onChange={(val) => setValue({ ...value, resources: val })}
        value={value && value.resources}
        error={errors && errors.resources}
        options={[
          { value: 'Resource 1', },
          { value: 'Resource 2', },
          { value: 'Resource 3', },
        ]}
      />
    </div>
  );
}

function ConfirmationStep(props) {
  const { fork, file, resource, url, metadata, harvester, harvesterConfig } = props.values;
  return (
    <div>
      <div>
        <ul>
          <li>Input method: {fork.label}</li>
        </ul>
        { 
          fork.value === 'file' ?
            <ul>
              <li>Name: {metadata.name}</li>
              <li>Description: {metadata.description}</li>
              <li>Format: {metadata.format.label}</li>
              <li>File: {file.file.name + ', ' + formatFileSize(file.file.size)}</li>
              <li>File alias: {file.name}</li>
            </ul>
            : null
        }
        {
          fork.value === 'resource' ?
            <ul>
              <li>Resources: {resource.resources.map(r => r.label).join(', ')}</li>
            </ul>
            : null
        }
        {
          fork.value === 'url' ?
            <ul>
              <li>Url: {url.url}</li>
            </ul>
            : null
        }
        {
          fork.value === 'harvester' ?
            <ul>
              <li>Type: {harvester.type.label}</li>
              <li>Url: {harvester.url}</li>
              <li>Option 1: {harvesterConfig.option1}</li>
              <li>Option 2: {harvesterConfig.option2}</li>
            </ul>
            : null
        }
      </div>
    </div>
  );
}

export default class ReactWizard extends React.Component {
  render() {
    return (
      <div className="animated fadeIn">
        <Wizard
          onComplete={(values) => { 
            console.log('completed with', values);
            toast.dismiss();
            toast.success(<span>Resource registration succeeded!</span>);
          }}
        >
          <ForkStep
            id="fork"
            title="Input mode"
            initialValue={{ value: 'resource', label: 'Resource list' }}
            next={value => value.value} 
          />
          <UrlSelectStep
            id="url"
            title="Select external url"
            initialValue={{
              url: null,
            }}
            validate={(value) => validateUrl(value.url)
              .catch((err) => { throw { url: err }; })
            }
            next={() => 'confirm'}
          />
          <ResourceSelectStep
            id="resource"
            title="Select resource"
            initialValue={{
              resources: []
            }}
            validate={(value) => {
              const errors = {};
              if (!value.resources || (value.resources && !value.resources.length)) {
                errors.resources = 'Select at least 1 resource';
              }
              if (Object.keys(errors).length) throw errors;
            }}
            next={() => 'confirm'}
          />
          <FileUploadStep
            id="file"
            title="Upload resource"
            description=""
            initialValue={{
              file: null,
              name: null,
              description: null,
            }}
            validate={(value) => {
              const errors = {};
              if (!value.file) {
                errors.file = 'File required';
              }
              if (!value.name) {
                errors.name = 'File name required';
              }
              
              if (Object.keys(errors).length) {
                throw errors;
              }
            }}
            next={() => 'metadata'}
          />
          <ResourceMetadataStep
            id="metadata"
            title="Resource metadata"
            description=""
            initialValue={{
              name: '',
              description: '',
              format: null,
            }}
            validate={(value) => {
              const errors = {};

              if (!value.name) {
                errors.name = 'File name required';
              }
              if (!value.format) {
                errors.format = 'Format required';
              }
              if (!value.description || value.description.length < 5) {
                errors.description = 'Description should be longer than 5 characters';
              }

              if (Object.keys(errors).length) {
                throw errors;
              }
            }}
            next={() => 'confirm'}
          />
          <HarvesterSelectStep
            id="harvester"
            title="Harvester"
            description=""
            initialValue={{
              url: null,
            }}
            validate={(value) => {
              const errors = {};
              if (!value.type) {
                errors.type = 'Type required';
              }
              return validateUrl(value.url)
                .then(() => { if (Object.keys(errors).length) { throw errors; }})
                .catch((err) => { errors.url = err; throw errors;});
            }}
            next={() => 'harvesterConfig'}
          />
          <HarvesterConfigStep
            id="harvesterConfig"
            title="Harvester Configuration"
            initialValue={{ }}
            validate={(value) => {

            }}
            next={() => 'confirm'}
          />

          <ConfirmationStep
            id="confirm"
            title="Confirm"
            description="Please confirm"
            initialValue={{}}
          />
        </Wizard>
      </div>
    );
  }
}

