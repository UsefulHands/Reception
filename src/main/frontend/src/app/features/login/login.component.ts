import { Component } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
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
