const configureMockStore = require('redux-mock-store').default;
const thunk = require('redux-thunk').default;
const forms = require('../ducks/forms');
const reducer = forms.default;

const middlewares = [thunk];
const mockStore = configureMockStore(middlewares);

// Test Reducer
describe('forms reducer', () => {
  it('should handle register forms', () => {
    // empty form
    expect(reducer({}, {
      type: forms.REGISTER,
      form: 'test',
      // no initialValues
    }))
    .toEqual({
      test: {
        values: {},
        errors: {},
        initialValues: {},
      }
    });
    // non-empty form
    expect(reducer({}, {
      type: forms.REGISTER,
      form: 'test',
      initialValues: { a: 123, b: 'hello', c: { ca: 1, cb: 2 } },
    }))
    .toEqual({
      test: {
        values: {
          a: 123,
          b: 'hello',
          c: { ca: 1, cb: 2 },
        },
        initialValues: {
          a: 123,
          b: 'hello',
          c: { ca: 1, cb: 2 },
        },
        errors: {},
      }
    });
    // new form together with existing
    expect(reducer({ test1: { x: 'x' }}, {
      type: forms.REGISTER,
      form: 'test2',
      initialValues: { c: 3, d: 4 },
    }))
    .toEqual({
      test1: { x: 'x' },
      test2: { errors: {}, values: { c: 3, d: 4 }, initialValues: { c: 3, d: 4 } },
    });

    // form with same id -- overriden ?
    expect(reducer({ test: { x: 'x' }}, {
      type: forms.REGISTER,
      form: 'test',
      initialValues: {},
    }))
    .toEqual({
      test: { errors: {}, values: {}, initialValues: {} }
    });
    

  });

  it('should handle set form values', () => {
    // set values without register?
    expect(reducer({ test: { errors: {'x': 'x' }, initialValues: {}, values: { y: 'y' } }}, {
      type: forms.SET_VALUES,
      form: 'test',
      values: { z: 'z' },
    }))
    .toEqual({
      test: { errors: { x: 'x' }, initialValues: {}, values: { z: 'z' } }
    });
  });
  
  it('should handle update form values', () => {
    expect(reducer({ test: { errors: {'x': 'x' }, initialValues: {}, values: { y: 'y' } }}, {
      type: forms.UPDATE_VALUES,
      form: 'test',
      values: { z: 'z' },
    }))
    .toEqual({
      test: { errors: { x: 'x' }, initialValues: {}, values: { y: 'y', z: 'z' } }
    });
  });

  it('should handle set form errors', () => {
    expect(reducer({ test: { errors: {}, initialValues: {}, values: { y: 'y' } }}, {
      type: forms.SET_ERRORS,
      form: 'test',
      errors: { e1: 'error 1' },
    }))
    .toEqual({
      test: { errors: { e1: 'error 1' }, initialValues: {}, values: { y: 'y' } }
    });

    expect(reducer({ test: { errors: { e1: 'error 1', e2: 'error 2' }, initialValues: { y: 'x' }, values: { y: 'y' } }}, {
      type: forms.SET_ERRORS,
      form: 'test',
      errors: { e1: 'new error 1' },
    }))
    .toEqual({
      test: { errors: { e1: 'new error 1' }, initialValues: { y: 'x' }, values: { y: 'y' } }
    });
  });

  it('should handle reset form errors', () => {
    expect(reducer({ test: { errors: { e1: 'error 1'}, initialValues: { y: 'x' }, values: { y: 'y' } }}, {
      type: forms.RESET_ERRORS,
      form: 'test',
    }))
    .toEqual({
      test: { errors: {}, initialValues: { y: 'x' }, values: { y: 'y' } }
    });
  });

  it('should handle reset form errors', () => {
    expect(reducer({ test: { errors: { e1: 'error 1'}, initialValues: { y: 'x' }, values: { y: 'y' } }}, {
      type: forms.RESET_ERRORS,
      form: 'test',
    }))
    .toEqual({
      test: { errors: {}, initialValues: { y: 'x' }, values: { y: 'y' } }
    });
  });

  it('should handle form reset', () => {
    expect(reducer({ test: { errors: { e1: 'error 1' }, initialValues: { y: 'x' }, values: { y: 'y' } }}, {
      type: forms.RESET,
      form: 'test',
    }))
    .toEqual({
      test: { errors: {}, initialValues: { y: 'x' }, values: { y: 'x' } }
    });
  });
});

