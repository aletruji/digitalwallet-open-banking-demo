import { Injectable } from '@angular/core';
import { WalletApiService } from '../api/wallet-api.service';
import { BehaviorSubject, Observable, tap } from 'rxjs';

const STORAGE_KEY = 'wallet_connected';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private connectedSubject = new BehaviorSubject<boolean>(
    localStorage.getItem(STORAGE_KEY) === 'true'
  );

  connected$ = this.connectedSubject.asObservable();

  constructor(private api: WalletApiService) {}

  login(): Observable<unknown> {
    return this.api.connect().pipe(
      tap(() => this.setConnected(true))
    );
  }

  logout(): Observable<void> {
    return this.api.logout().pipe(
      tap(() => this.setConnected(false))
    );
  }

  setConnected(v: boolean) {
    this.connectedSubject.next(v);
    if (v) localStorage.setItem(STORAGE_KEY, 'true');
    else localStorage.removeItem(STORAGE_KEY);
  }

  get connected(): boolean {
    return this.connectedSubject.value;
  }
}
