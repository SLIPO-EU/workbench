import * as React from 'react';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';


import {
  action,
  thunk,
} from '../../ducks/ui/views/harvester-data-explorer';

/**
 * Harvester data explorer
 *
 * @class HarvesterDataExplorer
 * @extends {React.Component}
 */
class HarvesterDataExplorer extends React.Component {

  render() {

    return (
      <div className="animated fadeIn">
        {
          JSON.stringify(this.props.data, null, 2)
        }
      </div>
    );
  }

}

const mapStateToProps = (state) => ({
  data: state.ui.views.harvester.data,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  action,
  thunk,
}, dispatch);

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps)(HarvesterDataExplorer);
