import { Routes } from '@angular/router';
import { LoginPage } from './pages/login/login.page';
import { OverviewPage } from './pages/overview/overview.page';
import { DetailsPage } from './pages/details/details.page';

export const routes: Routes = [
  { path: '', component: LoginPage },
  { path: 'overview', component: OverviewPage },
  { path: 'accounts/:accountId', component: DetailsPage },
  { path: '**', redirectTo: '' }
];
