import * as React from 'react';
import * as ReactRedux from 'react-redux';
import * as ReactIntl from 'react-intl';

import { BrowserRouter, Route } from 'react-router-dom';
import { basename } from '../history';

import ContentRoot from './content-root';

//
// Add locale-specific data for each supported locale
//

import en from 'react-intl/locale-data/en';
import de from 'react-intl/locale-data/en';
import el from 'react-intl/locale-data/en';

ReactIntl.addLocaleData(en);
ReactIntl.addLocaleData(de);
ReactIntl.addLocaleData(el);

//
// Define presentational component
//

class Root extends React.Component {

  render() {
    var { locale, messages } = this.props;

    return (
      <ReactIntl.IntlProvider locale={locale} key={locale} messages={messages}>
        <BrowserRouter basename={basename} >
          {/* wrap connected component in a Route to be aware of navigation */}
          <Route path="/" component={ContentRoot} />
        </BrowserRouter>
      </ReactIntl.IntlProvider>
    );
  }
}

Root.defaultProps = {
  locale: 'en-GB',
  messages: {},
};

//
// Wrap into a connected component
//

const mapStateToProps = (state) => {
  var locale = state.i18n.locale;
  var messages = state.i18n.messages[locale];
  return { locale, messages };
};

const mapDispatchToProps = null;

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps)(Root);
