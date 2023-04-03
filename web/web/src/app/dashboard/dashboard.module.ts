import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardComponent } from './dashboard.component';
import {ReactiveFormsModule} from "@angular/forms";
import {YzModalModule} from "../share/yz-modal/yz-modal.module";



@NgModule({
  declarations: [DashboardComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    YzModalModule
  ],
  exports: [
    DashboardComponent
  ]
})
export class DashboardModule { }
