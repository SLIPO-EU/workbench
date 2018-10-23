import * as React from 'react';
import * as ReactRedux from 'react-redux';

import classnames from 'classnames';

import {
  bindActionCreators
} from 'redux';

import {
  ButtonGroup,
  ButtonToolbar,
  Input,
  Label,
} from 'reactstrap';

/**
 * A connected component for rendering execution selected files available to map
 * viewer
 *
 * @class ToolBar
 * @extends {React.Component}
 */
class ToolBar extends React.Component {

  collapse() {
    this.props.setSidebarVisibility(false);
    this.props.setAsideMenuVisibility(false);
  }

  render() {
    return (
      <ButtonToolbar style={{ position: 'absolute', right: 13, top: 4 }}>
        <ButtonGroup data-toggle="buttons" aria-label="First group">
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

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(ToolBar);
