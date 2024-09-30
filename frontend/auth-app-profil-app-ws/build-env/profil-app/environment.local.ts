import { provideStoreDevtools } from '@ngrx/store-devtools';

export const environment = {
  version: '9.0.0',
  envName: 'dev',
  production: false,
  baseUrl: '',
  loginRedirectUrl: 'http://localhost:/profil-app',
  datenschutzUrl: 'https://mathe-jung-alt.de/minikaenguru/datenschutz.html',
  withCredentials: true,
  assetsPath: '/profil-app/assets/',
  providers: [
    provideStoreDevtools({ maxAge: 25 , connectInZone: true})
  ],
};