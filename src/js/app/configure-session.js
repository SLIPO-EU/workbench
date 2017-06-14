module.exports = function configureSession(session, {secret, dataDir})
{
  const FileStore = require('session-file-store')(session);
  
  return {
    store: new FileStore({
      path: dataDir + '/sessions',
    }),
    secret,
    resave: false,
    saveUninitialized: true,
    cookie: {
      httpOnly: true,
      secure: false, 
    }
  };
};
