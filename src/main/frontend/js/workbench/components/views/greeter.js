var React = require('react');

// Note:
// Extend React.PureComponent to update component based on shallow equality of props+state

class Greeter extends React.PureComponent {
  
  constructor(props)
  {
    super(props);
  }
  
  render() 
  {
    console.info('About to render <Greeter/>...');
    var text = "Hello, " + this.props.name; 
    return (<p>{text}</p>);
  }
}

Greeter.defaultProps = {
  name: 'World',
};

module.exports = Greeter;
