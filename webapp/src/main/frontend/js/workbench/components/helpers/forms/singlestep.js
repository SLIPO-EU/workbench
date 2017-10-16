import React from 'react';
import createWizard from 'react-wiz';
import { Button } from 'reactstrap';

function WizardItem(props) {
  const { children, onNextClicked, onComplete, onValidationFail = (err) => {} } = props;
  return (
    <div>
      <div className="wizard-child" style={{ minHeight: 340 }}>
        { children }
      </div>
      <Button 
        color="primary" 
        className="complete" 
        onClick={() => onNextClicked().then(onComplete).catch(onValidationFail)} 
        style={{float: 'right'}}
      >
        Submit
      </Button>
      <br />
    </div>
  );
}

const SingleStepWizard = createWizard(WizardItem);

SingleStepWizard.defaultProps = {
  promiseOnNext: true,
};

export default SingleStepWizard;
