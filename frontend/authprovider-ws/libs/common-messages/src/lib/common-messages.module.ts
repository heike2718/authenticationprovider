import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MessagesComponent } from './messages/message.component';

@NgModule({
  imports: [
    CommonModule
  ],
  declarations: [
    MessagesComponent],
  exports: [
    MessagesComponent
  ]
})
export class CommonMessagesModule {}
