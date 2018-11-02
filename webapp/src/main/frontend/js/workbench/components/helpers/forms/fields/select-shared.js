export const styles = {
  control: (base, state) => {
    return {
      ...base,
      borderRadius: '0px',
      borderColor: '#cccccc',
      ':focus': {
        borderColor: '#8ad4ee',
      },
      boxShadow: 'none',
    };
  },
  container: (base, state) => ({
    ...base,
    zIndex: '1002',
  }),
  menu: (base, state) => ({
    ...base,
    borderRadius: '0px',
    boxShadow: 'none',
    marginTop: '-1px',
    border: '1px solid #cccccc',
  }),
};
