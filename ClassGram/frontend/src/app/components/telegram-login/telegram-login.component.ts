import {Component, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ModalService} from '../../services/modal.service';
import {TelegramService} from '../../services/telegram.service';
import {MatStepper, MatVerticalStepper} from '@angular/material/stepper';

export interface INodes {
  title: string;
  seq: number;
  flowId: string;
}

@Component({
  selector: 'app-telegram-login',
  templateUrl: './telegram-login.component.html',
  styleUrls: ['./telegram-login.component.css']
})
export class TelegramLoginComponent implements OnInit {

  @ViewChild(MatVerticalStepper) vert_stepper: MatVerticalStepper;
  @ViewChild('stepper') private myStepper: MatStepper;

  public firstFormGroup: FormGroup;
  public secondFormGroup: FormGroup;

  constructor(
    private formBuilder: FormBuilder,
    private telegramService: TelegramService,
    private modalService: ModalService
  ) {

  }
  ngOnInit() {
    this.firstFormGroup = this.formBuilder.group({
      phoneNumber: ['', [Validators.required, Validators.min(10)]],
    });
    this.secondFormGroup = this.formBuilder.group({
      confirmationCode: ['', [Validators.required, Validators.min(5)]],
    });
  }

  sendLoginPhoneNumber() {
    let phoneNumber = this.firstFormGroup.get('phoneNumber').value;

    this.telegramService.login(phoneNumber).subscribe(resp => {
      console.log(resp);
    }, error => {
      console.log(error);
      this.modalService.newErrorModal(`Error while logging into Telegram!`, `An unexpected error occurred while logging into Telegram`, null)
    });
  }

  sendConfirmationCode() {
    let loginCode = this.secondFormGroup.get('confirmationCode').value;

    this.telegramService.verifyLoginCode(loginCode).subscribe(resp => {
      console.log(resp);
      this.modalService.newSuccessModal('Telegram account successfully linked!', ``, null);
    }, error => {
      this.modalService.newErrorModal(`Error while sending verification code into Telegram!`, ``, null)
    });
  }
}
