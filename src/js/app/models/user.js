module.exports = function(sequelize, DataTypes) {
  
  //
  // Define model
  //

  var User = sequelize.define('User', {
    givenName: {type: DataTypes.STRING(64)},
    familyName: {type: DataTypes.STRING(64)},
    username: {type: DataTypes.STRING(64), primaryKey: true},
    password: {type: DataTypes.STRING(128)},
    email: {type: DataTypes.STRING(64), unique: true},
  }, {
    classMethods: {}
  });
  
  //
  // Enhance with instance methods
  //

  const {createHash} = require('crypto');
  
  User.prototype.checkPassword = function (password)
  {
    if (password == null || typeof(password) != 'string' || password.length == 0)
      return false;
    
    var h = createHash('sha256');
    h.update(password);
    return this.password == h.digest().toString('hex');
  };

  return User;
};
