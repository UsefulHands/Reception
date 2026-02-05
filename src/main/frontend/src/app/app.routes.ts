import { Routes } from '@angular/router';
import { LoginComponent } from './features/login/login.component';
import {RegisterComponent} from './features/register/register.component';
import { HomeComponent} from './features/home/home.component';
import {GuestComponent} from './features/guest/guest.component';
import { roleGuard } from './core/guards/role.guard';
import { AuditLogsComponent} from './features/auditLogs/auditLogs.component';
import {ReceptionistComponent} from './features/receptionist/receptionist.component';
import {AdminComponent} from './features/admin/admin.component';
import {RoomComponent} from './features/room/room.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'home', component: HomeComponent },
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  {
    path: 'guests',
    component: GuestComponent,
    canActivate: [roleGuard],
    data: { roles: ['ADMIN', 'RECEPTIONIST'] }
  },
  {
    path: 'rooms',
    component: RoomComponent
  },
  {
    path: 'receptionists',
    component: ReceptionistComponent,
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'admins',
    component: AdminComponent,
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'audit-logs',
    component: AuditLogsComponent,
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] }
  }
];
