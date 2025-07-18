import { AsyncPipe, CommonModule } from "@angular/common";
import { Component, inject } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatTableModule } from "@angular/material/table";
import { Benutzer } from "@bv-admin/shared/model";
import { VersandauftraegeFacade } from "@bv-admin/versandauftraege/api";
import { MailversandgruppeDetails } from "@bv-admin/versandauftraege/model";


const UUID = 'uuid';
const EMAIL = 'email';
const NACHNAME = 'nachname';
const VORNAME = 'vorname';
const AENDERUNGSDATUM = 'aenderungsdatum';
const ROLLE = 'rolle';
const REMOVE_FROM_GROUPR = 'removeFromGroup';

@Component({
    selector: 'bv-admin-versandgruppe',
    imports: [
        CommonModule,
        AsyncPipe,
        MatButtonModule,
        MatTableModule,
        MatIconModule
    ],
    templateUrl: './versandgruppe-details.component.html',
    styleUrls: ['./versandgruppe-details.component.scss']
})
export class VersandgruppeDetailsComponent {

    versandauftraegeFacade = inject(VersandauftraegeFacade);

    getDisplayedColumns(): string[] {
        return [UUID, EMAIL, NACHNAME, VORNAME, AENDERUNGSDATUM, ROLLE, REMOVE_FROM_GROUPR];
    }

    refresh(gruppe: MailversandgruppeDetails): void {

        if (!gruppe) {
            return;
        }

        this.versandauftraegeFacade.loadMailversandgruppe(gruppe.uuid);

    }

    btnRefreshDisabled(gruppe: MailversandgruppeDetails): boolean {

        if (!gruppe) {
            return true;
        }

        return gruppe.status === 'COMPLETED' || gruppe.status === 'ERRORS';
    }

    saveGruppe(gruppe: MailversandgruppeDetails): void {

        this.versandauftraegeFacade.updateMailversandgruppe(gruppe);

    }

    benutzerEntfernen(benutzer: Benutzer, gruppe: MailversandgruppeDetails): void {
        this.versandauftraegeFacade.removeBenutzerFromMailversandgruppe(benutzer, gruppe);
    }

    isGruppeAenderbar(gruppe: MailversandgruppeDetails): boolean {

        return gruppe.status === "CANCELLED" || gruppe.status === "ERRORS" || gruppe.status === "WAITING";
    }

    statusZuruecksetzen(gruppe: MailversandgruppeDetails): void {

        if (gruppe.status === "CANCELLED") {
            return;
        }

        if (this.isGruppeAenderbar(gruppe)) {

            const zuSpeicherndeGruppe: MailversandgruppeDetails = {
                ...gruppe,
                status: "CANCELLED"
            };
            this.versandauftraegeFacade.updateMailversandgruppe(zuSpeicherndeGruppe);
        }
    }

    goBack(): void {
        this.versandauftraegeFacade.unselectVersandgruppe();
    }

}