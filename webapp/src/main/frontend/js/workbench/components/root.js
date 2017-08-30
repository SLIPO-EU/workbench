const React = require('react');
const ReactRedux = require('react-redux');
const { BrowserRouter, Route } = require('react-router-dom');
const ReactIntl = require('react-intl');
const { basename, history } = require('../history');

//
// Add locale-specific data for each supported locale
//

ReactIntl.addLocaleData(require('react-intl/locale-data/en'));
ReactIntl.addLocaleData(require('react-intl/locale-data/de'));
ReactIntl.addLocaleData(require('react-intl/locale-data/el'));

//
// Define presentational component
//

const ContentRoot = require('./content-root');

class Root extends React.Component {
  render() {
    var { locale, messages } = this.props;

    return (
      <ReactIntl.IntlProvider locale={locale} key={locale} messages={messages}>
        <BrowserRouter basename={basename} history={history}>
          {/* wrap connected component in a Route to be aware of navigation */}
          <Route path="/" component={ContentRoot} />
        </BrowserRouter>
      </ReactIntl.IntlProvider>
    );
  }
}

Root.defaultProps = {
  locale: 'en',
  messages: {},
};

//
// Wrap into a connected component
//

const mapStateToProps = (state) => {
  var locale = state.locale;
  var messages = state.i18n.messages[locale];
  return { locale, messages };
};

const mapDispatchToProps = null;

Root = ReactRedux.connect(mapStateToProps, mapDispatchToProps)(Root);

module.exports = Root;
