const React = require('react');
const ReactRedux = require('react-redux');
const PropTypes = require('prop-types');
const { Dropdown, ButtonDropdown, DropdownToggle, DropdownMenu, DropdownItem } = require('reactstrap');
const { FormattedMessage } = require('react-intl');
const _ = require('lodash');

//
// Define presentational component
//

class SelectLanguage extends React.Component {

  constructor(props) {
    super(props);

    this._supportedLanguages = {
      'en': { value: 'en', titleId: 'locales.en', title: 'English' },
      'el': { value: 'el', titleId: 'locales.el', title: 'Greek' },
    };

    this.state = {
      open: false,
    };
  }

  componentWillReceiveProps(nextProps) {
    this.setState({ open: false });
  }

  render() {
    var { language } = this.props;
    var languageInfo = this._supportedLanguages[language];

    return (
      <Dropdown isOpen={this.state.open} toggle={() => this.setState({ open: !this.state.open })}>
        <DropdownToggle caret size="sm">
          <FormattedMessage id={languageInfo.titleId} defaultMessage={languageInfo.title} />
        </DropdownToggle>
        <DropdownMenu>
          {_.values(this._supportedLanguages).map(
            y => (
              <DropdownItem key={language + "/" + y.value} onClick={(ev) => this.props.changeLanguage(y.value)} >
                <FormattedMessage id={y.titleId} defaultMessage={y.title} />
              </DropdownItem>
            )
          )}
        </DropdownMenu>
      </Dropdown>
    );
  }
}

SelectLanguage.defaultProps = {
  language: 'en',
};

SelectLanguage.propTypes = {
  language: PropTypes.string.isRequired,
  changeLanguage: PropTypes.func.isRequired,
};

//
// Wrap into a connected component
//

const { changeLocale } = require('../../actions/i18n');

const mapStateToProps = (state, ownProps) => ({
  language: state.locale,
});

const mapDispatchToProps = (dispatch, ownProps) => ({
  changeLanguage: (language) => dispatch(changeLocale(language)),
});

SelectLanguage = ReactRedux.connect(mapStateToProps, mapDispatchToProps)(SelectLanguage);

module.exports = SelectLanguage;

