import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  constructor(private auth: AuthService, private router: Router) {}

  onLogin(u: string, p: string) {
    this.auth.login({ username: u, password: p }).subscribe({
      next: (res) => {
        alert('Login Successful!');
        this.router.navigate(['/home']);
      },
      error: (err) => {
        alert('Login error: ' + (err.error?.message || err.message));
      }
    });
  }
}
