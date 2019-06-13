import React from 'react';
import PropTypes from 'prop-types';

import {
  Modal, ModalHeader, ModalBody
} from 'reactstrap';

import { default as User } from './user';

class AccountForm extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      user: {
        email: '',
        username: '',
        familyName: '',
        givenName: '',
        password: '',
        verifyPassword: '',
        roles: [],
      }
    };
  }

  static propTypes = {
    hide: PropTypes.func.isRequired,
    onSave: PropTypes.func.isRequired,
    visible: PropTypes.bool,
  }

  static defaultProps = {
    visible: false,
  }

  render() {
    const { user } = this.state;

    return (
      <Modal
        centered={true}
        isOpen={this.props.visible}
        toggle={() => this.props.hide()}
        style={{ maxWidth: 500 }}
      >
        <ModalHeader toggle={() => this.props.hide()}>
          Create User
        </ModalHeader>
        <ModalBody style={{ minWidth: 640 }}>
          <User
            mode={User.EditMode.CREATE}
            onSave={(account) => this.props.onSave(account)}
            onCancel={() => this.props.hide()}
            user={user}
          />
        </ModalBody>
      </Modal>
    );
  }
}

export default AccountForm;
