import React from 'react';
import PropTypes from 'prop-types';

import {
  Button, Modal, ModalHeader, ModalBody, ModalFooter, Row, Col,
} from 'reactstrap';

import {
  message,
} from '../../../service';

import {
  SelectField,
  TextField,
} from '../../helpers/forms/fields';

class ApplicationKeyForm extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      applicationName: '',
      mappedAccount: null,
    };
  }

  static propTypes = {
    className: PropTypes.string,
    hide: PropTypes.func.isRequired,
    visible: PropTypes.bool,
  }

  static defaultProps = {
    className: '',
    visible: false,
  }

  save() {
    const { applicationName, mappedAccount } = this.state;

    if (applicationName && mappedAccount) {
      this.props.create({
        applicationName,
        mappedAccount,
      });
    }
  }

  cancel() {
    this.props.hide();
  }

  render() {
    const { applicationName, mappedAccount } = this.state;
    const { accounts } = this.props;

    const accountOptions = accounts.map(a => ({ value: a.id, label: [a.familyName, a.givenName, `(${a.username})`].join(' ') }));

    return (
      <Modal
        centered={true}
        isOpen={this.props.visible}
        toggle={() => this.props.hide()}
        className={this.props.className}
        style={{ maxWidth: 500 }}
      >
        <ModalHeader toggle={() => this.props.hide()}>
          Create Application Key
        </ModalHeader>
        <ModalBody style={{ minWidth: 640 }}>
          <Row>
            <Col>
              <TextField
                id="applicationName"
                label="Application Name"
                value={applicationName}
                readOnly={false}
                onChange={(value) => this.setState({ applicationName: value })}
                error={applicationName ? null : 'Required'}
                help="Application unique name"
              />
            </Col>
          </Row>
          <Row>
            <Col>
              <SelectField
                id="mappedAccount"
                label="Mapped Account"
                value={mappedAccount}
                readOnly={false}
                onChange={(value) => this.setState({ mappedAccount: value })}
                options={accountOptions}
                error={mappedAccount ? null : 'Required'}
                help="Specify the account to which the new application key will be mapped to"
              />
            </Col>
          </Row>
        </ModalBody>
        <ModalFooter>
          <React.Fragment>
            <Button color="primary" onClick={() => this.save()} disabled={!applicationName || !mappedAccount}>Create</Button>
            <Button color="danger" onClick={() => this.cancel()}>Cancel</Button>
          </React.Fragment>
        </ModalFooter>
      </Modal>
    );
  }
}

export default ApplicationKeyForm;
