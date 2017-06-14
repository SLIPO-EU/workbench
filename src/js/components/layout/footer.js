const React = require('react');

class Footer extends React.Component {
  render() {
    return (
      <footer className="app-footer">
        <span className="float-right">Powered by <a href="http://">CoreUI</a></span>
      </footer>
    );
  }
}

module.exports = Footer;
