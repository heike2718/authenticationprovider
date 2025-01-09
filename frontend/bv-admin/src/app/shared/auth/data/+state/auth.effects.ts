import { Injectable, inject } from "@angular/core";
import { Router } from "@angular/router";
import { Actions, createEffect, ofType } from "@ngrx/effects";
import { AuthHttpService } from "../auth-http.service";
import { authActions } from "./auth.actions";
import { catchError, map, of, switchMap, tap } from "rxjs";
import { Message } from "@bv-admin/shared/messages/api";
import { Session } from "@bv-admin/shared/auth/model";


@Injectable({
    providedIn: 'root'
})
export class AuthEffects {

    #actions = inject(Actions);
    #router = inject(Router);
    #authHttpService = inject(AuthHttpService);

    requestLoginUrl$ = createEffect(() => {

        return this.#actions.pipe(
            ofType(authActions.rEQUEST_LOGIN_URL),
            switchMap(() => this.#authHttpService.getLoginUrl()),
            map((message: Message) => authActions.rEDIRECT_TO_AUTH({ authUrl: message.message }))
        );

    });

    redirectToAuth$ = createEffect(() =>

        this.#actions.pipe(
            ofType(authActions.rEDIRECT_TO_AUTH),
            switchMap((action) => of(action.authUrl)),
            tap((authUrl) => window.location.href = authUrl)
        ), { dispatch: false });

    createSession$ = createEffect(() => {

        return this.#actions.pipe(
            ofType(authActions.iNIT_SESSION),
            switchMap(({ authResult }) =>
                this.#authHttpService.createSession(authResult)
            ),
            map((session: Session) => authActions.sESSION_CREATED({ session }))
        );
    });

    logOut$ = createEffect(() => {

        return this.#actions.pipe(
            ofType(authActions.lOG_OUT),
            switchMap(() =>
                this.#authHttpService.logOut()),
            map(() => authActions.lOGGED_OUT()),
            catchError(() => of(authActions.lOGGED_OUT()))
        );
    });

    loggedOut$ = createEffect(() =>
        this.#actions.pipe(
            ofType(authActions.lOGGED_OUT),
            tap(() => this.#router.navigateByUrl('/'))
        ), { dispatch: false });

}