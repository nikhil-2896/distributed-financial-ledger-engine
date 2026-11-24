import { NavLink } from "react-router-dom";
import {
  LayoutDashboard,
  Users,
  Wallet,
  ArrowLeftRight,
  History,
  Landmark,
} from "lucide-react";

const links = [
  { name: "Dashboard", path: "/", icon: LayoutDashboard },
  { name: "Customers", path: "/customers", icon: Users },
  { name: "Accounts", path: "/accounts", icon: Wallet },
  { name: "Transactions", path: "/transactions", icon: ArrowLeftRight },
  { name: "History", path: "/history", icon: History },
];

export default function Sidebar() {
  return (
    <aside className="sidebar">
      <div className="brand">
        <Landmark size={25} />
        <span>LedgerFlow</span>
      </div>

      <p className="nav-heading">MAIN MENU</p>

      <nav>
        {links.map(({ name, path, icon: Icon }) => (
          <NavLink
            key={path}
            to={path}
            end={path === "/"}
            className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}
          >
            <Icon size={19} />
            <span>{name}</span>
          </NavLink>
        ))}
      </nav>

      <div className="sidebar-footer">Financial Ledger Engine</div>
    </aside>
  );
}