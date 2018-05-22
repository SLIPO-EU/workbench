import * as React from 'react';
import * as ReactRedux from 'react-redux';
import * as ReactDOM from 'react-dom';

import store from './store';
import Root from './components/root.js';

var renderRoot = function (placeholder) {
  ReactDOM.render(
    <ReactRedux.Provider store={store}>
      <Root />
    </ReactRedux.Provider>,
    placeholder);
};

export default renderRoot;
