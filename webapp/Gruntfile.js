module.exports = function (grunt) {

  const develop = process.env.NODE_ENV != 'production';

  // Project configuration

  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),

    sourceDir: 'src/main/frontend',

    buildDir: 'target/frontend',

    targetDir: 'target/classes/public/',

    // Clean build directory
    clean: {
      options: {
        force: true,
      },
      'workbench': {
        src: ['<%= buildDir %>/*'],
      },
    },

    // Compile Sass files
    sass: {
      'workbench': {
        options: {
          style: develop ? 'expanded' : 'compressed',
        },
        files: {
          '<%= buildDir %>/css/style.css': ['<%= sourceDir %>/scss/style.scss'],
        },
      },
    },

    // Apply JavaScript lint rules
    eslint: {
      'workbench': {
        options: {
          configFile: develop ? '.eslintrc.develop.js' : '.eslintrc.js',
        },
        src: [
          '<%= sourceDir %>/js/workbench/**/*.js',
          '!<%= sourceDir %>/js/workbench/__tests__/**/*.js',
        ],
      },
    },

    // Transpile and bundle JavaScript files
    browserify: {
      options: {
        /* moved to package.json */
        watch: true,
      },
      'workbench': {
        options: {
          // Exclude the modules below from being packaged into the main JS file:
          // The following will be resolved globally (shim) or via earlier vendor includes
          external: [
            'fetch',
            'flat',
            'history',
            'immutable',
            'intl-messageformat',
            'lodash',
            'moment',
            'moment/locale/el',
            'prop-types',
            'react',
            'react-dom',
            'react-intl',
            'react-intl/locale-data/el',
            'react-intl/locale-data/en',
            'react-redux',
            'react-router-dom',
            'react-router-redux',
            'react-transition-group',
            'reactstrap',
            'redux',
            'redux-logger',
            'redux-thunk',
            'url-search-params',
          ]
        },
        files: {
          '<%= buildDir %>/js/workbench.js': ['<%= sourceDir %>/js/workbench/main.js'],
        },
      },
      'vendor-util': {
        options: {
          alias: [
            'isomorphic-fetch:fetch',
          ],
          require: [
            'flat',
            'history',
            'immutable',
            'intl-messageformat',
            'lodash',
            'moment',
            'moment/locale/el',
            'url-search-params',
          ],
        },
        files: {
          '<%= buildDir %>/js/vendor/util.js': []
        },
      },
      'vendor-react': {
        options: {
          alias: [
            'tether:reactstrap-tether',
          ],
          require: [
            'prop-types',
            'react',
            'react-dom',
            'react-intl',
            'react-intl/locale-data/el',
            'react-intl/locale-data/en',
            'react-redux',
            'react-router-dom',
            'react-router-redux',
            'react-transition-group',
            'reactstrap',
            'redux',
            'redux-logger',
            'redux-thunk',
          ],
        },
        files: {
          '<%= buildDir %>/js/vendor/react-with-redux.js': [],
        },
      },
    },

    // Minify JavaScript files
    uglify: {
      options: {
        banner: '/*! Package: <%= pkg.name %> <%= grunt.template.today("yyyy-mm-dd") %> */\n',
        sourceMap: true,
        compress: {
          // Workaround for https://github.com/mishoo/UglifyJS2/issues/3274
          collapse_vars: false,
        },
      },
      'workbench': {
        files: {
          '<%= buildDir %>/js/workbench.min.js': ['<%= buildDir %>/js/workbench.js'],
        },
      },
      'vendor': {
        files: {
          '<%= buildDir %>/js/vendor/util.min.js': ['<%= buildDir %>/js/vendor/util.js'],
          '<%= buildDir %>/js/vendor/react-with-redux.min.js': ['<%= buildDir %>/js/vendor/react-with-redux.js'],
        },
      },
    },

    // Generate API documentation
    apidoc: {
      'webapp-action': {
        src: 'apidoc/src/webapp-action',
        dest: 'apidoc/docs/webapp-action',
        template: 'apidoc/template',
        options: {
          debug: false,
          includeFilters: ['.*\\.js$']
        }
      },
      'webapp-api': {
        src: 'apidoc/src/webapp-api',
        dest: 'apidoc/docs/webapp-api',
        template: 'apidoc/template',
        options: {
          debug: false,
          includeFilters: ['.*\\.js$']
        }
      },
    },

    // Generate workbench documentation
    jsdoc: {
      'workbench': {
        src: [
          '<%= sourceDir %>/js/workbench/**/*.js',
          '!<%= sourceDir %>/js/workbench/__tests__/**/*.js',
        ],
        options: {
          destination: 'jsdoc',
        }
      },
    },

    // Copy files to target folder
    copy: {
      options: {
        mode: '0644',
      },
      // Copy API documentation
      'apidoc': {
        expand: true,
        filter: 'isFile',
        cwd: 'apidoc/docs/',
        src: ['**/*'],
        dest: '<%= targetDir %>/docs/',
      },
      // Copy workbench documentation
      'jsdoc': {
        expand: true,
        filter: 'isFile',
        cwd: 'jsdoc',
        src: ['**/*'],
        dest: '<%= targetDir %>/docs/webapp-workbench/',
      },
      // Copy JavaScript files
      'workbench-scripts': {
        files: [
          {
            expand: true,
            filter: 'isFile',
            cwd: '<%= buildDir %>',
            src: 'js/workbench*.js',
            dest: '<%= targetDir %>',
          },
        ],
      },
      'vendor-scripts': {
        files: [
          {
            expand: true,
            filter: 'isFile',
            cwd: '<%= buildDir %>/',
            src: 'js/vendor/*.js',
            dest: '<%= targetDir %>',
          },
        ],
      },
      // Copy i18n data
      'workbench-i18n-data': {
        files: [
          // Note i18n data are just copied verbatim to build/target directory
          {
            expand: true,
            filter: 'isFile',
            cwd: '<%= sourceDir %>/js/workbench',
            src: 'i18n/**/*.json',
            dest: '<%= buildDir %>',
          },
          {
            expand: true,
            filter: 'isFile',
            cwd: '<%= sourceDir %>/js/workbench',
            src: 'i18n/**/*.json',
            dest: '<%= targetDir %>',
          },
        ],
      },
      // Copy CSS files
      'workbench-stylesheets': {
        files: [
          {
            expand: true,
            filter: 'isFile',
            cwd: '<%= buildDir %>',
            src: 'css/*.css',
            dest: '<%= targetDir %>',
          },
          {
            expand: true,
            filter: 'isFile',
            cwd: 'node_modules',
            src: 'ol/*.css',
            dest: '<%= targetDir %>/css',
          },
          {
            expand: true,
            filter: 'isFile',
            cwd: 'node_modules',
            src: 'codemirror/lib/*.css',
            dest: '<%= targetDir %>/css',
          },
        ],
      },
    },

    // Watch for changes to JavaScript, Sass and i18n files
    watch: {
      options: {
        interrupt: true
      },
      'workbench-i18n-data': {
        files: ['<%= sourceDir %>/js/workbench/i18n/**/*.json'],
        tasks: ['copy:workbench-i18n-data'],
      },
      'workbench-scripts': {
        files: ['<%= sourceDir %>/js/workbench/**/*.js'],
        tasks: ['copy:workbench-scripts'],
      },
      'workbench-stylesheets': {
        files: ['<%= sourceDir %>/scss/**/*.scss'],
        tasks: ['sass:workbench', 'copy:workbench-stylesheets'],
      },
    },



  }); /* initConfig */


  //
  // Load task modules
  //

  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-sass');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-eslint');
  grunt.loadNpmTasks('grunt-apidoc');
  grunt.loadNpmTasks('grunt-jsdoc');
  grunt.loadNpmTasks('grunt-browserify');

  //
  // Events
  //

  grunt.event.on('watch', function (action, filepath, target) {
    grunt.log.writeln(target + ': ' + filepath + ' has ' + action);
  });

  //
  // Register new tasks
  //

  grunt.registerTask('docs', ['apidoc', 'jsdoc', 'copy:apidoc', 'copy:jsdoc']);

  grunt.registerTask('browserify:vendor', [
    'browserify:vendor-util', 'browserify:vendor-react',
  ]);

  grunt.registerTask('build:vendor', develop ?
    ['browserify:vendor',] :
    ['browserify:vendor', 'uglify:vendor']
  );

  grunt.registerTask('build:workbench', develop ?
    ['sass:workbench', 'eslint:workbench', 'browserify:workbench', 'copy:workbench-i18n-data'] :
    ['sass:workbench', 'eslint:workbench', 'browserify:workbench', 'copy:workbench-i18n-data', 'uglify:workbench']
  );

  grunt.registerTask('build', ['build:workbench', 'build:vendor']);

  grunt.registerTask('develop', ['clean', 'build', 'docs', 'copy', 'watch']);

  grunt.registerTask('default', ['clean', 'build', 'docs', 'copy',]);

};
