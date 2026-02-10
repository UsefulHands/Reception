import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import {LoginRequest} from './login.request';
import {ApiResponse} from '../../core/models/api/ApiResponse';
import {LoginResponse} from './login.response';

@Component({
  selector: 'app-login',
  standalone: true,
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  onLogin(username: string, password: string) {
    const credentials: LoginRequest = { username, password };

    this.authService.login(credentials).subscribe({
      next: (res: ApiResponse<string>) => {
        if (res.success) {
          alert('Login Successful!');
          const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/home';
          this.router.navigateByUrl(returnUrl);
        } else {
          alert('Login failed: ' + res.message);
        }
      },
      error: (err) => {
        alert('Login error: ' + (err.error?.message || err.message));
      }
    });
  }
}
