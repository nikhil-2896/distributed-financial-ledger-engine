import { BrowserRouter, Routes, Route, Navigate, useLocation } from "react-router-dom";
import Sidebar from "./components/Sidebar";
import Navbar from "./components/Navbar";
import Dashboard from "./pages/Dashboard";
import Customers from "./pages/Customers";
import Accounts from "./pages/Accounts";
import Transactions from "./pages/Transactions";
import History from "./pages/History";

function Layout() {
  const location = useLocation();

  const titles = {
    "/": "Dashboard",
    "/customers": "Customers",
    "/accounts": "Accounts",
    "/transactions": "Transactions",
    "/history": "Ledger History",
  };

  return (
    <div className="app-layout">
      <Sidebar />
      <main className="main-content">
        <Navbar title={titles[location.pathname] || "LedgerFlow"} />
        <div className="page-content">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/customers" element={<Customers />} />
            <Route path="/accounts" element={<Accounts />} />
            <Route path="/transactions" element={<Transactions />} />
            <Route path="/history" element={<History />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </div>
      </main>
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <Layout />
    </BrowserRouter>
  );
}