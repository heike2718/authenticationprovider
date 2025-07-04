import { inject } from "@angular/core";
import { Routes } from "@angular/router";
import { AuthFacade } from "@bv-admin/shared/auth/api";
import { BenutzerRootComponent } from "./benutzer-root/benutzer-root.component";
import { BenutzerListComponent } from "./benutzer-list/benutzer-list.component";
import { BenutzerBasketComponent } from "./benutzer-basket/benutzer-basket.component";


export const benutzerRoutes: Routes = [
    {
        path: '',
        canActivate: [() => inject(AuthFacade).userIsLoggedIn$],
        component: BenutzerRootComponent,
        children: [
          {
            path: '',
            component: BenutzerListComponent,
          },
          {
            path: 'basket',
            component: BenutzerBasketComponent,
          },
        //   {
        //     path: 'new',
        //     component: AddCustomerComponent,
        //     data: { mode: 'new' },
        //   },
        //   {
        //     path: ':id',
        //     component: EditCustomerComponent,
        //     data: { mode: 'edit' },
        //   },
        ],
      },
]