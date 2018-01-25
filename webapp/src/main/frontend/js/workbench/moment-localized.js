const moment = require('moment');

require('moment/locale/el');
require('moment/locale/de');

moment.locale('en-GB'); // set global locale

module.exports = moment;