// Test Action creators
describe('form simple action creators', () => {
  it('should create a register form action', () => {
    expect(forms.registerForm('test', { x: 'x', y: 'y' }))
    .toEqual({
      type: forms.REGISTER,
      form: 'test',
      initialValues: { x: 'x', y: 'y' },
    });
  }); 

  it('should create a reset form action', () => {
    expect(forms.resetForm('test'))
    .toEqual({
      type: forms.RESET,
      form: 'test',
    });
  });

  it('should create a set form values action', () => {
    expect(forms.setFormValues('test', { x: 'x', y: 'y' }))
    .toEqual({
      type: forms.SET_VALUES,
      form: 'test',
      values: { x: 'x', y: 'y' }
    });
  });

  it('should create a reset form values action', () => {
    expect(forms.resetFormValues('test'))
    .toEqual({
      type: forms.RESET_VALUES,
      form: 'test',
    });
  });

  it('should create a set form errors action', () => {
    expect(forms.setFormErrors('test', { e1: 'error 1', e2: 'error 2' }))
    .toEqual({
      type: forms.SET_ERRORS,
      form: 'test',
      errors: { e1: 'error 1', e2: 'error 2' }
    });
  });

  it('should create a reset form errors action', () => {
    expect(forms.resetFormErrors('test'))
    .toEqual({
      type: forms.RESET_ERRORS,
      form: 'test',
    });
  });
});

// Test thunk actions
describe('form thunk actions', () => {
  const errors = { x: 'x required' };

  const syncValidator = (values, state) => { 
    if (!values.x) 
      throw errors; 
  };

  const asyncValidator = (values, state) => {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        if (!values.x) {
          reject(errors);
        }
        resolve(values);
      }, 100);
    });
  };

  const accessor = state => state;

  const initialState = { test: { errors: {}, initialValues: { x: 'x' }, values: { x: 'y' }} };
  const initialErrorState = { test: { errors: {}, initialValues: { x: 'x' }, values: { x: null }} };

  it('should return validated values after synchronous validation', () => {
    const store = mockStore(initialState);
    
    return store.dispatch(forms.validateForm('test', syncValidator, accessor))
    .then((values) => { 
      expect(values).toEqual({ x: 'y' }); 
      expect(store.getState().test).toEqual({ errors: {}, initialValues: { x: 'x' }, values: { x: 'y' } });
    });
  });

  it('should create validation errors after synchronous validation', () => {
    const store = mockStore(initialErrorState);
    
    return store.dispatch(forms.validateForm('test', syncValidator, accessor))
    .then((values) => { expect('shouldnt').toEqual('be here'); })
    .catch((caughtErrors) => { expect(caughtErrors).toEqual(errors); });
  });
  
  it('should return validated values after Asynchronous validation', () => {
    const store = mockStore(initialState);
    
    return store.dispatch(forms.validateForm('test', asyncValidator, accessor))
    .then((values) => { 
      expect(values).toEqual({ x: 'y' }); 
      expect(store.getState().test).toEqual({ errors: {}, initialValues: { x: 'x' }, values: { x: 'y' } });
    })
    .catch((caughtErrors) => { expect('shouldnt').toEqual('be here'); });
  });

  it('should create validation errors after Asynchronous validation', () => {
    const store = mockStore(initialErrorState);
    
    return store.dispatch(forms.validateForm('test', asyncValidator, accessor))
    .then((values) => { expect('shouldnt').toEqual('be here'); })
    .catch((caughtErrors) => { expect(caughtErrors).toEqual(errors); });
  });
});
