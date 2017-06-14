module.exports = function(grunt) {
  
  const prefix = grunt.option('prefix') || 'public/www';

  const develop = process.env.NODE_ENV != 'production';

  // Project configuration.
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    
    
    clean: {
      options: {
        force: true,
      },
      'workbench': {
        src: ['build/*', 'public/www/*'],
      },
    },
    
    
    uglify: {
      options: {
        banner: '/*! Package: <%= pkg.name %> <%= grunt.template.today("yyyy-mm-dd") %> */\n'
      },
      'workbench': {
        files: { 
          'build/<%= pkg.name %>.min.js': ['build/<%= pkg.name %>.js'],
        },
      },
      'vendor': {
        files: {
          'build/vendor/util.min.js': ['build/vendor/util.js'],
          'build/vendor/moment-localized.min.js': ['build/vendor/moment-localized.js'],
          'build/vendor/react-with-redux.min.js': ['build/vendor/react-with-redux.js'],         
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
            'fetch', 'lodash', 'immutable', 'rgbcolor', 'history', 'sprintf', 'url-search-params',
            'moment', 'moment/locale/el', 'moment/locale/es', 'moment/locale/de',
            'react', 'react-dom', 'prop-types', 'react-router-dom', 
            'redux', 'redux-logger', 'redux-thunk', 'react-router-redux', 'react-redux', 
            'reactstrap', 'react-transition-group',
          ]
        },
        files: {
          'build/<%= pkg.name %>.js': ['src/js/main.js'],
        },
      },
      'vendor-util': {
        options: {
          alias: [
            'isomorphic-fetch:fetch',
            'url-search-params',
            'lodash',
            'history',
            'immutable',
            'rgbcolor',
            'sprintf',
          ]
        },
        files: {
          'build/vendor/util.js': []
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
          ],
        },
        files: {
          'build/vendor/react-with-redux.js': [],
        },
      },
      'vendor-moment': {
        options: {
          require: [
            'moment', 'moment/locale/el', 'moment/locale/es', 'moment/locale/de',
          ],
        },
        files: {
          'build/vendor/moment-localized.js': [],
        },
      },
    },


    sass: {
      'workbench': {
        options: {
          style: develop? 'expanded' : 'compressed',
        },
        files: {
          'build/style.css': ['assets/style.scss'], 
        },
      },
    },


    copy: {
      options: {
        mode: '0644',
      },
      'workbench-markup': {
         options: {
          // Pre-process HTML files as templates
          processContent: function (data, src) {
            return grunt.template.process(data)
          },
        },
        files: [
          {
            expand: true,
            filter: 'isFile',
            cwd: 'src/html/',
            src: '*.html',
            dest: prefix,
          }, 
        ],
      },
      'workbench-scripts': {
        files: [
          {
            expand: true,
            filter: 'isFile',
            cwd: 'build/',
            src: '<%= pkg.name %>*.js',
            dest: prefix,
          },
        ],
      },
      'workbench-stylesheets': { 
        files: [
          {
            expand: true,
            filter: 'isFile',
            cwd: 'build/',
            src: '*.css',
            dest: prefix,
          },
          {
            expand: true,
            filter: 'isFile',
            cwd: 'assets/',
            src: '**/*.png',
            dest: prefix,
          },          
          {
            expand: true,
            filter: 'isFile',
            cwd: 'assets/',
            src: 'fonts/*',
            dest: prefix,
          },
        ],
      },
      'vendor': {
        files: [ 
          {
            expand: true,
            filter: 'isFile',
            cwd: 'build/',
            src: 'vendor/*.js',
            dest: prefix,
          },
          {
            expand: true,
            filter: 'isFile',
            cwd: '.',
            src: 'vendor/coreui/**/*',
            dest: prefix,
          }
        ],
      },
    },


    eslint: {
      'workbench': {
        options: {
          configFile: develop? '.eslintrc.develop.js' : '.eslintrc.js',
        },
        src: [
          'src/js/**/*.js',
          '!src/js/__tests__/**/*.js',
        ],
      },
    },


    watch: {
      'workbench-scripts': {
         files: ['src/js/**/*.js'],
         tasks: ['build:workbench', 'copy:workbench-scripts'],
      },
      'workbench-markup': {
        files: ['src/html/**.html'],
        tasks: ['copy:workbench-markup'],
      },
      'workbench-stylesheets': {
        files: ['assets/**.scss'],
        tasks: ['sass:workbench', 'copy:workbench-stylesheets'],
      },
      'vendor': {
        files: ['vendor/**/*'],
        tasks: ['build:vendor', 'deploy:vendor'],
      },
    },

    
    jsdoc: {
      'workbench': {
        src: ['src/js/**/*.js', '!src/js/__tests__/*.js'],
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
    'browserify:vendor-util', 'browserify:vendor-react', 'browserify:vendor-moment'
  ]);

  grunt.registerTask('build:workbench', [
    'sass:workbench', 'eslint:workbench', 'browserify:workbench', 'uglify:workbench'
  ]);
  
  grunt.registerTask('build:vendor', [
    'browserify:vendor', 'uglify:vendor'
  ]);
  
  grunt.registerTask('build', [
    'sass', 'eslint', 'browserify', 'uglify'
  ]);
  
  grunt.registerTask('deploy:workbench', [
    'copy:workbench-markup', 'copy:workbench-scripts', 'copy:workbench-stylesheets',
  ]);
  grunt.registerTask('deploy:vendor', ['copy:vendor']);
  grunt.registerTask('deploy', ['copy']);  

  grunt.registerTask('default', ['build', 'deploy']);
};
