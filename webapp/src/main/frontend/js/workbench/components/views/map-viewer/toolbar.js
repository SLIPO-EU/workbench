import * as React from 'react';
import * as ReactRedux from 'react-redux';

import { withRouter } from 'react-router';

import {
  bindActionCreators
} from 'redux';

import {
  ButtonGroup,
  ButtonToolbar,
  Input,
  Label,
} from 'reactstrap';

import {
  buildPath,
  DynamicRoutes,
} from '../../../model';

/**
 * A connected component for rendering execution selected files available to map
 * viewer
 *
 * @class ToolBar
 * @extends {React.Component}
 */
class ToolBar extends React.Component {

  viewExecution() {
    const { data: { resource }, history } = this.props;
    if (resource) {
      const args = [resource.execution.id, resource.execution.version, resource.execution.execution];
      history.push(buildPath(DynamicRoutes.ProcessExecutionMapViewer, args));
    }
  }

  collapse() {
    this.props.setSidebarVisibility(false);
    this.props.setAsideMenuVisibility(false);
  }

  render() {
    const { data: { resource } } = this.props;

    return (
      <ButtonToolbar style={{ position: 'absolute', right: 13, top: 4 }}>
        <ButtonGroup data-toggle="buttons" aria-label="First group">
          {resource &&
            <Label
              key={'view-execution'}
              htmlFor={'view-execution'}
              className={"btn btn-outline-secondary"}
              check={true}
              style={{ border: 'none', padding: '0.5rem 0.7rem' }}
              title={"View execution"}
            >
              <Input type="radio" name="view-execution" id={'view-execution'} onClick={() => this.viewExecution()} />
              <i className={'fa fa-cog'}></i>
            </Label>
          }
          <Label
            key={'maximize'}
            htmlFor={'maximize'}
            className={"btn btn-outline-secondary"}
            check={true}
            style={{ border: 'none', padding: '0.5rem 0.7rem' }}
            title={"Maximize"}
          >
            <Input type="radio" name="resourceFilter" id={'maximize'} onClick={() => this.collapse()} />
            <i className={'fa fa-expand'}></i>
          </Label>
        </ButtonGroup>
      </ButtonToolbar>
    );
  }

}

const mapStateToProps = (state) => ({
  data: state.ui.views.map.data,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
}, dispatch);

const mergeProps = (stateProps, dispatchProps, ownProps) => {
  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
  };
};

export default withRouter(ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(ToolBar));
