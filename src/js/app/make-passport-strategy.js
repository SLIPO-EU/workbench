const passport = require('passport');
const LocalStrategy = require('passport-local').Strategy;

module.exports = function makeStrategy(db)
{
  return new LocalStrategy(function (username, password, decide) {
    // Load user entity from database
    db.User.findById(username)
      .then((user) => {
        // Check credentials
        if (user == null || !user.checkPassword(password)) {
          // Failed
          return decide(null, false, {message: 'invalid credentials'});
        } else {  
          // The authentication is succesfull: return a piece of user info 
          // (a plain object) that can be stored into session.
          // Note What is actually stored in session can also be configured
          // with passport.{serializeUser/deserializeUser} hooks.
          var userInfo = {
            username,
            email: user.email,
            givenName: user.givenName,
            familyName: user.familyName,
          };
          return decide(null, userInfo);
        }
      })
      .catch(err => decide(err));
  });
};
