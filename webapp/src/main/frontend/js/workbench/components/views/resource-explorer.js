import * as React from 'react';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';

import { fetchResources } from '../../ducks/data/resources';
/**
 * Browse and manage resources
 *
 * @class ResourceExplorer
 * @extends {React.Component}
 */
class ResourceExplorer extends React.Component {

  componentWillMount() {
    this.props.fetchResources();
  }

  render() {
    const { resources } = this.props;
    return (
      <div className="animated fadeIn">
        <ul>
          {
            resources.map(resource => (
              <li key={resource.id}>{resource.name}: {resource.description} ({resource.format})</li>
            ))
          }
        </ul>
      </div>
    );
  }

}

const mapStateToProps = (state) => ({
  resources: state.data.resources,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({ fetchResources }, dispatch);

const mergeProps = (stateProps, dispatchProps, ownProps) => {
  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
  };
};

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(ResourceExplorer);
