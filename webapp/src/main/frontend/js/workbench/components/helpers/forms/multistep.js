import React from 'react';
import createWizard from 'react-wiz';
import { Button } from 'reactstrap';

function WizardItem(props) {
  const { id, value, values, next, children, hasPrevious, hasNext, onNextClicked, onPreviousClicked, reset, steps, onComplete, onGoToId, saveTemp, clearTemp } = props;
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
            <Button 
              className="prev" 
              onClick={onPreviousClicked}
              style={{float: 'left'}}
            >
              Previous
            </Button>
            :
            <div />
        }
        <Button
          color="warning"
          className="reset"
          onClick={() => {
            clearTemp();
            setTimeout(reset, 200);
          }}
          style={{float: 'left', marginLeft: hasPrevious ? 20 : 0}}
        >
          Start over
        </Button>
        {
          hasNext ?
            <Button 
              className="next" 
              onClick={() => { 
                const newValues = { ...values }; 
                newValues[id] = value; 

                onNextClicked()
                  .then(() => {
                    saveTemp(next(value), newValues); 
                  })
                  .catch(() => {
                    saveTemp(id, newValues); 
                  });
              }} 
              style={{float: 'right'}}
            >
              Next
            </Button>
            :
            <Button 
              color="primary" 
              className="complete" 
              onClick={() => {
                clearTemp();
                onComplete();
              }} 
              style={{float: 'right'}}
            >
              Submit
            </Button>
        }        
        <br />
      </div>
    </div>
  );
}

const Wizard = createWizard(WizardItem);

Wizard.defaultProps = {
  promiseOnNext: true,
};

export default Wizard;
