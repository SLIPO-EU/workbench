import React from 'react';
import PropTypes from 'prop-types';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';

import { registerForm, resetForm, validateForm, setFormValues, updateFormValues } from '../../../ducks/forms';


export default function createForm(id, model, validate = () => {}, accessor = (state) => state.ui.forms) {

  const mapStateToProps = state => accessor(state)[id] ? ({
    values: accessor(state)[id].values || {},
    errors: accessor(state)[id].errors || {},
    registered: true,
  }) 
    : 
    ({ values: {}, errors: {}, registered: false });

  const mapDispatchToProps = (dispatch) => bindActionCreators({ 
    registerForm,
    resetForm,
    setFormValues,
    updateFormValues,
    validateForm,
  }, dispatch);

  const mergeProps = (stateProps, dispatchProps, ownProps) => ({
    values: ownProps.initialValues,
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
    registerForm: () => dispatchProps.registerForm(id, model),
    setForm: (data) => dispatchProps.setForm(id, data),
    updateForm: (field, value) => {
      const values = {};  
      values[field] = value;
      dispatchProps.updateFormValues(id, values); 
    },
    resetForm: () => dispatchProps.resetForm(id),
    validateForm: () => dispatchProps.validateForm(id, validate, accessor),
  });
  
  
  return (Component) => {

    class Form extends React.Component {
      constructor() {
        super();
      }

      componentWillMount() {
        if (!this.props.registered) {
          this.props.registerForm();
        }
      }

      render() {
        return (
          <form
            onSubmit={(e) => {
              e.preventDefault();
              this.props.validateForm()
                .then((values) => typeof this.props.onSuccess === 'function' && this.props.onSuccess(values))
                .catch((error) => typeof this.props.onFailure === 'function' && this.props.onFailure(error));
            }}
          >
            <Component {...this.props} />          
          </form>
        );
      }
    }
    
    Form.propTypes = {
      onSuccess: PropTypes.func,
      onFailure: PropTypes.func,
      validate: PropTypes.func,
      initialValues: PropTypes.object,
      errors: PropTypes.object,
    };

    return connect(mapStateToProps, mapDispatchToProps, mergeProps)(Form);
  };
}
