import { AsyncPipe, CommonModule, NgFor, NgIf } from "@angular/common";
import { AfterViewInit, ChangeDetectorRef, Component, inject, OnDestroy } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatTableModule } from "@angular/material/table";
import { InfomailsFacade } from '@bv-admin-app/infomails/api';
import { Infomail } from "@bv-admin-app/infomails/model";
import { combineLatest, Subscription } from "rxjs";
import { InfomailsDetailsComponent } from "../infomails-details/infomails-details.component";
import { InfomailEditComponent } from "../infomails-edit/infomails-edit.component";

const UUID = 'uuid';
const BETREFF = 'betreff';
const ANZAHL_VERSENDET = "anzahlVersendet"

@Component({
  selector: 'bv-infomails',
  standalone: true,
  imports: [
    CommonModule,
    InfomailsDetailsComponent,
    InfomailEditComponent,
    NgIf,
    NgFor,
    AsyncPipe,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './infomails-list.component.html',
  styleUrls: ['./infomails-list.component.scss'],
})
export class InfomailsListComponent implements OnDestroy, AfterViewInit {

  infomailsFacade = inject(InfomailsFacade);

  selectedInfomail: Infomail | undefined;
  editMode = false;

  // eine für alle mit add :)
  #subscriptions: Subscription = new Subscription();

  constructor(private changeDetector: ChangeDetectorRef) {
  }

  ngAfterViewInit(): void {

    const loadedSubscription = this.infomailsFacade.infomailsLoaded$.subscribe((loaded) => {
      if (!loaded) {
        this.infomailsFacade.loadInfomails();
      }
    });

    const combinedStatusSubscription = combineLatest([
      this.infomailsFacade.selectedInfomail$,
      this.infomailsFacade.editMode$]).subscribe(
      ([infomailText, editMode]) => {
        this.selectedInfomail = infomailText;
        this.editMode = editMode;
      }
    );

    this.#subscriptions.add(loadedSubscription);
    this.#subscriptions.add(combinedStatusSubscription);

    this.changeDetector.detectChanges();

  }

  ngOnDestroy(): void {
    this.#subscriptions.unsubscribe();
  }


  getDisplayedColumns(): string[] {
    return [UUID, BETREFF, ANZAHL_VERSENDET];
  }

  selectInfomail(infomail: Infomail) {
    this.infomailsFacade.selectInfomail(infomail);
  }
}