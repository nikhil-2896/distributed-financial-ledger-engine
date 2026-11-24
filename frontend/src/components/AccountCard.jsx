import { Wallet } from "lucide-react";

export default function AccountCard({ account }) {
  return (
    <div className="account-card">
      <div className="account-card-top">
        <Wallet size={22} />
        <span className={`status ${account.status.toLowerCase()}`}>
          {account.status}
        </span>
      </div>

      <p className="account-number">{account.accountNumber}</p>
      <h2>
        {Number(account.balance).toLocaleString("en-IN", {
          style: "currency",
          currency: account.currency,
        })}
      </h2>
      <p className="account-currency">{account.currency} Account</p>
    </div>
  );
}