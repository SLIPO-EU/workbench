# Introduction

Design and execute workflows on POI datasets.

# Quickstart (example)

## Configure

Copy initial configuration from samples, and tweak:

    mkdir secrets data
    echo -n secr3t >secrets/session-secret
    cp config/config.json.example config/config.json
    cp initial-data/app.db data/app.db

## Build

Install SASS globally to be used as CSS compiler:

    sudo gem install sass

Install Grunt locally (and use a symlink for convenience):

    npm install grunt-cli grunt
    ./grunt

Install all project dependencies:

    npm install

Build, copy to target folder (by default is `public/www`), and watch for changes:

    ./grunt build deploy watch

## Serve the example application

Start Express server:

    npm start

