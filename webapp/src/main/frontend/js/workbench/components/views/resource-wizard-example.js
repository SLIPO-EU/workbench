import React from 'react';
import createWizard from 'react-wiz';
import { Button } from 'reactstrap';

import { TextField, SelectField, FileDropField } from '../forms/';
import formatFileSize from '../../util/file-size';

const Wizard = createWizard(WizardItem);

function WizardItem(props) {
  const { id, title, description, children, hasPrevious, hasNext, isLast, onNextClicked, onPreviousClicked, reset, errors, completed, step, onComplete } = props;
  return (
    <div>
      <h3>Step {step}: {title}</h3>
      <h5>{description}</h5>
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
           completed ?
             <Button onClick={reset} style={{ float: 'right' }}>Start over?</Button>
             :
               (
               hasNext ?
                <Button className="next" onClick={onNextClicked} style={{float: 'right'}}>Next</Button>
                :
                  <Button className="complete" onClick={onComplete} style={{float: 'right'}}>Send</Button>
               )
         }
        <br />
      </div>
    </div>
  );
}

function FileUpload(props) {
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

function Confirmation(props) {
  const { resource } = props.values;
  return (
    <div>
      <div>
        <h5>Resource</h5>
        <ul>
          <li> Name: {resource.name}</li>
          <li>Description: {resource.description}</li>
          <li>Format: {resource.format}</li>
          <li>File: {resource.file.name + ', ' + formatFileSize(resource.file.size)}</li>
        </ul>
      </div>
    </div>
  );
}

export default class ReactWizard extends React.Component {
  render() {
    return (
      <div className="animated fadeIn">
        <Wizard
          onComplete={(values) => { console.log('completed with', values); }}
        >
        <FileUpload
          id="resource"
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
        />
        <Confirmation
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

