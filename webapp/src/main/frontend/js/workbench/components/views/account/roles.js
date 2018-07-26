import * as React from 'react';

import { Roles as RoleNames } from '../../../model';

import { Checkbox } from '../../helpers';

class Roles extends React.Component {

  constructor(props) {
    super(props);
  }

  render() {
    const { roles } = this.props;

    return (
      <div>
        {Object.keys(RoleNames).map((role) =>
          <Checkbox
            key={role}
            id={role}
            text={role}
            value={roles.includes(role)}
            state="success"
            readOnly={false}
            onChange={(checked) => this.props.onChange(checked, role)}
          />
        )}
      </div>
    );
  }
}

export default Roles;
