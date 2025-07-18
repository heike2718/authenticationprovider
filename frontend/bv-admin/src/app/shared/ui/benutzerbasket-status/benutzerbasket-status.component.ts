import { CommonModule } from "@angular/common";
import { Component, inject } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { Router } from "@angular/router";
import { BenutzerFacade } from "@bv-admin/benutzer/api";
import { AuthFacade } from "@bv-admin/shared/auth/api";


@Component({
    selector: 'bv-admin-benutzerbasket-status',
    imports: [
        CommonModule,
        MatButtonModule,
        MatIconModule
    ],
    templateUrl: './benutzerbasket-status.component.html',
    styleUrls: ['./benutzerbasket-status.component.scss']
})
export class BenutzerBasketStatusComponent {


    authFacade = inject(AuthFacade);
    benutzerFacade = inject(BenutzerFacade);

    #router = inject(Router);

    gotoBasket(): void {
        this.#router.navigateByUrl('users/basket');
    }

}