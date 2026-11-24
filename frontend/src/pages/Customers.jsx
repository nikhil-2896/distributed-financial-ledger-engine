import { useEffect, useState } from "react";
import { Plus, RefreshCw, Users } from "lucide-react";
import { customerAPI } from "../services/api";
import Modal from "../components/Modal";
import FormInput from "../components/FormInput";

export default function Customers() {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({ fullName: "", email: "" });

  const loadCustomers = async () => {
    setLoading(true);
    setError("");

    try {
      const response = await customerAPI.getAll();
      setCustomers(response.data);
    } catch {
      setError("Could not load customers. Check your backend.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCustomers();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    try {
      await customerAPI.create(form);
      setForm({ fullName: "", email: "" });
      setShowModal(false);
      loadCustomers();
    } catch (err) {
      setError(err.response?.data?.message || "Could not create customer.");
    }
  };

  return (
    <div>
      <div className="page-heading">
        <div>
          <h1>Customers</h1>
          <p>Manage your registered customers.</p>
        </div>
        <button className="primary-button" onClick={() => setShowModal(true)}>
          <Plus size={17} /> Add Customer
        </button>
      </div>

      {error && <p className="error-message">{error}</p>}

      <div className="content-card">
        <div className="card-heading">
          <h3><Users size={19} /> Customer Directory</h3>
          <button className="icon-button" onClick={loadCustomers}>
            <RefreshCw size={17} />
          </button>
        </div>

        {loading ? (
          <p className="empty-state">Loading customers...</p>
        ) : customers.length === 0 ? (
          <p className="empty-state">No customers registered yet.</p>
        ) : (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Customer Name</th>
                  <th>Email Address</th>
                </tr>
              </thead>
              <tbody>
                {customers.map((customer) => (
                  <tr key={customer.id}>
                    <td>#{customer.id}</td>
                    <td>{customer.fullName}</td>
                    <td>{customer.email}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {showModal && (
        <Modal title="Add New Customer" onClose={() => setShowModal(false)}>
          <form onSubmit={handleSubmit}>
            <FormInput
              label="Full Name"
              value={form.fullName}
              onChange={(value) => setForm({ ...form, fullName: value })}
              placeholder="Enter customer name"
            />
            <FormInput
              label="Email Address"
              type="email"
              value={form.email}
              onChange={(value) => setForm({ ...form, email: value })}
              placeholder="Enter email address"
            />
            <button className="primary-button full-button" type="submit">
              Create Customer
            </button>
          </form>
        </Modal>
      )}
    </div>
  );
}