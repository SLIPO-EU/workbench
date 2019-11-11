import React from 'react';

import { components } from 'react-select';
import { UncontrolledTooltip, } from 'reactstrap';

const ProfileOption = (props) => {
  return (
    props.data && props.data.comments ? (
      <>
        <components.Option {...props} />
        <UncontrolledTooltip placement="bottom" target={props.innerProps.id} fade={false} delay={0}>
          {props.data.comments}
        </UncontrolledTooltip>
      </>
    ) : (
      <components.Option {...props} />
    )
  );
};

export default ProfileOption;
