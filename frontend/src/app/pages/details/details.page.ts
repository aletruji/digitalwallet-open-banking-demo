import { Component, Inject, OnInit, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { WalletApiService } from '../../api/wallet-api.service';
import { WalletAccountDetailsDto } from '../../api/wallet.models';
import { PageHeaderComponent } from '../../shared/page-header.components';



@Component({
  standalone: true,
  imports: [CommonModule, PageHeaderComponent],
  templateUrl: './details.page.html'
})
export class DetailsPage implements OnInit {
  data: WalletAccountDetailsDto | null = null;
  loading = false;
  error: string | null = null;
  accountId = '';
  sortAsc = false;
  reload = () => this.load();

  constructor(
    private api: WalletApiService,
    private route: ActivatedRoute

  ) {}

  ngOnInit(): void {
        this.route.paramMap.subscribe(pm => {
      this.accountId = pm.get('accountId') || '';
      this.load();
    });
  }


  toggleSort() {
    this.sortAsc = !this.sortAsc;
    this.load(false);
  }


  load(showSpinner: boolean = true) {
    if (!this.accountId) return;

    if (showSpinner) {
      this.loading = true;
    }

    this.error = null;

    const sort = this.sortAsc ? 'asc' : 'desc';

    this.api.getAccountDetails(this.accountId, sort)
      .pipe(finalize(() => {
        if (showSpinner) {
          this.loading = false;
        }
      }))
      .subscribe({
        next: (res) => {
          this.data = res;
        },
        error: (e) => {
          console.log('Detail load error', e);
          this.error = 'Bitte zuerst anmelden';
        }
      });
  }
}
