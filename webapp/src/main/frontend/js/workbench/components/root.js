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
import de from 'react-intl/locale-data/de';
import el from 'react-intl/locale-data/el';

ReactIntl.addLocaleData(en);
ReactIntl.addLocaleData(de);
ReactIntl.addLocaleData(el);

//
// Define presentational component
//

class Root extends React.Component {

  static defaultProps = {
    locale: 'en-GB',
    messages: {},
  }

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
