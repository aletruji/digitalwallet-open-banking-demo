import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { PageHeaderComponent } from '../../shared/page-header.components';


@Component({
  standalone: true,
  imports: [CommonModule, PageHeaderComponent],
  templateUrl: './login.page.html'
})
export class LoginPage {
  loading = false;
  error: string | null = null;

  constructor(
    private auth: AuthService,
    private router: Router
  ) {}

  login() {
    this.loading = true;
    this.error = null;

    this.auth.login().subscribe({
      next: () => {
        this.loading = false;
        this.router.navigateByUrl('/overview');
      },
      error: (e) => {
        this.loading = false;
        this.error = 'Anmelden hat nicht geklappt';
        console.error(e);
      }
    });
  }
}
