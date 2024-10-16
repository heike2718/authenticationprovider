// The file contents for the current environment will overwrite these during build.
// The build system defaults to the dev environment which uses `environment.ts`, but if you do
// `ng build --env=prod` then `environment.prod.ts` will be used instead.
// The list of which env maps to which file can be found in `.angular-cli.json`.

export const environment = {
  production: false,
  version: '8.0.2',
  envName: 'Test',
  apiUrl: 'http://localhost:10600/profil-api',
  assetsUrl: 'assets',
  loginRedirectUrl: 'http://localhost:4400/profil-app',
  consoleLogActive: true,
  serverLogActive: true,
  loglevel: 1
};
