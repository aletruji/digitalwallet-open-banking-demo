export interface WalletAccountDto {
  accountId: string;
  bankName: string;
  accountName: string;
  accountSubType?: string;
  currency: string;
  balance: number;
  isNegative: boolean;
}

export interface WalletOverviewDto {
  accounts: WalletAccountDto[];
  totalAmount: number;
  totalIsNegative: boolean;
}

export interface WalletTransactionDto {
  date: string;
  amount: number;
  currency: string;
  counterparty: string;
  isNegative: boolean;
}

export interface WalletAccountDetailsDto {
  accountId: string;
  bankName: string;
  accountName: string;
  accountSubType?: string;
  currency: string;
  balance: number;
  isNegative: boolean;
  transactions: WalletTransactionDto[];
}
