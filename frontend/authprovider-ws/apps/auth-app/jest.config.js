module.exports = {
  preset: '../../jest.preset.js',
  coverageDirectory: '../../coverage/apps/auth-app',
  snapshotSerializers: [
    'jest-preset-angular/build/AngularNoNgAttributesSnapshotSerializer.js',
    'jest-preset-angular/build/AngularSnapshotSerializer.js',
    'jest-preset-angular/build/HTMLCommentSerializer.js',
  ],
  displayName: 'auth-app',
};
