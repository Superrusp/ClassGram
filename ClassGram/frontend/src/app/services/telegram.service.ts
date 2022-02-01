import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class TelegramService {

  private url = '/api-telegram';

  constructor(private http: HttpClient) {
  }

  login(phoneNumber: string) {
    console.log('send Telegram login request');

    let body =  { phoneNumber: phoneNumber };
    let headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'X-Requested-With': 'XMLHttpRequest'
    });
    let options = ({headers});
    return this.http.post(this.url + '/login', body, options);
  }

  verifyLoginCode(loginCode: string) {
    console.log('send verification code');

    let body = { confirmationCode: loginCode };
    let headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'X-Requested-With': 'XMLHttpRequest'
    });
    let options = ({headers});
    return this.http.post(this.url + '/verify-code', body, options);
  }
}
