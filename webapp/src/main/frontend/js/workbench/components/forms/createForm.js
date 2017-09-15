import React from 'react';
import PropTypes from 'prop-types';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';

import { setForm, resetForm, validateForm } from '../../ducks/forms';

class Form extends React.Component {
  componentWillMount() {
    this.props.registerForm();
  }

  render() {
    return (
      <form
        onSubmit={(e) => {
          e.preventDefault();
          
          this.props.validateForm()
          .then((values) => this.props.onSuccess(values))
          .catch((error) => this.props.onFailure(error));
        }}
      >
      {
        React.Children.map(this.props.children, ((Child,i) => (
          Child.props.id ?
            <Child.type 
              key={i} 
              {...Child.props}
              onChange={(value) => this.props.updateForm(Child.props.id, value)}
              error={this.props.errors[Child.props.id]}
              value={this.props.values[Child.props.id]}
            />
            : <Child.type 
                key={i}
                {...Child.props}
              />
          )))
      }
      </form>
    );
  }
}

export default function createForm(id, model, validate = () => {}) {

  const mapStateToProps = state => state.forms[id] ? ({
    values: state.forms[id].values || {},
    errors: state.forms[id].errors || {},
  }) 
  : ({ values: {}, errors: {} });

  const mapDispatchToProps = (dispatch) => bindActionCreators({ 
    setForm,
    resetForm,
    validateForm,
  }, dispatch);

  const mergeProps = (stateProps, dispatchProps, ownProps) => ({
    values: ownProps.initialValues,
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
    registerForm: () => dispatchProps.setForm(id, model),
    setForm: (data) => dispatchProps.setForm(id, data),
    updateForm: (field, value) => {
      const newValues = { ...stateProps.values };  
      newValues[field] = value;
      dispatchProps.setForm(id, newValues); 
    },
    resetForm: () => dispatchProps.resetForm(id),
    validateForm: () => dispatchProps.validateForm(id, validate),
  });
  
  Form.propTypes = {
    onSuccess: PropTypes.func,
    onFailure: PropTypes.func,
    validate: PropTypes.func,
    initialValues: PropTypes.object,
    errors: PropTypes.object,
  };

  Form.defaultProps = {
    validate: () => {},
    initialValues: {},
  };

  return connect(mapStateToProps, mapDispatchToProps, mergeProps)(Form);
}
