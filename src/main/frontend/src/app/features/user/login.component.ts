import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service'; // Yol senin yapına göre güncellendi

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  constructor(private auth: AuthService) {}

  onLogin(u: string, p: string) {
    this.auth.login({ username: u, password: p }).subscribe({
      next: (res) => {
        alert('Giriş Başarılı! JWT alındı ve saklandı.');
      },
      error: (err) => {
        alert('Giriş Başarısız: ' + (err.error?.message || err.message));
      }
    });
  }
}
