import { Component, OnInit } from '@angular/core';
import { environment } from '../environments/environment';
import { LogService } from 'hewi-ng-lib';

@Component({
	// tslint:disable-next-line:component-selector
	selector: 'app-root',
	templateUrl: './app.component.html',
	styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

	version = environment.version;
	envName = environment.envName;
	showEnv = !environment.production;
	api = environment.apiUrl;
	logo = environment.assetsUrl + '/mja_logo.png';
	innerWidth: number;

	constructor(private logger: LogService) { }

	ngOnInit() {
		const location = window.location;
		const hash = window.location.href;
		this.logger.debug('location=' + location);
		this.logger.debug('hash=' + hash);
		this.innerWidth = window.innerWidth;

		this.logger.debug('innerWidth=' + this.innerWidth);
	}
}

