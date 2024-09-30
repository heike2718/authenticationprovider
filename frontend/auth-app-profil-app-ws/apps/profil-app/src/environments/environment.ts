import { provideStoreDevtools } from '@ngrx/store-devtools';

export const environment = {
  version: '9.0.0',
  envName: 'dev',
  production: false,
  baseUrl: 'http://localhost:10000/authprovider',
  loginRedirectUrl: 'http://localhost:4400/profil-app',
  datenschutzUrl: 'https://mathe-jung-alt.de/minikaenguru/datenschutz.html',
  withCredentials: true,
  assetsPath: '',
  providers: [
    provideStoreDevtools({
      maxAge: 25,
      autoPause: true,
      connectInZone: true
    }),
  ],
};

