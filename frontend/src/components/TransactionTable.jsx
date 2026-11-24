export default function TransactionTable({ entries = [] }) {
  if (!entries.length) {
    return <p className="empty-state">No transactions found.</p>;
  }

  return (
    <div className="table-wrapper">
      <table>
        <thead>
          <tr>
            <th>Reference</th>
            <th>Type</th>
            <th>Entry</th>
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
  );
}