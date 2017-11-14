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
  EnumOperation,
} from './constants';
import DataSource from './data-source';
import Operation from './operation';
import Harvester from './harvester';

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
            <DataSource title={'File System'} source={EnumDataSource.FILESYSTEM} iconClass={'fa fa-file-code-o'} />
            <DataSource title={'External URL'} source={EnumDataSource.EXTERNAL_URL} iconClass={'fa fa-link'} />
            <Harvester title={'OSM'} harvester={EnumHarvester.OSM} iconClass={'fa fa-map-o'} />
            <Operation title={'TripleGeo'} tool={EnumTool.TripleGeo} operation={EnumOperation.Transform} iconClass={'fa fa-cogs'} />
            <Operation title={'LIMES'} tool={EnumTool.LIMES} operation={EnumOperation.Interlink} iconClass={'fa fa-random '} />
            <Operation title={'FAGI'} tool={EnumTool.FAGI} operation={EnumOperation.Fusion} iconClass={'fa fa-object-ungroup'} />
            <Operation title={'DEER'} tool={EnumTool.DEER} operation={EnumOperation.Enrichment} iconClass={'fa fa-tags'} />
            <Operation title={'Register Resource'} tool={EnumTool.CATALOG} operation={EnumOperation.Registration} iconClass={'fa fa-book'} />
          </TabPane>
          <TabPane tabId="2">
            <Operation title={'TripleGeo'} tool={EnumTool.TripleGeo} operation={EnumOperation.Transform} iconClass={'fa fa-cogs'} />
            <Operation title={'LIMES'} tool={EnumTool.LIMES} operation={EnumOperation.Interlink} iconClass={'fa fa-random '} />
            <Operation title={'FAGI'} tool={EnumTool.FAGI} operation={EnumOperation.Fusion} iconClass={'fa fa-object-ungroup'} />
            <Operation title={'DEER'} tool={EnumTool.DEER} operation={EnumOperation.Enrichment} iconClass={'fa fa-tags'} />
          </TabPane>
          <TabPane tabId="3">
            <DataSource title={'File System'} source={EnumDataSource.FILESYSTEM} iconClass={'fa fa-file-code-o'} />
            <DataSource title={'External URL'} source={EnumDataSource.EXTERNAL_URL} iconClass={'fa fa-link'} />
          </TabPane>
          <TabPane tabId="4">
            <Harvester title={'OSM'} harvester={EnumHarvester.OSM} iconClass={'fa fa-map-o'} />
          </TabPane>
          <TabPane tabId="5">
            <Operation title={'Register Resource'} tool={EnumTool.CATALOG} operation={EnumOperation.Registration} iconClass={'fa fa-book'} />
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
