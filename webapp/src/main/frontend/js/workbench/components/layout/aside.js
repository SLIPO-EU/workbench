import * as React from 'react';
import { ContextSidebar } from '../views/';


class Aside extends React.Component {

  render() {
    return (
      <aside className="aside-menu">
        <ContextSidebar />
      </aside>
    );
  }

}

export default Aside;
