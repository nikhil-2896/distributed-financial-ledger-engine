
import axios from "axios";

const api = axios.create({
  baseURL: "/api",
  headers: {
    "Content-Type": "application/json",
  },
});

export const customerAPI = {
  create: (data) => api.post("/customers", data),
  getAll: () => api.get("/customers"),
};

export const accountAPI = {
  create: (data) => api.post("/accounts", data),
  getById: (id) => api.get(`/accounts/${id}`),
  getByCustomer: (customerId) =>
    api.get(`/accounts/customer/${customerId}`),
  freeze: (id) => api.patch(`/accounts/${id}/freeze`),
  unfreeze: (id) => api.patch(`/accounts/${id}/unfreeze`),
};

export const transactionAPI = {
  create: (data) =>
    api.post("/transactions", data, {
      headers: {
        "Idempotency-Key": crypto.randomUUID(),
      },
    }),

  createWithKey: (data, key) =>
    api.post("/transactions", data, {
      headers: {
        "Idempotency-Key": key,
      },
    }),

  getByReference: (reference) =>
    api.get(`/transactions/${reference}`),
};

export const ledgerAPI = {
  getAccountHistory: (accountId) =>
    api.get(`/ledger-entries/account/${accountId}`),
};

export default api;
