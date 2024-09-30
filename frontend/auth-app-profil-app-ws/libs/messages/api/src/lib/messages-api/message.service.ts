import { Injectable, signal } from "@angular/core";
import { Message } from "./messages.model";


@Injectable({
    providedIn: 'root'
})
export class MessageService {

    #messageSignal = signal<Message | undefined>(undefined);

    get message() {
        return this.#messageSignal;
    }

    info(message: string) {
        this.#add({ content: message, level: 'INFO' });
        setTimeout(() => {
            this.clearMessage();
        }, 5000); // Clear after 5 seconds
    }

    warn(message: string) {
        this.#add({ content: message, level: 'WARN' });
    }

    error(message: string) {
        this.#add({ content: message, level: 'ERROR' });
    }

    clearMessage() {
        this.#messageSignal.set(undefined);
    }

    #add(message: Message) {
        this.#messageSignal.set(message);
    }
}