import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { WalletApiService } from '../../api/wallet-api.service';
import { WalletOverviewDto } from '../../api/wallet.models';
import { timeout } from 'rxjs/operators';
import { PageHeaderComponent } from '../../shared/page-header.components';
import { AuthService } from '../../core/auth.service';

@Component({
  standalone: true,
  imports: [CommonModule, PageHeaderComponent],
  templateUrl: './overview.page.html',
})
export class OverviewPage implements OnInit {
  data: WalletOverviewDto | null = null;
  loading = false;
  error: string | null = null;
  customerName: string | null = null;
  today = new Date();
  reload = () => this.load();

  constructor(
    private api: WalletApiService,
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load() {
    this.loading = true;
    this.error = null;

    this.api.getOverview()
      .pipe(timeout(8000))
      .subscribe({

        next: (res) => {
          this.data = res;
          this.customerName = res.accounts?.[0]?.accountName ?? null;
          this.loading = false;

          this.auth.setConnected(true);
        },
        error: (e) => {
          console.log('Overview load error', e);
          this.loading = false;
          this.error = 'Bitte zuerst anmelden';

          this.auth.setConnected(false);
        }
      });
  }

  openAccount(accountId: string) {
    void this.router.navigate(['/accounts', accountId]);
  }
}
