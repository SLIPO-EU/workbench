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

    const { user: { familyName, givenName, roles } } = props;

    this.state = {
      familyName,
      givenName,
      roles: [...roles],
    };
  }

  static propTypes = {
    user: PropTypes.object.isRequired,
    onSave: PropTypes.func.isRequired,
  }

  onRoleChange(selected, role) {
    const roles = [...this.state.roles.filter((r) => r !== role)];

    if (selected) {
      roles.push(role);
    }
    this.setState({
      roles,
    });
  }

  componentWillReceiveProps(nextProps) {
    const { user: { roles } } = nextProps;

    this.setState({
      roles: [...roles],
    });
  }

  isModified() {
    const { user: { givenName: oldGivenName, familyName: oldFamilyName, roles: oldRoles } } = this.props;
    const { givenName: newGivenName, familyName: newFamilyName, roles: newRoles } = this.state;

    if (oldGivenName !== newGivenName) {
      return true;
    }
    if (oldFamilyName !== newFamilyName) {
      return true;
    }
    return !_.isEqual([...oldRoles].sort(), [...newRoles].sort());
  }

  save(e) {
    e.preventDefault();

    if (!this.isModified()) {
      return;
    }

    const { user: { id } } = this.props;
    const { givenName, familyName, roles } = this.state;

    this.props.onSave({
      id,
      givenName,
      familyName,
      roles,
    });
  }

  cancel(e) {
    e.preventDefault();

    const { user: { familyName, givenName, roles } } = this.props;

    this.setState({
      familyName,
      givenName,
      roles: [...roles],
    });
  }

  render() {
    const { user } = this.props;

    return (
      <div>
        <Row>
          <Col>
            <Row>
              <Col>
                <TextField
                  id="username"
                  label="Account"
                  value={user.username}
                  readOnly={true}
                />
              </Col>
              <Col>
                <TextField
                  id="email"
                  label="Email"
                  value={user.email}
                  readOnly={true}
                />
              </Col>
            </Row>
            <Row>
              <Col>
                <TextField
                  id="givenName"
                  label="First Name"
                  value={this.state.givenName}
                  readOnly={false}
                  onChange={(value) => this.setState({ givenName: value })}
                />
              </Col>
              <Col>
                <TextField
                  id="familyName"
                  label="Last Name"
                  value={this.state.familyName}
                  readOnly={false}
                  onChange={(value) => this.setState({ familyName: value })}
                />
              </Col>
            </Row>
          </Col>
          <Col>
            <Row>
              <Col>
                <FormGroup>
                  <Label for="roles">Roles</Label>
                  <Roles
                    roles={this.state.roles}
                    onChange={(selected, role) => this.onRoleChange(selected, role)}
                  />
                </FormGroup>
              </Col>
            </Row>
          </Col>
        </Row>
        {this.isModified() &&
          <Row className="float-right">
            <Col>
              <Button
                color="primary"
                className="ml-1 mb-1"
                onClick={(e) => this.save(e)}>
                <i className="fa fa-save mr-2" /> Save
              </Button>
              <Button
                color="danger"
                className="ml-1 mb-1"
                onClick={(e) => this.cancel(e)}>
                <i className="fa fa-trash mr-2" /> Cancel
              </Button>
            </Col>
          </Row>
        }
      </div>
    );
  }
}

export default UserDetails;
