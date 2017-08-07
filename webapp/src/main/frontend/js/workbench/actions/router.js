const { push: navigateTo } = require('react-router-redux');

var actions = {

  //
  // Router actions (intercepted by router middleware)
  //

  navigateTo, // provides the illusion that is a normal redux action
};

module.exports = actions;
