export interface IBankAccount {
    id?: string;
    userId?: string;
    bankName?: string;
    accountNumber?: string;
    currentbalance?: number;
}

export class BankAccount implements IBankAccount {
    constructor(
        public id?: string,
        public userId?: string,
        public bankName?: string,
        public accountNumber?: string,
        public currentbalance?: number
    ) {}
}
