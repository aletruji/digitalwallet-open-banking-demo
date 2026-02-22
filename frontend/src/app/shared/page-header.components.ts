import { Component, Input, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { finalize } from 'rxjs/operators';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './page-header.components.html'
})
export class PageHeaderComponent implements OnDestroy {
  @Input() title: string | null = null;

  @Input() showBack = false;
  @Input() backUrl: any[] = ['/'];
  @Input() leftLabel: string = 'Digitales Portemonnaie';
  @Input() showReload = false;
  @Input() reloadFn: (() => void) | null = null;
  @Input() loading = false;
  @Input() showAuthButton = true;


  connected = false;

  private sub: Subscription;

  constructor(
    private router: Router,
    private auth: AuthService
  ) {
    this.connected = this.auth.connected;
    this.sub = this.auth.connected$.subscribe(v => this.connected = v);
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  goBack(): void {
    if (this.showBack) {
      void this.router.navigate(this.backUrl);
    }
  }
// in overview reloadFn = reload is defined
  reload(): void {
      try {
      this.reloadFn?.();
    } catch (e) {
      console.error('reloadFn crashed', e);
    }
  }

  logoutOrLogin(): void {
    if (!this.connected) {
      void this.router.navigate(['/']);
      return;
    }

    this.loading = true;

    this.auth.logout()
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: () => void this.router.navigate(['/']),
        error: () => console.error('Logout fehlgeschlagen')
      });
  }
}
