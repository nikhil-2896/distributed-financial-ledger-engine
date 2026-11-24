
import { useEffect, useState } from "react";
import {
  Users,
  Wallet,
  ArrowLeftRight,
  IndianRupee,
  RefreshCw,
} from "lucide-react";

import { customerAPI, accountAPI, ledgerAPI } from "../services/api";
import StatCard from "../components/StatCard";

export default function Dashboard() {
  const [stats, setStats] = useState({
    customers: 0,
    accounts: 0,
    transactions: 0,
    balances: {},
  });

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadDashboard = async () => {
    setLoading(true);
    setError("");

    try {
      const customerResponse = await customerAPI.getAll();
      const customers = customerResponse.data;

      const accountResponses = await Promise.all(
        customers.map((customer) =>
          accountAPI.getByCustomer(customer.id)
        )
      );

      const accounts = accountResponses.flatMap(
        (response) => response.data
      );

      const historyResponses = await Promise.all(
        accounts.map((account) =>
          ledgerAPI.getAccountHistory(account.id)
        )
      );

      const entries = historyResponses.flatMap(
        (response) => response.data
      );

      const balances = {};

      accounts.forEach((account) => {
        const currency = account.currency;
        balances[currency] =
          (balances[currency] || 0) + Number(account.balance);
      });

      const uniqueTransactions = new Set(
        entries.map((entry) => entry.transactionReference)
      );

      setStats({
        customers: customers.length,
        accounts: accounts.length,
        transactions: uniqueTransactions.size,
        balances,
      });
    } catch (err) {
      console.error("Dashboard error:", err);
      setError("Unable to load dashboard data. Check API connection.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboard();
  }, []);

  const balanceText = Object.entries(stats.balances)
    .map(([currency, amount]) =>
      Number(amount).toLocaleString("en-IN", {
        style: "currency",
        currency,
        maximumFractionDigits: 2,
      })
    )
    .join(" | ");

  return (
    <div>
      <div className="page-heading">
        <div>
          <h1>Overview</h1>
          <p>Here's what's happening with your ledger today.</p>
        </div>

        <button
          className="secondary-button"
          onClick={loadDashboard}
          disabled={loading}
        >
          <RefreshCw size={16} /> Refresh
        </button>
      </div>

      {error && <p className="error-message">{error}</p>}

      <div className="stats-grid">
        <StatCard
          title="Total Customers"
          value={loading ? "..." : stats.customers}
          icon={Users}
          color="blue"
        />

        <StatCard
          title="Total Accounts"
          value={loading ? "..." : stats.accounts}
          icon={Wallet}
          color="purple"
        />

        <StatCard
          title="Transactions"
          value={loading ? "..." : stats.transactions}
          icon={ArrowLeftRight}
          color="orange"
        />

        <StatCard
          title="Account Balances"
          value={loading ? "..." : balanceText || "₹0.00"}
          icon={IndianRupee}
          color="green"
        />
      </div>

      <div className="welcome-card">
        <div>
          <span className="welcome-tag">FINANCIAL LEDGER ENGINE</span>
          <h2>Welcome to LedgerFlow</h2>
          <p>
            Manage customers, accounts and secure financial
            transactions from one place.
          </p>
        </div>

        <Wallet size={75} strokeWidth={1.2} />
      </div>
    </div>
  );
}
