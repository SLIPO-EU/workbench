import * as React from 'react';
import * as PropTypes from 'prop-types';
import _ from 'lodash';

import {
  Button,
  Col,
  FormGroup,
  Label,
  Row,
} from 'reactstrap';


import {
  TextField,
} from '../../helpers/forms/fields';

import {
  default as Roles
} from './roles';

class UserDetails extends React.Component {

  constructor(props) {
    super(props);

    this.state = this.getStateFromProps(props);
  }

  static EditMode = {
    CREATE: 'Create',
    UPDATE: 'Update',
  };

  static propTypes = {
    user: PropTypes.object.isRequired,
    onSave: PropTypes.func.isRequired,
  }

  onRoleChange(selected, role) {
    const { current } = this.state;
    const roles = [...current.roles.filter((r) => r !== role)];

    if (selected) {
      roles.push(role);
    }
    this.setState({
      current: {
        ...current,
        roles,
      },
    });
  }

  componentWillReceiveProps(nextProps) {
    this.setState(this.getStateFromProps(nextProps));
  }

  getStateFromProps(props) {
    const { user: { email, username, familyName, givenName, password = '', roles = [] } } = props;

    return {
      initial: {
        email,
        username,
        familyName,
        givenName,
        password,
        verifyPassword: password,
        roles: [...roles],
      },
      current: {
        email,
        username,
        familyName,
        givenName,
        password,
        verifyPassword: password,
        roles: [...roles],
      },
    };
  }

  isModified() {
    const { mode } = this.props;
    const { initial, current } = this.state;

    if (mode === UserDetails.EditMode.CREATE) {
      if (initial.username !== current.username) {
        return true;
      }
      if (initial.email !== current.email) {
        return true;
      }
      if (initial.password !== current.password) {
        return true;
      }
      if (initial.verifyPassword !== current.verifyPassword) {
        return true;
      }
    }
    if (initial.givenName !== current.givenName) {
      return true;
    }
    if (initial.familyName !== current.familyName) {
      return true;
    }

    return !_.isEqual([...initial.roles].sort(), [...current.roles].sort());
  }

  save(e) {
    e.preventDefault();

    if (!this.isModified()) {
      return;
    }

    const { mode, user: { id = null } } = this.props;
    const { email, username, givenName, familyName, password, roles } = this.state.current;

    if (mode === UserDetails.EditMode.CREATE) {
      this.props.onSave({
        email,
        username,
        givenName,
        familyName,
        password,
        roles,
      });
    } else {
      this.props.onSave({
        id,
        givenName,
        familyName,
        roles,
      });
    }
  }

  cancel(e) {
    e.preventDefault();

    this.setState(this.getStateFromProps(this.props));

    if (typeof this.props.onCancel === 'function') {
      this.props.onCancel();
    }
  }

  render() {
    const { mode } = this.props;
    const { current: user } = this.state;

    return (
      <div>
        <Row>
          <Col>
            <Row>
              {mode === UserDetails.EditMode.UPDATE &&
                < Col >
                  <TextField
                    id="form-username"
                    label="Account"
                    value={user.username}
                    readOnly={true}
                    autoComplete="new-password"
                  />
                </Col>
              }
              <Col>
                <TextField
                  id="email"
                  label="Email"
                  value={user.email}
                  readOnly={mode === UserDetails.EditMode.UPDATE}
                  error={user.email ? null : 'Required'}
                  onChange={(value) => this.setState({
                    current: {
                      ...user,
                      email: value,
                      username: value,
                    }
                  })}
                  autoComplete="new-password"
                />
              </Col>
            </Row>
            <Row>
              <Col>
                <TextField
                  id="givenName"
                  label="First Name"
                  value={user.givenName}
                  readOnly={false}
                  error={user.givenName ? null : 'Required'}
                  onChange={(value) => this.setState({
                    current: {
                      ...user,
                      givenName: value,
                    }
                  })}
                />
              </Col>
              <Col>
                <TextField
                  id="familyName"
                  label="Last Name"
                  value={user.familyName}
                  readOnly={false}
                  error={user.familyName ? null : 'Required'}
                  onChange={(value) => this.setState({
                    current: {
                      ...user,
                      familyName: value,
                    }
                  })}
                />
              </Col>
            </Row>
            {mode === UserDetails.EditMode.CREATE &&
              <Row>
                <Col>
                  <TextField
                    id="password"
                    label="Password"
                    value={user.password}
                    readOnly={false}
                    error={user.password ? null : 'Required'}
                    onChange={(value) => this.setState({
                      current: {
                        ...user,
                        password: value,
                      }
                    })}
                    type="password"
                    autoComplete="new-password"
                  />
                </Col>
                <Col>
                  <TextField
                    id="verifyPassword"
                    label="Verify Password"
                    value={user.verifyPassword}
                    readOnly={false}
                    error={user.password !== user.verifyPassword ? 'Password does not match' : null}
                    onChange={(value) => this.setState({
                      current: {
                        ...user,
                        verifyPassword: value,
                      }
                    })}
                    type="password"
                    autoComplete="new-password"
                  />
                </Col>
              </Row>
            }
          </Col>
          <Col sm="3">
            <Row>
              <Col>
                <FormGroup>
                  <Label for="roles">Roles</Label>
                  <Roles
                    roles={user.roles}
                    onChange={(selected, role) => this.onRoleChange(selected, role)}
                  />
                </FormGroup>
              </Col>
            </Row>
          </Col>
        </Row>
        <Row className="float-right">
          <Col>
            <Button
              color="primary"
              className="ml-1 mb-1"
              disabled={!this.isModified()}
              onClick={(e) => this.save(e)}>
              <i className="fa fa-save mr-2" /> Save
              </Button>
            <Button
              color="danger"
              className="ml-1 mb-1"
              disabled={mode !== UserDetails.EditMode.CREATE && !this.isModified()}
              onClick={(e) => this.cancel(e)}>
              <i className="fa fa-trash mr-2" /> Cancel
              </Button>
          </Col>
        </Row>
      </div>
    );
  }
}

export default UserDetails;
