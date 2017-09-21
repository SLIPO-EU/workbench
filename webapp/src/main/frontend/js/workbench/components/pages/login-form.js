const React = require('react');
const ReactRedux = require('react-redux');
const PropTypes = require('prop-types');
const { NavLink } = require('react-router-dom');
const { FormattedMessage } = require('react-intl');

import { toast } from 'react-toastify';

import { Pages } from '../../model/routes';

//
// Presentational component
//

class LoginForm extends React.Component {
  constructor(props) {
    super(props);

    this._submit = this._submit.bind(this);

    this.state = {
      username: props.username || '',
      password: '',
    };
  }

  componentWillReceiveProps(nextProps) {
    this.setState({
      username: nextProps.username || '',
      password: '',
    });
  }

  _submit($event) {
    $event.preventDefault();

    let { username, password } = this.state;

    this.props.submit(username, password);
  }

  render() {
    return (
      <div className="app flex-row align-items-center">
        <div className="container">
          <div className="row justify-content-center">
            <div className="col-md-8">
              <div className="card-group mb-0">

                <div className="card p-4">
                  <form className="card-block" onSubmit={this._submit}>

                    <h1><FormattedMessage id="login.title" defaultMessage="Sign in" /></h1>
                    <p className="text-muted">
                      <FormattedMessage id="login.subtitle" defaultMessage="Sign-in into your account" />
                    </p>

                    <div className="input-group mb-3">
                      <span className="input-group-addon"><i className="icon-user"></i></span>
                      <input type="text" className="form-control" placeholder="username"
                        value={this.state.username}
                        onChange={(ev) => this.setState({ username: ev.target.value })}
                      />
                    </div>

                    <div className="input-group mb-4">
                      <span className="input-group-addon"><i className="icon-lock"></i></span>
                      <input type="password" className="form-control" placeholder="password"
                        value={this.state.password}
                        onChange={(ev) => this.setState({ password: ev.target.value })}
                      />
                    </div>

                    <div className="row">
                      <div className="col-6">
                        <button type="submit" className="btn btn-primary px-4">
                          <FormattedMessage id="login.login" defaultMessage="Login" />
                        </button>
                      </div>
                      <div className="col-6 text-right">
                        <NavLink className="btn px-0" activeClassName="active" to={Pages.ResetPassword}>
                          <FormattedMessage id="login.forgot-password" defaultMessage="Forgot password?" />
                        </NavLink>
                      </div>
                    </div>
                  </form>
                </div>

                <div className="card card-inverse card-primary py-5 d-md-down-none">
                  <div className="card-block text-center">
                    <div>
                      <h2><FormattedMessage id="register.title" defaultMessage="Sign up" /></h2>
                      <p>
                        <FormattedMessage id="register.subtitle" defaultMessage="Register for a new account" />
                      </p>
                      <NavLink className="btn btn-primary active mt-3" to={Pages.Register}>
                        <FormattedMessage id="register.register" defaultMessage="Register!" />
                      </NavLink>
                    </div>
                  </div>
                </div>

              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

LoginForm.propTypes = {
  submit: PropTypes.func.isRequired,
  username: PropTypes.string,
};

//
// Container component
//

const { login, refreshProfile } = require('../../ducks/user');
const { push: navigateTo } = require('react-router-redux');

const mapStateToProps = null;

const mapDispatchToProps = (dispatch) => ({
  submit: (username, password) => (
    dispatch(login(username, password))
      .then(() => dispatch(refreshProfile()))
      .then(() => toast.dismiss(),
      () => {
        toast.dismiss();
        toast.error(<FormattedMessage id="login.failure" defaultMessage="The username or password is incorrect." />);
      })
      .catch((err) => null)
  ),
});

LoginForm = ReactRedux.connect(mapStateToProps, mapDispatchToProps)(LoginForm);

module.exports = LoginForm;
