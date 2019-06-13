import * as React from 'react';
import * as ReactRedux from 'react-redux';

import {
  bindActionCreators
} from 'redux';

import {
  Card,
  CardBody,
  Col,
  Row,
} from 'reactstrap';

import {
  FormattedTime
} from 'react-intl';

import {
  UPDATE_INTERVAL_SECONDS,
} from '../../model';

import {
  createAccount,
  fetchAccounts,
  resetFilters,
  setFilter,
  setPager,
  setSelected,
  updateAccount,
} from '../../ducks/ui/views/account';

import {
  AccountForm,
  Filters,
  User,
  Users,
} from "./account";

import {
  message,
} from '../../service';

/**
 * Browse and manage user accounts
 *
 * @class UserManager
 * @extends {React.Component}
 */
class UserManager extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      showCreateDialog: false,
    };

    this.setSelected = this.setSelected.bind(this);

    this.refreshIntervalId = null;
  }

  componentWillMount() {
    this.refreshIntervalId = setInterval(() => {
      this.search();
    }, UPDATE_INTERVAL_SECONDS * 1000);

    this.search();
  }

  componentWillUnmount() {
    if (this.refreshIntervalId) {
      clearInterval(this.refreshIntervalId);
      this.refreshIntervalId = null;
    }
  }

  search() {
    this.props.fetchAccounts({
      query: { ...this.props.filters },
    });
  }

  setSelected(id) {
    this.props.setSelected(id);
  }

  showCreateDialog() {
    this.setState({
      showCreateDialog: true,
    });
  }

  hideCreateDialog() {
    this.setState({
      showCreateDialog: false,
    });
  }

  createAccount(data) {
    this.props.createAccount(data)
      .then(() => {
        this.hideCreateDialog();
        this.search();
        message.infoHtml(
          `The account has been created successfully.`
        );
      })
      .catch((err) => {
        const reason = err.errors.map((e) => e.description + '<br>').join('');
        message.errorHtml(`Account creation has failed. Reason${err.errors.length > 1 ? 's' : ''}:<br>${reason} `, 'fa-user');
      });
  }

  updateAccount(account) {
    this.props.updateAccount(account)
      .then(() => {
        message.success('Account has been updated successfully', 'fa-user');
        this.search();
      })
      .catch((err) => {
        const reason = err.errors.map((e) => e.description + '<br>').join('');
        message.errorHtml(`Account update has failed. Reason${err.errors.length > 1 ? 's' : ''}:<br>${reason} `, 'fa-user');
      });
  }

  render() {
    const { items, selected } = this.props;
    const selectedUser = selected ? items.find((e) => e.id === selected.id) : null;

    return (
      <div className="animated fadeIn">
        {this.state.showCreateDialog &&
          <AccountForm
            onSave={(account) => this.createAccount(account)}
            hide={() => this.hideCreateDialog()}
            visible={this.state.showCreateDialog}
          />
        }
        <Row>
          <Col className="col-12">
            <Card>
              <CardBody className="card-body">
                {this.props.lastUpdate &&
                  <Row className="mb-2">
                    <Col >
                      <div className="small text-muted">
                        Last Update: <FormattedTime value={this.props.lastUpdate} day='numeric' month='numeric' year='numeric' />
                      </div>
                    </Col>
                  </Row>
                }
                <Row>
                  <Col>
                    <Filters
                      create={() => this.showCreateDialog()}
                      filters={this.props.filters}
                      setFilter={this.props.setFilter}
                      resetFilters={this.props.resetFilters}
                      fetchAccounts={this.props.fetchAccounts}
                    />
                  </Col>
                </Row>
              </CardBody>
            </Card>
            <Card>
              <CardBody className="card-body">
                <Row className="mb-2">
                  <Col>
                    <Users
                      fetchAccounts={this.props.fetchAccounts}
                      filters={this.props.filters}
                      items={this.props.items}
                      pager={this.props.pager}
                      selected={this.props.selected}
                      setPager={this.props.setPager}
                      setSelected={this.setSelected}
                    />
                  </Col>
                </Row>
              </CardBody>
            </Card>
          </Col>
        </Row >
        {selectedUser && !this.state.showCreateDialog &&
          <Row>
            <Col className="col-12">
              <Card>
                <CardBody className="card-body">
                  <User
                    mode={User.EditMode.UPDATE}
                    onSave={(account) => this.updateAccount(account)}
                    user={selectedUser}
                  />
                </CardBody>
              </Card>
            </Col>
          </Row>
        }
      </div>
    );
  }

}

const mapStateToProps = (state) => ({
  filters: state.ui.views.account.filters,
  items: state.ui.views.account.items,
  lastUpdate: state.ui.views.account.lastUpdate,
  loading: state.ui.views.account.loading,
  pager: state.ui.views.account.pager,
  selected: state.ui.views.account.selected,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  createAccount,
  fetchAccounts,
  resetFilters,
  setFilter,
  setPager,
  setSelected,
  updateAccount
}, dispatch);

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps)(UserManager);
