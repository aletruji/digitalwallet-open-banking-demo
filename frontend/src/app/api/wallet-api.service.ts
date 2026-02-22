import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { WalletAccountDetailsDto, WalletOverviewDto } from './wallet.models';

@Injectable({ providedIn: 'root' })
export class WalletApiService {

  constructor(private http: HttpClient) {}

  connect(): Observable<any> {
    return this.http.post('/api/wallet/connect', {});
  }

  getOverview(): Observable<WalletOverviewDto> {
    console.log('[API] Reload Overview', new Date().toISOString());
    return this.http.get<WalletOverviewDto>('/api/wallet/overview');
  }

  getAccountDetails(accountId: string, sort: 'asc' | 'desc' = 'desc'): Observable<WalletAccountDetailsDto> {
    console.log('[API] Reload Details', {
      time: new Date().toISOString(),
      accountId,
      sort
    });
    const params = new HttpParams().set('sort', sort);
    return this.http.get<WalletAccountDetailsDto>(`/api/wallet/accounts/${accountId}`, { params });
  }

  logout() {
    return this.http.delete<void>('/api/wallet/logout');
  }

}
