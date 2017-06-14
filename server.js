#!/usr/bin/env node

'use strict';

//
// Load the raw JSON configuration and tranform it
//

const {readFileSync} = require('fs');
const {resolve: resolvePath} = require('path');

var config = require('./config/config.json');

config.env = process.env.NODE_ENV || 'development';

config.session.secret = readFileSync(config.session.secret, 'ascii');

config.docRoot = config.docRoot.map(p => resolvePath(__dirname, p));

//
// Load app and serve
//

const app = require('./src/js/app/index')(config);

const server_address = process.env.SERVER_ADDRESS || '0.0.0.0';
const server_port = process.env.SERVER_PORT || 3000;

app.listen(server_port, server_address, () => {
  console.info('Listening to ' + server_address + ':' + server_port);
});
