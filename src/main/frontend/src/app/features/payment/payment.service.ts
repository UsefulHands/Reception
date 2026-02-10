import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PaymentRequest} from './payment.request';
import {ApiResponse} from '../../core/models/api/ApiResponse';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private readonly apiUrl = `${environment.apiUrl}/payments/process`;

  constructor(private http: HttpClient) {}

  processPayment(paymentDetails: PaymentRequest): Observable<any> {
    return this.http.post<ApiResponse<PaymentResponse>>(this.apiUrl, paymentDetails);
  }
}
