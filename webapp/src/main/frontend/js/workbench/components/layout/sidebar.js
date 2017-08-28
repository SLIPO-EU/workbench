const React = require('react');
const PropTypes = require('prop-types');
const { NavLink } = require('react-router-dom');
const Immutable = require('immutable');

class Sidebar extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      expanded: new Immutable.Set(),
    };
  }

  render() {
    var { location } = this.props;

    var expanded = (p) => (
      location.pathname.indexOf(p) >= 0 || this.state.expanded.has(p)
    );

    var toggle = (p) => {
      var s = this.state.expanded;
      this.setState({ expanded: s.has(p) ? s.remove(p) : s.add(p) });
    };

    return (
      <div className="sidebar">
        <nav className="sidebar-nav">
          <ul className="nav">

            <li className="nav-title">{'Admin'}</li>

            <li className="nav-item">
              <NavLink to={'/dashboard'} className="nav-link" activeClassName="active">
                <i className="fa fa-dashboard"></i>{'Dashboard'}
              </NavLink>
            </li>

            <li className="nav-item">
              <NavLink to={'/scheduler'} className="nav-link" activeClassName="active">
                <i className="fa fa-gears"></i>{'Scheduler'}
              </NavLink>
            </li>

            <li className={'nav-item nav-dropdown ' + (expanded('/examples') ? 'open' : '')}>
              <a className="nav-link nav-dropdown-toggle" onClick={() => (toggle('/examples'), false)}>
                {'Examples'}
              </a>
              <ul className="nav-dropdown-items">
                <li className="nav-item">
                  <NavLink to={'/examples/buttons'} className="nav-link" activeClassName="active">
                    <i className="fa fa-puzzle-piece"></i>{'Buttons'}
                  </NavLink>
                </li>
                <li className="nav-item">
                  <NavLink to={'/examples/tables'} className="nav-link" activeClassName="active">
                    <i className="fa fa-puzzle-piece"></i>{'Tables'}
                  </NavLink>
                </li>
              </ul>
            </li>

            <li className={'nav-item nav-dropdown ' + (expanded('/pages') ? 'open' : '')}>
              <a className="nav-link nav-dropdown-toggle" onClick={() => (toggle('/pages'), false)}>
                {'Pages'}
              </a>
              <ul className="nav-dropdown-items">
                <li className="nav-item">
                  <NavLink to={'/pages/login'} className="nav-link" activeClassName="active">
                    <i className="fa fa-file-text-o"></i>{'Login'}
                  </NavLink>
                </li>
                <li className="nav-item">
                  <NavLink to={'/pages/register'} className="nav-link" activeClassName="active">
                    <i className="fa fa-file-text-o"></i>{'Register'}
                  </NavLink>
                </li>
              </ul>
            </li>

            <li className="nav-title">{'About'}</li>

          </ul>
        </nav>
      </div>
    );
  }
}

Sidebar.propTypes = {
  location: PropTypes.shape({
    pathname: PropTypes.string.isRequired,
  }),
};

module.exports = Sidebar;
