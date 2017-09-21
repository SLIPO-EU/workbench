import React from 'react';
import createWizard from 'react-wiz';
import { Button } from 'reactstrap';

import { TextField, SelectField, MultiSelectField, FileDropField } from '../forms/';
import formatFileSize from '../../util/file-size';

const Wizard = createWizard(WizardItem);

function WizardItem(props) {
  const { id, title, description, children, hasPrevious, hasNext, isLast, onNextClicked, onPreviousClicked, reset, errors, completed, step, steps, onComplete } = props;
  return (
    <div>
      <h5>{step.title}</h5>
      <hr />
      { children }
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
         <Button color="warning" className="reset" onClick={reset} style={{float: 'right', marginRight: 10}}>Reset</Button>
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
        label="Input method"
        help="Choose one"
        onChange={(val) => setValue(val)}
        value={value}
        error={errors}
        options={[
          { value: 'resource', label: 'Resource list' },
          { value: 'url', label: 'External url' },
          { value: 'file', label: 'File upload' },
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
function FileUploadStep(props) {
  const { setValue, value, errors } = props;
  return (
    <div>
      <TextField
        id="name"
        label="File name"
        help="Upload file name"
        onChange={(val) => setValue({ ...value, name: val })}
        value={value && value.name}
        error={errors && errors.name}
      />
      
      <TextField
        id="description"
        label="File description"
        help="Upload file description"
        onChange={(val) => setValue({ ...value, description: val })}
        value={value && value.description}
        error={errors && errors.description}
      />
      
      <SelectField
        id="format"
        label="File format"
        help="Upload file format"
        onChange={(val) => setValue({ ...value, format: val })}
        value={value && value.format}
        error={errors && errors.format}
        options={[
          { value: 'JSON' },
          { value: 'XML' },
          { value: 'YAML' },
        ]}
      />
      
      <FileDropField
        id="upload"
        label="Upload file"
        help="Click to select or drop file"
        onChange={(val) => setValue({ ...value, file: val })}
        value={value && value.file}
        error={errors && errors.file}
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
  const { fork, file, resource, url } = props.values;
  return (
    <div>
      <div>
        <ul>
          <li>Input method: {fork.label}</li>
        </ul>
        { 
          fork.value === 'file' ?
            <ul>
              <li>Name: {file.name}</li>
              <li>Description: {file.description}</li>
              <li>Format: {file.format.label}</li>
              <li>File: {file.file.name + ', ' + formatFileSize(file.file.size)}</li>
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
      </div>
    </div>
  );
}

export default class ReactWizard extends React.Component {
  render() {
    return (
      <div className="animated fadeIn" style={{ width: 500 }}>
        <Wizard
          onComplete={(values) => { console.log('completed with', values); alert('completed'); }}
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
          validate={(value) => {}}
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
            name: '',
            description: '',
            format: null,
          }}
          validate={(value) => {
            const errors = {};
            if (!value.file) {
              errors.file = 'File required';
            }
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

