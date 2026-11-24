import { useState } from "react";
import { History as HistoryIcon, Search } from "lucide-react";
import { ledgerAPI } from "../services/api";

export default function History() {
  const [accountId, setAccountId] = useState("");
  const [entries, setEntries] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const loadHistory = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    setEntries([]);

    try {
      const response = await ledgerAPI.getAccountHistory(accountId);
      setEntries(response.data);
    } catch (err) {
      setError(err.response?.data?.message || "Could not load transaction history.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="page-heading">
        <div>
          <h1>Ledger History</h1>
          <p>View the complete double-entry ledger for an account.</p>
        </div>
      </div>

      <div className="content-card">
        <div className="card-heading">
          <h3><HistoryIcon size={19} /> Account Transaction History</h3>
        </div>

        <form className="history-search" onSubmit={loadHistory}>
          <input
            type="number"
            min="1"
            placeholder="Enter Account ID"
            value={accountId}
            onChange={(e) => setAccountId(e.target.value)}
            required
          />
          <button className="primary-button" type="submit">
            <Search size={17} /> Search
          </button>
        </form>

        {error && <p className="error-message">{error}</p>}
        {loading && <p className="empty-state">Loading history...</p>}

        {!loading && entries.length > 0 && (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Reference</th>
                  <th>Transaction</th>
                  <th>Entry Type</th>
                  <th>Amount</th>
                  <th>Status</th>
                  <th>Date</th>
                </tr>
              </thead>
              <tbody>
                {entries.map((entry) => (
                  <tr key={entry.entryId}>
                    <td>{entry.transactionReference}</td>
                    <td>{entry.transactionType}</td>
                    <td>
                      <span className={`status ${entry.entryType.toLowerCase()}`}>
                        {entry.entryType}
                      </span>
                    </td>
                    <td>
                      {Number(entry.amount).toLocaleString("en-IN", {
                        style: "currency",
                        currency: entry.currency,
                      })}
                    </td>
                    <td>{entry.transactionStatus}</td>
                    <td>{new Date(entry.transactionDate).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {!loading && accountId && entries.length === 0 && !error && (
          <p className="empty-state">No ledger entries found.</p>
        )}
      </div>
    </div>
  );
}