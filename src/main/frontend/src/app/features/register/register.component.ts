import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import {ApiResponse} from '../../core/models/api/ApiResponse';
import {RegisterRequest} from './models/guest.model';

@Component({
  selector: 'app-register',
  standalone: true,
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onRegister(username: string, password: string, fName: string, lName: string, phone: string, idNum: string): void {
    const request: RegisterRequest = {
      username: username,
      password: password,
      guestDetails: {
        firstName: fName,
        lastName: lName,
        phoneNumber: phone,
        identityNumber: idNum
      }
    };

    this.authService.register(request).subscribe({
      next: (res: ApiResponse<any>) => {
        if (res.success) {
          alert(res.message || 'Registration Successful!');
          this.router.navigate(['/home']);
        } else {
          alert(res.message || 'Registration failed');
        }
      },
      error: (err) => {
        const errorMessage = err.error?.message || 'Registration error!';
        alert(errorMessage);
        console.error('Registration failed', err);
      }
    });
  }
}
