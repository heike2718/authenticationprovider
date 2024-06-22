import { Component, OnInit, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';

import { LoadingIndicatorComponent, MessageComponent } from './shared/messages/ui';
import { NavbarComponent } from './shell/layout/navbar/navbar.component';
import { SidenavComponent } from './shell/layout/sidenav/sidenav.component';
import { AuthFacade } from '@bv-admin-app/shared/auth/api';
import { BenutzerListComponent } from './benutzer/feature/benutzer-list/benutzer-list.component';

@Component({
  standalone: true,
  imports: [
    MatToolbarModule,
    MatSidenavModule,
    NavbarComponent,
    SidenavComponent,
    MessageComponent,
    BenutzerListComponent,
    LoadingIndicatorComponent,
    RouterModule
  ],
  selector: 'bv-admin-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent implements OnInit {

  #authFacade = inject(AuthFacade)

  ngOnInit(): void {
    this.#authFacade.initClearOrRestoreSession();      
  }

  
}
