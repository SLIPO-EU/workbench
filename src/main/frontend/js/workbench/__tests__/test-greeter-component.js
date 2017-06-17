jest.unmock('../components/greeter');

const React = require('react');
const ReactDOM = require('react-dom');
const TestUtils = require('react-dom/test-utils');

const Greeter = require('../components/greeter');

describe('Greeter', () => {

  it('renders normally', () => {
    
    // Render a checkbox with label in the document
    const greeter = TestUtils.renderIntoDocument(
      <Greeter name="Galaxy" />
    );

    const greeterNode = ReactDOM.findDOMNode(greeter);
    expect(greeterNode).toBeTruthy();
  });

});
