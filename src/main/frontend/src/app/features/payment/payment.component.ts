import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReservationService } from '../reservation/reservation.service';
import { PaymentService } from './payment.service';
import {CardDetails} from './card.details';
import { PaymentRequest} from './payment.request';
import {ApiResponse} from '../../core/models/api/ApiResponse';

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './payment.component.html',
  styleUrls: ['./payment.component.css']
})
export class PaymentComponent implements OnInit {
  bookingData: any = null;
  isProcessing = false;

  cardDetails: CardDetails = {
    holderName: '',
    cardNumber: '',
    expiryDate: '',
    cvv: ''
  };

  constructor(
    private reservationService: ReservationService,
    private paymentService: PaymentService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.bookingData = this.reservationService.getTemporaryBooking();

    if (!this.bookingData) {
      this.router.navigate(['/rooms']);
    }
  }

  confirmPayment(): void {
    this.isProcessing = true;

    const paymentRequest: PaymentRequest = {
      reservation: this.bookingData,
      payment: {
        method: 'CREDIT_CARD',
        amount: this.bookingData.totalPrice
      }
    };

    this.paymentService.processPayment(paymentRequest).subscribe({
      next: (response: ApiResponse<PaymentResponse>) => {
        if (response.success) {
          alert(response.message || "Payment Successful!");
          this.reservationService.clearStorage();
          this.router.navigate(['/home']);
        } else {
          this.isProcessing = false;
          alert(response.message || "Payment could not be completed.");
        }
      },
      error: (err) => {
        this.isProcessing = false;
        const errorMessage = err.error?.message || "An internal error occurred.";
        alert("Error: " + errorMessage);
      }
    });
  }
}
