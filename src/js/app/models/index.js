const fs = require('fs');
const path = require('path');
const Sequelize = require('sequelize');

const basename = path.basename(module.filename);
const dirname = path.dirname(module.filename); 

module.exports = function(config) 
{
  var db = {};
  
  var sequelize = new Sequelize(config.url, /* opts */ {
    define: {
      // prevent sequelize from pluralizing table names
      freezeTableName: true, 
    },
  });

  // Inspect current folder and load models

  fs.readdirSync(dirname)
    .filter(function (file) {
      return (file.indexOf('.') !== 0) && (file !== basename) && (file.slice(-3) === '.js');
    })
    .forEach(function (file) {
      var model = sequelize['import'](path.join(dirname, file));
      db[model.name] = model;
    });

  Object.keys(db).forEach(function (modelName) {
    if (db[modelName].associate) {
      db[modelName].associate(db);
    }
  });

  db.sequelize = sequelize;
  db.Sequelize = Sequelize;

  return db;
};
