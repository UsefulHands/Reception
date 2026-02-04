import { Component } from '@angular/core';
import {AuthService} from '../../core/services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  constructor(private authService: AuthService, private router: Router) {}

  onRegister(username: string, password: string, fName: string, lName: string, phone: string, idNum: string) {
    const request = {
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
      next: (res: any) => {
        console.log('Registration successful!', res);
        alert('Registration Successful!');
        this.router.navigate(['/home']);
      },
      error: (err: any) => {
        alert('Registration error!');
        console.error('Registration failed', err);
      }
    });
  }
}
