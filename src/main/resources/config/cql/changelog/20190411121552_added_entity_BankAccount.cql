
CREATE TABLE IF NOT EXISTS bankAccount (
    id uuid,
    userId text,
    bankName text,
    accountNumber text,
    currentbalance decimal,
    PRIMARY KEY(id)
);
