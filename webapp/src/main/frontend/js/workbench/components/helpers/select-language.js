import _ from 'lodash';
import * as React from 'react';
import * as ReactRedux from 'react-redux';
import * as PropTypes from 'prop-types';

import { Dropdown, DropdownToggle, DropdownMenu, DropdownItem } from 'reactstrap';
import { FormattedMessage } from 'react-intl';

import { changeLocale } from '../../ducks/i18n';

//
// Define presentational component
//

class SelectLanguage extends React.Component {

  constructor(props) {
    super(props);

    this._supportedLanguages = {
      'en-GB': { value: 'en-GB', titleId: 'locales.en', title: 'English' },
      'el': { value: 'el', titleId: 'locales.el', title: 'Greek' },
    };

    this.state = {
      open: false,
    };
  }

  static propTypes = {
    language: PropTypes.string.isRequired,
    changeLanguage: PropTypes.func.isRequired,
  }

  static defaultProps = {
    language: 'en-GB',
  }

  componentWillReceiveProps() {
    this.setState({ open: false });
  }

  render() {
    var { language } = this.props;
    var languageInfo = this._supportedLanguages[language];

    return (
      <Dropdown isOpen={this.state.open} toggle={() => this.setState({ open: !this.state.open })}>
        <DropdownToggle caret size="sm" className="no-outline">
          <FormattedMessage id={languageInfo.titleId} defaultMessage={languageInfo.title} />
        </DropdownToggle>
        <DropdownMenu>
          {_.values(this._supportedLanguages).map(
            y => (
              <DropdownItem key={language + "/" + y.value} onClick={() => this.props.changeLanguage(y.value)} >
                <FormattedMessage id={y.titleId} defaultMessage={y.title} />
              </DropdownItem>
            )
          )}
        </DropdownMenu>
      </Dropdown>
    );
  }
}

//
// Wrap into a connected component
//

const mapStateToProps = (state) => ({
  language: state.i18n.locale,
});

const mapDispatchToProps = (dispatch) => ({
  changeLanguage: (language) => dispatch(changeLocale(language)),
});

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps)(SelectLanguage);
