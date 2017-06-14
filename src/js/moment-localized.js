const moment = require('moment');

require('moment/locale/es');
require('moment/locale/el');
require('moment/locale/de');

moment.locale('en'); // set global locale

module.exports = moment;
