import { Jobstatus } from "@bv-admin-app/shared/model";

export interface Mailversandgruppe {
    readonly uuid: string;
    readonly idMailversandauftrag: string;
    readonly sortnr: number;
    readonly status: Jobstatus;
    readonly aenderungsdatum: string;
    readonly empfaengerUUIDs: string[];
}

export interface MailversandauftragOverview {
    readonly uuid: string;
    readonly idInfomailtext: string;
    readonly betreff: string;
    readonly erfasstAm: string;
    readonly status: Jobstatus;
    readonly anzahlEmpfaenger: number;
    readonly anzahlGruppen: number;
}

export interface MailversandauftragDetails {
    readonly uuid: string;
    readonly idInfomailtext: string;
    readonly versandJahrMonat: string;
    readonly status: Jobstatus;
    readonly erfasstAm: string;
    readonly versandBegonnenAm: string;
    readonly versandBeendetAm: string;
    readonly anzahlEmpfaenger: number;
    readonly anzahlVersendet: number;
    readonly versandMitFehlern: boolean;
    readonly mailversandgruppen: Mailversandgruppe[];
}

export interface DeleteMailversandauftragResponseDto {
    readonly uuid: string
}

export interface MailversandauftragDetailsResponseDto {
    readonly uuid: string;
    readonly versandauftrag: MailversandauftragDetails | undefined;
}


export function sortMailversandauftragOverviewByBetreff(versandauftraege: MailversandauftragOverview[]): MailversandauftragOverview[] {
    return versandauftraege.slice().sort((a, b) => a.betreff.localeCompare(b.betreff));
}

