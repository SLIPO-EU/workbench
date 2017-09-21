const React = require('react');

class Footer extends React.Component {
  render() {
    return (
      <footer className="app-footer">
        <span className="float-right">Powered by <a target="_blank" href="http://coreui.io/">CoreUI</a></span>
      </footer>
    );
  }
}

module.exports = Footer;
