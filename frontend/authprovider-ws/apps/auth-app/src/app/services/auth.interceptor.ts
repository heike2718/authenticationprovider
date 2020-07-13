import { HttpInterceptor, HttpEvent, HttpRequest, HttpHandler } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { STORAGE_KEY_DEV_SESSION_ID, HEADER_NAME_SESSION_ID } from '../shared/model/auth-model';


@Injectable()
export class AuthInterceptor implements HttpInterceptor {

	intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

		if (environment.production) {
			return next.handle(req);
		}

		// da auf localhost das cookie nicht in den Browser gesetzt und folglich zurückgesendet werden kann,
		// machen wir hier den Umweg über localstorage.
		const sessionId = localStorage.getItem(STORAGE_KEY_DEV_SESSION_ID);
		if (sessionId) {
			const cloned = req.clone({
				headers: req.headers.set(HEADER_NAME_SESSION_ID, sessionId)
			});
			return next.handle(cloned);

		} else {
			return next.handle(req);
		}
	}

}
