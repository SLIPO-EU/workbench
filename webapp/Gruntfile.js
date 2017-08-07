module.exports = function (grunt) {

  const develop = process.env.NODE_ENV != 'production';

  // Project configuration

  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),

    sourceDir: 'src/main/frontend',

    buildDir: 'target/frontend',

    targetDir: 'target/classes/public/',

    clean: {
      options: {
        force: true,
      },
      'workbench': {
        src: ['<%= buildDir %>/*'],
      },
    },


    uglify: {
      options: {
        banner: '/*! Package: <%= pkg.name %> <%= grunt.template.today("yyyy-mm-dd") %> */\n',
        sourceMap: true,
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


    browserify: {
      options: {
        /* moved to package.json */
      },
      'workbench': {
        options: {
          // Exclude the modules below from being packaged into the main JS file:
          // The following will be resolved globally (shim) or via earlier vendor includes
          external: [
            'fetch', 'lodash', 'immutable', 'history', 'url-search-params', 'flat',
            'moment', 'moment/locale/el', 
            'react', 'react-dom', 'prop-types', 'react-router-dom',
            'redux', 'redux-logger', 'redux-thunk', 'react-router-redux', 'react-redux',
            'reactstrap', 'react-transition-group',
            'intl-messageformat', 'react-intl', 'react-intl/locale-data/en', 'react-intl/locale-data/el',
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
            'moment', 'moment/locale/el',
            'url-search-params',
            'intl-messageformat',
            'lodash',
            'flat',
            'history',
            'immutable',
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
            'react', 'react-dom', 'prop-types', 'react-router-dom',
            'redux', 'redux-logger', 'redux-thunk', 'react-router-redux', 'react-redux',
            'reactstrap', 'react-transition-group',
            'react-intl', 'react-intl/locale-data/en', 'react-intl/locale-data/el',
          ],
        },
        files: {
          '<%= buildDir %>/js/vendor/react-with-redux.js': [],
        },
      },
    },


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


    copy: {
      options: {
        mode: '0644',
      },
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
            cwd: '<%= buildDir %>',
            src: 'i18n/**/*.json',
            dest: '<%= targetDir %>',
          },
        ],
      },
      'workbench-stylesheets': {
        files: [
          {
            expand: true,
            filter: 'isFile',
            cwd: '<%= buildDir %>',
            src: 'css/*.css',
            dest: '<%= targetDir %>',
          },
        ],
      },
      'vendor': {
        files: [
          {
            expand: true,
            filter: 'isFile',
            cwd: '<%= buildDir %>/',
            src: 'vendor/*.js',
            dest: '<%= targetDir %>',
          },
        ],
      },
    },


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


    watch: {
      'workbench-scripts': {
        files: ['<%= sourceDir %>/js/workbench/**/*.js'],
        tasks: ['build:workbench', 'copy:workbench-scripts'],
      },
      'workbench-i18n-data': {
        files: ['<%= sourceDir %>/js/workbench/i18n/**/*.json'],
        tasks: ['copy:workbench-i18n-data'],

      },
      'workbench-stylesheets': {
        files: ['<%= sourceDir %>/scss/**.scss'],
        tasks: ['sass:workbench', 'copy:workbench-stylesheets'],
      },
    },


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
  grunt.loadNpmTasks('grunt-jsdoc');
  grunt.loadNpmTasks('grunt-browserify');


  //
  // Register new tasks
  //

  grunt.registerTask('browserify:vendor', [
    'browserify:vendor-util', 'browserify:vendor-react',
  ]);

  grunt.registerTask('build:workbench', develop ?
    ['sass:workbench', 'eslint:workbench', 'browserify:workbench', 'copy:workbench-i18n-data'] :
    ['sass:workbench', 'eslint:workbench', 'browserify:workbench', 'copy:workbench-i18n-data', 'uglify:workbench']
  );

  grunt.registerTask('build:vendor', develop ?
    ['browserify'] :
    ['browserify:vendor', 'uglify:vendor']
  );

  grunt.registerTask('build', ['build:workbench', 'build:vendor']);

  grunt.registerTask('copy:workbench', [
    'copy:workbench-scripts', 'copy:workbench-i18n-data', 'copy:workbench-stylesheets',
  ]);
  grunt.registerTask('deploy', ['copy']);

  grunt.registerTask('default', ['build', 'deploy']);
};
