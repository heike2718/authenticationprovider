import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormGroup, AbstractControl, Validators, FormControl } from '@angular/forms';
import { Observable, Subscription } from 'rxjs';
import { User, ProfileDataPayload } from '../shared/model/profil.model';
import { store } from '../shared/store/app-data';
import { UserService } from '../services/user.service';
import { RemoteValidatorService } from '../services/remote-validator.service';
import { map } from 'rxjs/operators';
import { MessageService, ResponsePayload } from '@authprovider-ws/common-messages';
import { LogService } from '@authprovider-ws/common-logging';
import { trimString } from '@authprovider-ws/common-components';

@Component({
	selector: 'prfl-base-data',
	templateUrl: './base-data.component.html',
	styleUrls: ['./base-data.component.css']
})
export class BaseDataComponent implements OnInit, OnDestroy {

	user$: Observable<User>;

	changeDataForm: FormGroup;

	loginName: AbstractControl;

	vorname: AbstractControl;

	nachname: AbstractControl;

	email: AbstractControl;

	private userSubscription: Subscription;

	private blockingIndicatorSubscription: Subscription;

	private csrfTokenSubscription: Subscription;

	private cachedUser: User;

	private csrfToken = '';

	showBlockingIndicator: boolean;

	showLoginName: boolean;

	constructor(private userService: UserService
		, private validationService: RemoteValidatorService
		, private messageService: MessageService
		, private logger: LogService) {


	}

	ngOnInit() {

		this.user$ = store.user$;

		this.userSubscription = this.user$.subscribe(
			user => {
				this.cachedUser = user;

				this.showLoginName = user.email !== user.loginName;

				if (this.showLoginName) {

					this.changeDataForm = new FormGroup({
						loginName: new FormControl('', {
							validators: [Validators.required, Validators.maxLength(255)],
							asyncValidators: [this.forbiddenLoginName.bind(this)],
							// updateOn: 'blur'
						}),
						email: new FormControl('', {
							validators: [Validators.required, Validators.email],
							// asyncValidators: [this.forbiddenEmail.bind(this)],
							// updateOn: 'blur'
						}),
						vorname: new FormControl('', { validators: [Validators.required, Validators.maxLength(100)] }),
						nachname: new FormControl('', { validators: [Validators.required, Validators.maxLength(100)] }),
					});

					this.loginName = this.changeDataForm.controls.loginName;
					this.vorname = this.changeDataForm.controls.vorname;
					this.nachname = this.changeDataForm.controls.nachname;
					this.email = this.changeDataForm.controls.email;
				} else {
					this.changeDataForm = new FormGroup({
						email: new FormControl('', {
							validators: [Validators.required, Validators.email],
							// asyncValidators: [this.forbiddenEmail.bind(this)],
							// updateOn: 'blur'
						}),
						vorname: new FormControl('', { validators: [Validators.required, Validators.maxLength(100)] }),
						nachname: new FormControl('', { validators: [Validators.required, Validators.maxLength(100)] }),
					});

					this.vorname = this.changeDataForm.controls.vorname;
					this.nachname = this.changeDataForm.controls.nachname;
					this.email = this.changeDataForm.controls.email;
				}

				this.changeDataForm.patchValue(user);
			}

		);

		this.blockingIndicatorSubscription = store.blockingIndicator$.subscribe(
			value => this.showBlockingIndicator = value
		);

		this.csrfTokenSubscription = store.csrfToken$.subscribe(
			token => this.csrfToken = token
		);
	}

	ngOnDestroy() {
		if (this.userSubscription) {
			this.userSubscription.unsubscribe();
		}
		if (this.blockingIndicatorSubscription) {
			this.blockingIndicatorSubscription.unsubscribe();
		}
		if (this.csrfTokenSubscription) {
			this.csrfTokenSubscription.unsubscribe();
		}
	}

	submit(): void {

		let data = {};

		const emailValue = trimString(this.email.value);

		if (this.showLoginName) {
			data = {
				loginName: trimString(this.loginName.value),
				email: emailValue,
				nachname: trimString(this.nachname.value),
				vorname: trimString(this.vorname.value)
			};
		} else {
			data = {
				loginName: emailValue,
				email: emailValue,
				nachname: trimString(this.nachname.value),
				vorname: trimString(this.vorname.value)
			};
		}

		this.messageService.clear();
		this.userService.changeProfileData(data as ProfileDataPayload, this.cachedUser, this.csrfToken);

	}

	cancel(): void {
		this.userService.resetUser(this.cachedUser);
		this.messageService.clear();
	}

	forbiddenEmail(control: FormControl): Promise<any> | Observable<any> {
		const promise = new Promise<any>((resolve, _reject) => {
			const email = control.value;

			if (this.csrfToken && this.csrfToken.length > 0) {

				this.validationService.validate('email', email, this.csrfToken).pipe(
					map(res => res as ResponsePayload)
				).subscribe(
					payload => {
						const messagePayload = payload.message;

						if ('ERROR' === messagePayload.level) {
							resolve({ emailKnown: true });
						} else {
							resolve(null);
						}
					},
					error => {
						this.logger.debug(error);
						resolve(null);
					}
				);
			} else {
				resolve(null);
			}
		});
		return promise;
	}

	forbiddenLoginName(control: FormControl): Promise<any> | Observable<any> {
		const promise = new Promise<any>((resolve, _reject) => {
			const loginName = control.value;

			if (this.csrfToken && this.csrfToken.length > 0) {

				this.validationService.validate('loginname', loginName, this.csrfToken).pipe(
					map(res => res as ResponsePayload)
				).subscribe(
					payload => {
						const messagePayload = payload.message;

						if ('ERROR' === messagePayload.level) {
							resolve({ loginNameKnown: true });
						} else {
							resolve(null);
						}
					},
					error => {
						this.logger.debug(error);
						resolve(null);
					}
				);
			} else {
				resolve(null);
			}
		});
		return promise;
	}
}
