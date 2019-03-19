const del         = require('del')
const gulp        = require('gulp')
const compressCSS = require('gulp-clean-css')
const concat      = require('gulp-concat')
const decide      = require('gulp-if')
const sass        = require('gulp-sass')
const sourcemaps  = require('gulp-sourcemaps')
const noComments  = require('gulp-strip-comments')
const compressJS  = require('gulp-uglify')
const typescript  = require('rollup-plugin-typescript')
const rollup      = require('rollup-stream')
const buffer      = require('vinyl-buffer')
const source      = require('vinyl-source-stream')

// Compile Typescript files to ES6 and combine modules with Rollup.
gulp.task('compile:js', () => {
  return rollup({
      entry: './src/main/typescript/app.tsx',
      moduleName: 'frontend',
      format: 'iife',
      sourceMap: true,
      plugins: [
        typescript({
          'jsx': 'React',
        }),
      ],
      external: [
        'codemirror',
        'codemirror-no-newlines',
        'localforage',
        'react',
        'react-dom',
        'superagent',
      ],
      globals: {
        'react'       : 'React',
        'react-dom'   : 'ReactDOM',
        'superagent'  : 'superagent',
        'codemirror'  : 'CodeMirror',
        'localforage' : 'localforage',
      },
    })
    .on('error', (err) => {
      console.error(err.stack)
      process.exit(1)
    })
    .pipe(source('app.js'))
    .pipe(buffer())
    .pipe(sourcemaps.init({loadMaps: true}))
    .pipe(sourcemaps.write('.'))
    .pipe(gulp.dest('./src/main/resources/dist'))
})

// Concatenate JS libraries into a single file.
gulp.task('bundle:js', () => {
  // Paths to libraries used by the frontend. Each file in this list is
  // concatenated into ./src/main/resources/dist/libs.js and will be
  // concatenated in this order:
  const libs = [
    './node_modules/react/dist/react.min.js',
    './node_modules/react-dom/dist/react-dom.min.js',
    './node_modules/superagent/superagent.js',
    './node_modules/codemirror/lib/codemirror.js',
    './node_modules/codemirror/addon/display/placeholder.js',
    './node_modules/codemirror-no-newlines/no-newlines.js',
  ]

  function isNotMinified (file) {
    let isMinified = /\.min\.js$/.test(file.history[0])
    return (isMinified === false)
  }

  gulp.src(libs)
    .pipe(decide(isNotMinified, compressJS()))
    .pipe(concat('libs.js'))
    .pipe(noComments())
    .pipe(gulp.dest('./src/main/resources/dist'))
})

// Uglify app logic and concatenated libraries.
gulp.task('compress:js', ['compile:js', 'bundle:js'], () => {
  return gulp.src('./src/main/resources/dist/app.js')
    .pipe(compressJS())
    .pipe(gulp.dest('./src/main/resources/dist'))
})

// Compile SCSS files to CSS.
gulp.task('compile:css', () => {
  return gulp.src('src/main/sass/*.scss')
    .pipe(sass().on('error', sass.logError))
    .pipe(gulp.dest('./src/main/resources/dist'))
})

// Concatenate CSS stylesheets used by libraries.
gulp.task('bundle:css', () => {
  // Paths to stylesheets used by the frontend. Each file in this list is
  // concatenated into ./src/main/resources/dist/libs.css and will be
  // concatenated in this order:
  const libs = [
    './node_modules/codemirror/lib/codemirror.css',
  ]

  return gulp.src(libs)
    .pipe(concat('libs.css'))
    .pipe(gulp.dest('./src/main/resources/dist'))
})

// Compress app & library stylesheets.
gulp.task('compress:css', ['compile:css', 'bundle:css'], () => {
  return gulp.src('./src/main/resources/dist/*.css')
    .pipe(compressCSS())
    .pipe(gulp.dest('./src/main/resources/dist'))
})

gulp.task('build:all', ['compress:css', 'compress:js'])

gulp.task('watch:js', ['compile:js'], () => {
  gulp.watch([
    'src/main/typescript/*.tsx',
    'src/main/typescript/*.ts',
  ], ['compile:js'])
})

gulp.task('watch:css', ['compile:css'], () => {
  gulp.watch('src/main/sass/*.scss', ['compile:css'])
})

// Delete everything inside the ./src/main/resources/dist directory including
// the directory itself.
gulp.task('clean', () => {
  return del([ './src/main/resources/dist/*.{css,js,map}' ])
})
