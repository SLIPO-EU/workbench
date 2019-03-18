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
  copyToClipboard,
} from '../../util';

import {
  message,
} from '../../service';

import {
  checkApplicationName,
  create,
  getAccounts,
  query,
  resetFilters,
  revoke,
  setExpanded,
  setFilter,
  setPager,
  setSelected,
} from '../../ducks/ui/views/application-key';

import {
  Filters,
  ApplicationKeys,
  ApplicationKeyForm,
} from "./application-key";

import {
  Dialog,
} from '../helpers';

const EnumRevokeAction = {
  Revoke: 'Revoke',
  Cancel: 'Cancel',
};

/**
 * Component for managing application keys
 *
 * @class ApplicationKeyViewer
 * @extends {React.Component}
 */
class ApplicationKeyViewer extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      showCreateDialog: false,
      showRevokeDialog: false,
      selected: null,
    };

    props.getAccounts();

    this.revokeDialogHandler = this.revokeDialogHandler.bind(this);
    this.hideRevokeDialog = this.hideRevokeDialog.bind(this);
  }

  componentWillMount() {
    this.search();
  }

  search() {
    this.props.query({
      query: { ...this.props.filters },
    });
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

  showRevokeDialog(key) {
    this.setState({
      showRevokeDialog: true,
      selected: key,
    });
  }

  hideRevokeDialog() {
    this.setState({
      showRevokeDialog: false,
      selected: null,
    });
  }

  create(data) {
    this.props.checkApplicationName(data.applicationName)
      .then(exists => {
        if (exists) {
          message.warn(`An application with name ${data.applicationName} already exists`);
        } else {
          this.props.create(data)
            .then((data) => {
              this.hideCreateDialog();
              this.search();
              const result = copyToClipboard('copy-key', data.key);
              message.infoHtml(
                `The application key has been created successfully.${result ? '<br />The key has been copied to the clipboard.' : ''}`
              );
            })
            .catch(err => {
              message.error('An error has occurred');
            });
        }
      })
      .catch(err => {
        message.error('Validation has failed');
      });
  }

  revokeDialogHandler(action) {
    const { selected: { id } } = this.state;

    switch (action.key) {
      case EnumRevokeAction.Revoke:
        this.props.revoke(id)
          .then(() => {
            this.hideRevokeDialog();
            this.search();
          })
          .catch(() => {
            message.error('Operation has failed');
          });
        break;

      default:
        this.hideRevokeDialog();
        break;

    }
  }

  renderRevokeDialog() {
    const { selected: { name } } = this.state;

    return (
      <Dialog className="modal-dialog-centered"
        header={
          <span>
            <i className={'fa fa-question mr-2'}></i>System Message
          </span>
        }
        modal={this.state.showRevokeDialog}
        handler={this.revokeDialogHandler}
        toggle={this.hideRevokeDialog}
        actions={[
          {
            key: EnumRevokeAction.Revoke,
            label: 'Yes',
            iconClass: 'fa fa-trash',
            color: 'danger',
          }, {
            key: EnumRevokeAction.Cancel,
            label: 'No',
            iconClass: 'fa fa-undo',
          }
        ]}
      >
        <div>The application key <span className="font-weight-bold">{name}</span> will be revoked. Are you sure you want to continue?</div>
      </Dialog>
    );
  }

  render() {
    return (
      <React.Fragment>
        {this.state.showRevokeDialog &&
          this.renderRevokeDialog()
        }
        {this.state.showCreateDialog &&
          <ApplicationKeyForm
            accounts={this.props.accounts}
            create={(data) => this.create(data)}
            hide={() => this.hideCreateDialog()}
            visible={this.state.showCreateDialog}
          />
        }
        <div className="animated fadeIn">
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
                        query={this.props.query}
                        resetFilters={this.props.resetFilters}
                        setFilter={this.props.setFilter}
                      />
                    </Col>
                  </Row>
                </CardBody>
              </Card>
              <Card>
                <CardBody className="card-body">
                  <Row className="mb-2">
                    <Col>
                      <ApplicationKeys
                        expanded={this.props.expanded}
                        filters={this.props.filters}
                        items={this.props.items}
                        pager={this.props.pager}
                        query={this.props.query}
                        revoke={(key) => this.showRevokeDialog(key)}
                        selected={this.props.selected}
                        setExpanded={this.props.setExpanded}
                        setPager={this.props.setPager}
                        setSelected={this.props.setSelected}
                      />
                    </Col>
                  </Row>
                </CardBody>
              </Card>
            </Col>
          </Row >
        </div>
        <textarea id="copy-key"
          style={{
            tabIndex: -1,
            ariaHidden: true,
            position: 'absolute',
            left: -9999
          }}
        >
        </textarea>
      </React.Fragment>
    );
  }
}

const mapStateToProps = (state) => ({
  accounts: state.ui.views.applicationKey.accounts,
  expanded: state.ui.views.applicationKey.expanded,
  filters: state.ui.views.applicationKey.filters,
  items: state.ui.views.applicationKey.items,
  lastUpdate: state.ui.views.applicationKey.lastUpdate,
  loading: state.ui.views.applicationKey.loading,
  pager: state.ui.views.applicationKey.pager,
  selected: state.ui.views.applicationKey.selected,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  checkApplicationName,
  create,
  getAccounts,
  query,
  resetFilters,
  revoke,
  setExpanded,
  setFilter,
  setPager,
  setSelected,
}, dispatch);

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps)(ApplicationKeyViewer);
