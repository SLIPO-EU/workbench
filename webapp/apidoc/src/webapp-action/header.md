This document describes the SLIPO Web Application Action API Interface.

All Action API <code>POST</code>, <code>PUT</code> and <code>DELETE</code> requests must set the HTTP header `X-CSRF-TOKEN` to the value of the CSRF token. The latter is initially stored in the HTML <meta> element named `_csrf`. The application is responsible for extracting and storing the CSRF value received by every response in the `X-CSRF-TOKEN` response header.
