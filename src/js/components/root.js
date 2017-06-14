const React = require('react');
const ReactRedux = require('react-redux');
const {HashRouter, Route} = require('react-router-dom');

const history = require('../history');
const store = require('../store');

const ContentRoot = require('./content-root');

class Root extends React.Component 
{
  render() 
  {
    return (
      <ReactRedux.Provider store={store}>
        <HashRouter history={history}>
          {/* wrap connected component in a Route to sensitive to navigation */}
          <Route path="/" component={ContentRoot} />
        </HashRouter>
      </ReactRedux.Provider>
    );
  }
}

module.exports = Root;
