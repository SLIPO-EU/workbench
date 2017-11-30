
import React from 'react';
import PropTypes from 'prop-types';
import { Button, Modal, ModalHeader, ModalBody, ModalFooter, Input, Label, Form, FormGroup } from 'reactstrap';

/**
 * Generic dialog
 *
 * @class Dialog
 * @extends {React.Component}
 */
class Dialog extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      modal: props.modal || false,
    };

    this._toggle = this._toggle.bind(this);
  }

  static propTypes = {
    header: PropTypes.node.isRequired,
    modal: PropTypes.bool.isRequired,
    className: PropTypes.string,
    handler: PropTypes.func.isRequired,
    actions: PropTypes.arrayOf(PropTypes.shape({
      key: PropTypes.string.isRequired,
      label: PropTypes.string.isRequired,
      iconClass: PropTypes.string.isRequired,
      color: PropTypes.string,
    })).isRequired,
  };

  _toggle() {
    this.setState({
      modal: !this.state.modal
    });
  }

  render() {
    return (
      <Modal isOpen={this.state.modal} toggle={this._toggle} className={this.props.className} backdrop={false}>
        <ModalHeader toggle={this._toggle}>{this.props.header}</ModalHeader>
        <ModalBody>
          {this.props.children}
        </ModalBody>
        <ModalFooter>
          {
            this.props.actions.map((value) => {
              return (
                <Button
                  key={value.key}
                  color={value.color || 'secondary'}
                  onClick={(e) => { this.props.handler({ key: value.key }); }}
                >
                  <span><i className={value.iconClass + ' mr-2'}></i>{value.label}</span>
                </Button>
              );
            })
          }
        </ModalFooter>
      </Modal>
    );
  }
}

export default Dialog;
