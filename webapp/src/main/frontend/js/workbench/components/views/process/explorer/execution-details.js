import * as React from 'react';
import { FormattedTime } from 'react-intl';
import { Card as ReactCard, Row, CardTitle, CardText, CardSubtitle } from 'reactstrap';
import Card from '../../../helpers/card';
import moment from 'moment';

export const JobCardConfig = (r) => ({
  title: r.component,
  items: [{
    value: 'Operation: ',
    label: r.operation,
  }, {
    value: 'Duration: ',
    label: moment.duration(r.completedOn - r.startedOn).humanize(),
  },
  ],
  color: '#ffffff',
  background: '#4682b4',
  footer: r.errorMessage,
});

export default function ExecutionDetails(props) {
  if (props.steps != null) {
    return props.steps.map(step => (
      <div key={step.id}>
        <div style={{ backgroundColor: '#4682b4', borderColor: '#ffffff', }}>
          <ReactCard inverse style={{ marginLeft: 15, marginTop: 5, backgroundColor: '#4682b4', borderColor: '#4682b4' }}>
            <CardTitle>{step.component}</CardTitle>
            <CardSubtitle> Operation: {step.operation} </CardSubtitle>
            <CardText> Duration: {moment.duration(step.completedOn - step.startedOn).humanize()}</CardText>
          </ReactCard>
        </div>
        <hr />
      </div>
    ));
  }
  else {
    return (<div>-</div>);
  }

}
