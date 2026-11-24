import { Bell, Search } from "lucide-react";

export default function Navbar({ title }) {
  return (
    <header className="navbar">
      <div>
        <h2>{title}</h2>
        <p>Manage your financial operations</p>
      </div>

      <div className="navbar-actions">
        <div className="search-box">
          <Search size={17} />
          <input placeholder="Search..." />
        </div>
        <button className="icon-button" aria-label="Notifications">
          <Bell size={20} />
        </button>
        <div className="avatar">A</div>
      </div>
    </header>
  );
}