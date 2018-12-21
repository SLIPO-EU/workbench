import React from 'react';

import { Button } from 'reactstrap';

class RevisionHistory extends React.Component {

  constructor(props) {
    super(props);
  }

  render() {
    const { resource, version } = this.props;
    if (!resource) {
      return null;
    }

    return (
      <div style={{ position: 'absolute', bottom: 65, display: 'flex', justifyContent: 'space-evenly', width: '100%', alignItems: 'center' }}>
        <Button outline color="secondary"><i className="fa fa-angle-left " /></Button>
        <div>
          {`${version} / ${resource.revisions.length}`}
        </div>
        <Button outline color="secondary"><i className="fa fa-angle-right" /></Button>
      </div>
    );
  }
}

export default RevisionHistory;
