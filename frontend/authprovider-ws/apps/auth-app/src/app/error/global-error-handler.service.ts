import { Injectable, ErrorHandler, Injector } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { LogPublishersService } from '../logger/log-publishers.service';
import { environment } from '../../environments/environment';
import { LogService } from '@authprovider-ws/common-logging';
import { MessageService } from '@authprovider-ws/common-messages';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {

	private logService: LogService;


	constructor(private injector: Injector) {

		// ErrorHandler wird vor allen anderen Injectables instanziiert,
		// so dass man benötigte Services nicht im Konstruktor injekten kann !!!
		const logPublishersService = this.injector.get(LogPublishersService);
		this.logService = this.injector.get(LogService);

		this.logService.initLevel(environment.loglevel);
		this.logService.registerPublishers(logPublishersService.publishers);
	}

	handleError(error: any): void {
		let message = 'Unerwarteter Fehler aufgetreten :/';

		if (error.message) {
			message += ': ' + error.message;
		}

		// try sending an Error-Log to the Server
		this.logService.error(message + ' (auth-app)');

		if (error instanceof HttpErrorResponse) {
			this.logService.debug('das sollte nicht vorkommen, da diese Errors vom ChecklistenService behandelt werden');
		} else {
			this.logService.error('Unerwarteter Fehler: ' + error.message);
		}

		this.injector.get(MessageService).error(message);

		// const router = this.injector.get(Router);
		// router.navigateByUrl('/error');
	}
}


