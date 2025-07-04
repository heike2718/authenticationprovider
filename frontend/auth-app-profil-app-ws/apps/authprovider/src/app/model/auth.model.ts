import { ZweiPassworte } from "@ap-ws/common-model";

export const STORAGE_KEY_SESSION_EXPIRES_AT = 'auth_session_expires_at';
export const STORAGE_KEY_DEV_SESSION_ID = 'auth_dev_session_id';
export const HEADER_NAME_SESSION_ID = 'X-SESSIONID';



export interface ClientCredentials {
    accessToken: string;
    redirectUrl: string;
    state: string;
}

export interface ClientInformation {
    // clientId: string;
    name: string;
    zurueckText: string;
    agbUrl: string;
    loginnameSupported: boolean;
    namenRequired: boolean;
    baseUrl: string;
}

export interface AuthorizationCredentials {
    loginName: string;
    passwort: string;
    kleber?: string;
}

export interface LoginCredentials {
    authorizationCredentials: AuthorizationCredentials;
    clientCredentials: ClientCredentials;
}

export interface TwoPasswords {
    passwort: string;
    passwortWdh: string;
}

export interface TempPasswordCredentials {
    email: string;
    kleber?: string;
}

export interface TempPasswordResponseDto {
    readonly message: string;
}

export interface ChangeTempPasswordPayload {
    tokenId: string;
    tempPassword: string;
    email: string;
    kleber?: string;
    zweiPassworte: ZweiPassworte;

}

export interface User {
    uid: string;
    email: string;
}

export interface SignUpLogInResponseData {
    readonly state: string;
    readonly nonce?: string;
    readonly idToken: string;
    readonly oauthFlowType: string;
}


export function createHashForRedirectUrl(data: SignUpLogInResponseData): string {

    const nonce = data.nonce ? data.nonce : '';

    return '#state=' + data.state
        + '&nonce=' + nonce
        + '&idToken=' + data.idToken
        + '&oauthFlowType=' + data.oauthFlowType;
}
