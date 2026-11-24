import { useEffect, useState } from "react";
import { Plus, RefreshCw, Wallet } from "lucide-react";
import { accountAPI, customerAPI } from "../services/api";
import Modal from "../components/Modal";

export default function Accounts() {
  const [customers, setCustomers] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [customerId, setCustomerId] = useState("");
  const [currency, setCurrency] = useState("INR");
  const [showModal, setShowModal] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const loadCustomers = async () => {
    try {
      const response = await customerAPI.getAll();
      setCustomers(response.data);
    } catch {
      setError("Could not load customers.");
    }
  };

  const loadAccounts = async (id = customerId) => {
    if (!id) {
      setAccounts([]);
      return;
    }

    setLoading(true);
    setError("");

    try {
      const response = await accountAPI.getByCustomer(id);
      setAccounts(response.data);
    } catch {
      setError("Could not load accounts for this customer.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCustomers();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setError("");

    try {
      await accountAPI.create({
        customerId: Number(customerId),
        currency,
      });
      setShowModal(false);
      await loadAccounts();
    } catch (err) {
      setError(err.response?.data?.message || "Could not create account.");
    }
  };

  const handleStatus = async (account) => {
    try {
      if (account.status === "FROZEN") {
        await accountAPI.unfreeze(account.id);
      } else {
        await accountAPI.freeze(account.id);
      }
      await loadAccounts();
    } catch (err) {
      setError(err.response?.data?.message || "Could not update account status.");
    }
  };

  return (
    <div>
      <div className="page-heading">
        <div>
          <h1>Accounts</h1>
          <p>Create and manage customer bank accounts.</p>
        </div>
        <button
          className="primary-button"
          onClick={() => setShowModal(true)}
          disabled={!customers.length}
        >
          <Plus size={17} /> Create Account
        </button>
      </div>

      {error && <p className="error-message">{error}</p>}

      <div className="content-card">
        <div className="card-heading">
          <h3><Wallet size={19} /> Account Directory</h3>
          <button className="icon-button" onClick={() => loadAccounts()}>
            <RefreshCw size={17} />
          </button>
        </div>

        <div className="filter-row">
          <label htmlFor="customerFilter">Select customer</label>
          <select
            id="customerFilter"
            value={customerId}
            onChange={(e) => {
              setCustomerId(e.target.value);
              loadAccounts(e.target.value);
            }}
          >
            <option value="">Choose a customer</option>
            {customers.map((customer) => (
              <option key={customer.id} value={customer.id}>
                {customer.fullName} (#{customer.id})
              </option>
            ))}
          </select>
        </div>

        {loading ? (
          <p className="empty-state">Loading accounts...</p>
        ) : accounts.length === 0 ? (
          <p className="empty-state">Select a customer to view their accounts.</p>
        ) : (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Account Number</th>
                  <th>Currency</th>
                  <th>Balance</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {accounts.map((account) => (
                  <tr key={account.id}>
                    <td>{account.accountNumber}</td>
                    <td>{account.currency}</td>
                    <td>{Number(account.balance).toLocaleString("en-IN", {
                      style: "currency",
                      currency: account.currency,
                    })}</td>
                    <td>
                      <span className={`status ${account.status.toLowerCase()}`}>
                        {account.status}
                      </span>
                    </td>
                    <td>
                      <button
                        className="secondary-button"
                        onClick={() => handleStatus(account)}
                      >
                        {account.status === "FROZEN" ? "Unfreeze" : "Freeze"}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {showModal && (
        <Modal title="Create Bank Account" onClose={() => setShowModal(false)}>
          <form onSubmit={handleCreate}>
            <div className="form-group">
              <label>Customer</label>
              <select
                value={customerId}
                onChange={(e) => setCustomerId(e.target.value)}
                required
              >
                <option value="">Select customer</option>
                {customers.map((customer) => (
                  <option key={customer.id} value={customer.id}>
                    {customer.fullName} (#{customer.id})
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label>Currency</label>
              <select
                value={currency}
                onChange={(e) => setCurrency(e.target.value)}
              >
                <option value="INR">INR</option>
                <option value="USD">USD</option>
                <option value="EUR">EUR</option>
              </select>
            </div>

            <button className="primary-button full-button" type="submit">
              Create Account
            </button>
          </form>
        </Modal>
      )}
    </div>
  );
}