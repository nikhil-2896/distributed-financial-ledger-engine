
import { useEffect, useState } from "react";
import {
  ArrowLeftRight,
  Plus,
  AlertCircle,
  ShieldCheck,
  Play,
} from "lucide-react";
import {
  accountAPI,
  customerAPI,
  transactionAPI,
} from "../services/api";
import Modal from "../components/Modal";
import FormInput from "../components/FormInput";

export default function Transactions() {
  const [customers, setCustomers] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [error, setError] = useState("");
  const [transactionError, setTransactionError] = useState("");
  const [success, setSuccess] = useState("");
  const [demoLoading, setDemoLoading] = useState(false);
  const [demoResult, setDemoResult] = useState(null);

  const [form, setForm] = useState({
    customerId: "",
    transactionType: "DEPOSIT",
    accountId: "",
    destinationAccountId: "",
    amount: "",
  });

  const loadCustomers = async () => {
    try {
      const response = await customerAPI.getAll();
      setCustomers(response.data);
    } catch {
      setError("Could not load customers.");
    }
  };

  const loadAccounts = async (customerId) => {
    if (!customerId) {
      setAccounts([]);
      return;
    }

    try {
      const response = await accountAPI.getByCustomer(customerId);
      setAccounts(response.data);
    } catch {
      setError("Could not load accounts.");
    }
  };

  useEffect(() => {
    loadCustomers();
  }, []);

  const handleCustomerChange = async (customerId) => {
    setForm({
      ...form,
      customerId,
      accountId: "",
      destinationAccountId: "",
    });

    setTransactionError("");
    setDemoResult(null);
    await loadAccounts(customerId);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setTransactionError("");
    setSuccess("");

    const payload = {
      transactionType: form.transactionType,
      accountId: Number(form.accountId),
      amount: Number(form.amount),
    };

    if (form.transactionType === "TRANSFER") {
      payload.destinationAccountId =
        Number(form.destinationAccountId);
    }

    try {
      const response = await transactionAPI.create(payload);

      setSuccess(
        `Transaction successful! Reference: ${response.data.referenceCode}`
      );

      setShowModal(false);
      await loadAccounts(form.customerId);

      setForm({
        ...form,
        accountId: "",
        destinationAccountId: "",
        amount: "",
      });
    } catch (err) {
      const message =
        err.response?.data?.message ||
        err.response?.data?.error ||
        "Transaction failed. Please try again.";

      setTransactionError(message);
    }
  };

  const runIdempotencyTest = async () => {
    if (!selectedAccount) {
      setTransactionError("Please select an account first.");
      return;
    }

    setDemoLoading(true);
    setTransactionError("");
    setDemoResult(null);

    const idempotencyKey = crypto.randomUUID();

    const firstRequest = {
      transactionType: "DEPOSIT",
      accountId: Number(selectedAccount.id),
      amount: 1,
    };

    const secondRequest = {
      transactionType: "DEPOSIT",
      accountId: Number(selectedAccount.id),
      amount: 2,
    };

    try {
      const firstResponse = await transactionAPI.createWithKey(
        firstRequest,
        idempotencyKey
      );

      setDemoResult({
        firstStatus: firstResponse.status,
        firstMessage: "₹1 deposit processed successfully.",
        secondStatus: null,
        secondMessage: "Sending duplicate key with a different amount...",
        conflict: false,
      });

      await loadAccounts(form.customerId);

      try {
        await transactionAPI.createWithKey(
          secondRequest,
          idempotencyKey
        );

        setDemoResult((previous) => ({
          ...previous,
          secondStatus: "Unexpected success",
          secondMessage:
            "The second request was accepted. Check backend idempotency validation.",
          conflict: false,
        }));
      } catch (err) {
        const status = err.response?.status;
        const message =
          err.response?.data?.message ||
          err.response?.data?.error ||
          "The duplicate request was rejected.";

        setDemoResult((previous) => ({
          ...previous,
          secondStatus: status || "Request failed",
          secondMessage: message,
          conflict: status === 409,
        }));
      }
    } catch (err) {
      setDemoResult({
        firstStatus: err.response?.status || "Failed",
        firstMessage:
          err.response?.data?.message ||
          err.response?.data?.error ||
          "First request failed. Idempotency test could not continue.",
        secondStatus: null,
        secondMessage: "",
        conflict: false,
      });
    } finally {
      setDemoLoading(false);
    }
  };

  const selectedAccount = accounts.find(
    (account) => String(account.id) === form.accountId
  );

  const destinationAccount = accounts.find(
    (account) =>
      String(account.id) === form.destinationAccountId
  );

  const formatBalance = (account) => {
    if (!account) return "";
    return Number(account.balance).toLocaleString("en-IN", {
      style: "currency",
      currency: account.currency,
    });
  };

  const activeAccounts = accounts.filter(
    (account) => account.status === "ACTIVE"
  );

  return (
    <div>
      <div className="page-heading">
        <div>
          <h1>Transactions</h1>
          <p>Deposit, withdraw or transfer money.</p>
        </div>

        <button
          className="primary-button"
          onClick={() => {
            setTransactionError("");
            setDemoResult(null);
            setShowModal(true);
          }}
        >
          <Plus size={17} /> New Transaction
        </button>
      </div>

      {error && <p className="error-message">{error}</p>}
      {success && <p className="success-message">{success}</p>}

      <div className="content-card">
        <div className="card-heading">
          <h3>
            <ArrowLeftRight size={19} />
            Transaction Operations
          </h3>
        </div>

        <p className="empty-state">
          Select New Transaction to perform a deposit,
          withdrawal or transfer.
        </p>
      </div>

      {showModal && (
        <Modal
          title="Create Transaction"
          onClose={() => {
            setShowModal(false);
            setTransactionError("");
          }}
        >
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Customer</label>
              <select
                value={form.customerId}
                onChange={(e) =>
                  handleCustomerChange(e.target.value)
                }
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
              <label>Transaction Type</label>
              <select
                value={form.transactionType}
                onChange={(e) => {
                  setForm({
                    ...form,
                    transactionType: e.target.value,
                    accountId: "",
                    destinationAccountId: "",
                  });
                  setTransactionError("");
                }}
              >
                <option value="DEPOSIT">Deposit</option>
                <option value="WITHDRAWAL">Withdrawal</option>
                <option value="TRANSFER">Transfer</option>
              </select>
            </div>

            <div className="form-group">
              <label>
                {form.transactionType === "DEPOSIT"
                  ? "Deposit Into Account"
                  : form.transactionType === "WITHDRAWAL"
                  ? "Withdraw From Account"
                  : "Transfer From (Source Account)"}
              </label>

              <select
                value={form.accountId}
                onChange={(e) => {
                  setForm({
                    ...form,
                    accountId: e.target.value,
                  });
                  setTransactionError("");
                  setDemoResult(null);
                }}
                required
              >
                <option value="">Select account</option>
                {activeAccounts.map((account) => (
                  <option key={account.id} value={account.id}>
                    {account.accountNumber} — {formatBalance(account)}
                  </option>
                ))}
              </select>
            </div>

            {selectedAccount && (
              <div className="balance-preview">
                <span>Current account balance</span>
                <strong>{formatBalance(selectedAccount)}</strong>
              </div>
            )}

            {form.transactionType === "TRANSFER" && (
              <>
                <div className="form-group">
                  <label>Transfer To (Destination Account)</label>
                  <select
                    value={form.destinationAccountId}
                    onChange={(e) => {
                      setForm({
                        ...form,
                        destinationAccountId: e.target.value,
                      });
                      setTransactionError("");
                    }}
                    required
                  >
                    <option value="">Select destination account</option>
                    {activeAccounts
                      .filter(
                        (account) =>
                          String(account.id) !== form.accountId
                      )
                      .map((account) => (
                        <option
                          key={account.id}
                          value={account.id}
                        >
                          {account.accountNumber} — {formatBalance(account)}
                        </option>
                      ))}
                  </select>
                </div>

                {destinationAccount && (
                  <div className="balance-preview">
                    <span>Destination account balance</span>
                    <strong>{formatBalance(destinationAccount)}</strong>
                  </div>
                )}
              </>
            )}

            <FormInput
              label="Amount"
              type="number"
              value={form.amount}
              onChange={(value) => {
                setForm({ ...form, amount: value });
                setTransactionError("");
              }}
              placeholder="Enter amount"
            />

            {transactionError && (
              <div
                role="alert"
                style={{
                  display: "flex",
                  alignItems: "flex-start",
                  gap: "10px",
                  padding: "13px 14px",
                  margin: "16px 0",
                  backgroundColor: "#FEECEC",
                  border: "1px solid #F2B8B5",
                  borderRadius: "8px",
                  color: "#B42318",
                  fontSize: "14px",
                  fontWeight: "500",
                  lineHeight: "1.5",
                }}
              >
                <AlertCircle size={19} style={{ flexShrink: 0 }} />
                <span>{transactionError}</span>
              </div>
            )}

            <button
              className="primary-button full-button"
              type="submit"
              disabled={
                !form.accountId ||
                !form.customerId ||
                !form.amount ||
                (form.transactionType === "TRANSFER" &&
                  !form.destinationAccountId)
              }
            >
              Process Transaction
            </button>

            <div
              style={{
                marginTop: "22px",
                padding: "16px",
                border: "1px solid #DCE3ED",
                borderRadius: "10px",
                background: "#F8FAFC",
              }}
            >
              <h3
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: "8px",
                  margin: "0 0 8px",
                  fontSize: "15px",
                }}
              >
                <ShieldCheck size={18} />
                Idempotency Demonstration
              </h3>

              <p
                style={{
                  fontSize: "13px",
                  color: "#64748B",
                  margin: "0 0 12px",
                  lineHeight: "1.5",
                }}
              >
                Sends two deposits with the same idempotency key
                but different amounts to demonstrate duplicate
                request protection.
              </p>

              <button
                type="button"
                className="primary-button full-button"
                onClick={runIdempotencyTest}
                disabled={!selectedAccount || demoLoading}
              >
                <Play size={16} />
                {demoLoading ? "Running Test..." : "Run Idempotency Test"}
              </button>

              <p
                style={{
                  fontSize: "12px",
                  color: "#64748B",
                  margin: "9px 0 0",
                }}
              >
                Note: The first request creates a real ₹1 deposit.
              </p>

              {demoResult && (
                <div
                  style={{
                    marginTop: "14px",
                    padding: "13px",
                    borderRadius: "8px",
                    background: "#FFFFFF",
                    border: "1px solid #DCE3ED",
                    fontSize: "13px",
                    lineHeight: "1.6",
                  }}
                >
                  <div style={{ marginBottom: "12px" }}>
                    <strong>First Request</strong>
                    <div style={{ color: "#15803D" }}>
                      HTTP {demoResult.firstStatus} —{" "}
                      {demoResult.firstMessage}
                    </div>
                  </div>

                  {demoResult.secondStatus !== null && (
                    <div>
                      <strong>Second Request (Same Key)</strong>
                      <div
                        style={{
                          color: demoResult.conflict
                            ? "#B42318"
                            : "#64748B",
                          fontWeight: "600",
                        }}
                      >
                        HTTP {demoResult.secondStatus}
                      </div>
                      <div>{demoResult.secondMessage}</div>
                    </div>
                  )}

                  {demoResult.conflict && (
                    <div
                      style={{
                        marginTop: "12px",
                        padding: "9px",
                        borderRadius: "6px",
                        background: "#ECFDF3",
                        color: "#15803D",
                        fontWeight: "600",
                      }}
                    >
                      Idempotency protection working: duplicate
                      request rejected.
                    </div>
                  )}
                </div>
              )}
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}
