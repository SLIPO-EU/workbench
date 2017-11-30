import * as React from 'react';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';
import { FormattedTime } from 'react-intl';
import {
  TabContent,
  TabPane,
  Nav,
  NavItem,
  NavLink,
} from 'reactstrap';
import classnames from 'classnames';

import {
  EnumDataSource,
  EnumHarvester,
  EnumTool,
  EnumToolboxItemGroup,
  EnumOperation,
} from './constants';

import {
  DataSourceIcons,
  DataSourceTitles,
  HarvesterIcons,
  HarvesterTitles,
  ToolDefaultOperation,
  ToolIcons,
  ToolTitles,
} from './config';
import DataSource from './data-source';
import Operation from './operation';

/**
 * Helper methods
 */

function getToolboxItems(type) {
  let index = 0;
  const items = [];

  if ((type === EnumToolboxItemGroup.All) || (type === EnumToolboxItemGroup.DataSource)) {
    for (let key in EnumDataSource) {
      if (key === EnumDataSource.HARVESTER) {
        continue;
      }
      items.push(<DataSource key={++index} title={DataSourceTitles[key]} source={key} iconClass={DataSourceIcons[key]} />);
    }
  }

  if ((type === EnumToolboxItemGroup.All) || (type === EnumToolboxItemGroup.Harvester)) {
    for (let key in EnumHarvester) {
      items.push(<DataSource key={++index} title={HarvesterTitles[key]} source={EnumDataSource.HARVESTER} iconClass={HarvesterIcons[key]} harvester={key} />);
    }
  }

  if ((type === EnumToolboxItemGroup.All) || (type === EnumToolboxItemGroup.Tools)) {
    for (let key in EnumTool) {
      if (key === EnumTool.CATALOG) {
        continue;
      }
      items.push(<Operation key={++index} title={ToolTitles[key]} tool={key} operation={ToolDefaultOperation[key]} iconClass={ToolIcons[key]} />);
    }
  }

  // Catalog is handled as a special tool component
  if ((type === EnumToolboxItemGroup.All) || (type === EnumToolboxItemGroup.Misc)) {
    items.push(
      <Operation
        key={++index}
        title={ToolTitles[EnumTool.CATALOG]}
        tool={EnumTool.CATALOG}
        operation={ToolDefaultOperation[EnumTool.CATALOG]}
        iconClass={ToolIcons[EnumTool.CATALOG]}
      />);
  }

  return items;
}

/**
 * A connected component for organizing the building blocks of the process
 * designer.
 *
 * @class Toolbox
 * @extends {React.Component}
 */
class Toolbox extends React.Component {

  constructor(props) {
    super(props);

    // Internal state used only by the component
    this.state = {
      // Active tab
      activeTab: '1'
    };

    this._toggle = this._toggle.bind(this);
  }

  /**
   * Toggle selected tab
   *
   * @param {any} tab
   * @memberof Toolbox
   */
  _toggle(tab) {
    if (this.state.activeTab !== tab) {
      this.setState({
        activeTab: tab
      });
    }
  }

  render() {
    return (
      <div>
        <Nav tabs>
          <NavItem>
            <NavLink
              className={classnames({ active: this.state.activeTab === '1' })}
              onClick={() => { this._toggle('1'); }}>
              All
            </NavLink>
          </NavItem>
          <NavItem>
            <NavLink
              className={classnames({ active: this.state.activeTab === '2' })}
              onClick={() => { this._toggle('2'); }}>
              SLIPO Toolkit
            </NavLink>
          </NavItem>
          <NavItem>
            <NavLink
              className={classnames({ active: this.state.activeTab === '3' })}
              onClick={() => { this._toggle('3'); }}>
              Data Sources
            </NavLink>
          </NavItem>
          <NavItem>
            <NavLink
              className={classnames({ active: this.state.activeTab === '4' })}
              onClick={() => { this._toggle('4'); }}>
              Harvesters
            </NavLink>
          </NavItem>
          <NavItem>
            <NavLink
              className={classnames({ active: this.state.activeTab === '5' })}
              onClick={() => { this._toggle('5'); }}>
              Misc
            </NavLink>
          </NavItem>
        </Nav>
        <TabContent activeTab={this.state.activeTab}>
          <TabPane tabId="1">
            {getToolboxItems(EnumToolboxItemGroup.All)}
          </TabPane>
          <TabPane tabId="2">
            {getToolboxItems(EnumToolboxItemGroup.Tools)}
          </TabPane>
          <TabPane tabId="3">
            {getToolboxItems(EnumToolboxItemGroup.DataSource)}
          </TabPane>
          <TabPane tabId="4">
            {getToolboxItems(EnumToolboxItemGroup.Harvester)}
          </TabPane>
          <TabPane tabId="5">
            {getToolboxItems(EnumToolboxItemGroup.Misc)}
          </TabPane>
        </TabContent>
      </div>
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

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(Toolbox);
