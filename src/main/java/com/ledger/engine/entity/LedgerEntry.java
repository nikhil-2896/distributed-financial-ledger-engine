package com.ledger.engine.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private LedgerTransaction ledgerTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "system_account_code")
    private String systemAccountCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryType entryType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    public LedgerEntry() {
    }

    public Long getId() {
        return id;
    }

    public LedgerTransaction getLedgerTransaction() {
        return ledgerTransaction;
    }

    public void setLedgerTransaction(
            LedgerTransaction ledgerTransaction) {
        this.ledgerTransaction = ledgerTransaction;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getSystemAccountCode() {
        return systemAccountCode;
    }

    public void setSystemAccountCode(String systemAccountCode) {
        this.systemAccountCode = systemAccountCode;
    }

    public EntryType getEntryType() {
        return entryType;
    }

    public void setEntryType(EntryType entryType) {
        this.entryType = entryType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}