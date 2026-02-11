import {Component, OnInit} from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthService} from '../../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {

  constructor(private authService: AuthService, private router: Router) {}
  currentRole: string = 'ANONYMOUS';
  get userRole(): string {
    const token = this.authService.getToken();

    if (!token) {
      return 'ANONYMOUS';
    }

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));

      return payload.role || 'GUEST';
    } catch (e) {
      return 'ANONYMOUS';
    }
  }

  logout() {
    localStorage.removeItem('token');
    alert("Log out successful!")
    this.authService.logout()
    this.router.navigate(['/home']);
  }

  ngOnInit() {

    this.authService.userRole$.subscribe(role => {
      this.currentRole = role;
    });
  }
}
