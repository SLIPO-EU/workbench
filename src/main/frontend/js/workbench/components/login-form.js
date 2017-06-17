const React = require('react');
const ReactRedux = require('react-redux');
const PropTypes = require('prop-types');
const {Link, NavLink} = require('react-router-dom');


//
// Presentational component
//

class LoginForm extends React.Component 
{
  constructor(props)
  {
    super(props);

    this._submit = this._submit.bind(this);

    this.state = {
      username: props.username || '',
      password: '',
    };
  }
  
  componentWillReceiveProps(nextProps)
  {
    this.setState({
      username: nextProps.username || '',
      password: '',
    });
  }

  _submit(ev)
  {
    var {username, password} = this.state;
    
    this.props.submit(username, password);
    return false;     
  }

  render() 
  {
    return (
      <div className="app flex-row align-items-center">
        <div className="container">
          <div className="row justify-content-center">
            <div className="col-md-8">
              <div className="card-group mb-0">
                
                <div className="card p-4">
                  <div className="card-block">
                    <h1>{'Login'}</h1>
                    <p className="text-muted">{'Sign into your account'}</p>
                    <div className="input-group mb-3">
                      <span className="input-group-addon"><i className="icon-user"></i></span>
                      <input type="text" className="form-control" placeholder="username"
                        value={this.state.username} 
                        onChange={(ev) => this.setState({username: ev.target.value})}
                       />
                    </div>
                    <div className="input-group mb-4">
                      <span className="input-group-addon"><i className="icon-lock"></i></span>
                      <input type="password" className="form-control" placeholder="password"
                        value={this.state.password} 
                        onChange={(ev) => this.setState({password: ev.target.value})}
                       />
                    </div>
                    <div className="row">
                      <div className="col-6">
                        <button type="button" className="btn btn-primary px-4" onClick={this._submit}>
                          {'Login'}
                        </button>
                      </div>
                      <div className="col-6 text-right">
                        <NavLink className="btn px-0" activeClassName="active" to="/reset-password">
                          {'Forgot password?'}
                        </NavLink>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="card card-inverse card-primary py-5 d-md-down-none">
                  <div className="card-block text-center">
                    <div>
                      <h2>{'Sign up'}</h2>
                      <p>{'Register for a new account'}</p>
                      <NavLink className="btn btn-primary active mt-3" to="/register">
                        {'Register!'}
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

const {login, refreshProfile} = require('../actions/user');
const {navigateTo} = require('../actions/router');

const mapStateToProps = null;

const mapDispatchToProps = (dispatch, ownProps) => ({
  submit: (username, password) => (
    dispatch(login(username, password))
      .then(() => dispatch(refreshProfile()))
      .then(() => dispatch(navigateTo("/")))
      .catch((err) => null) // ignore failed logins
  ),
});

LoginForm = ReactRedux.connect(mapStateToProps, mapDispatchToProps)(LoginForm);

module.exports = LoginForm;
