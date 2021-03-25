module.exports = {
  verbose: true,
  transform: { '^.+\\.js$': '<rootDir>/jest-preprocess.js' },
  roots: [
    '<rootDir>/../src'
  ]
};