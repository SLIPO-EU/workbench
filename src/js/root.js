const React = require('react');
const ReactDOM = require('react-dom');

const Root = require('./components/root.js');

var renderRoot = function (placeholder) 
{
  ReactDOM.render(<Root />, placeholder);
};

module.exports = {renderRoot};
