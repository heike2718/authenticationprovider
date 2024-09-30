import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { MessageComponent, LoadingIndicatorComponent} from '@auth-app-profil-app-ws/messages/ui';
import { HomeComponent } from './home/home.component';
import { Configuration } from '@auth-app-profil-app-ws/config';

@Component({
  standalone: true,
  imports: [
    MessageComponent,
    LoadingIndicatorComponent,
    HomeComponent,
    RouterModule],
  selector: 'auth-app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent  {

  configuration = inject(Configuration);

  imageSourceLogo = this.configuration.assetsPath + 'mja_logo_2.svg';
}
