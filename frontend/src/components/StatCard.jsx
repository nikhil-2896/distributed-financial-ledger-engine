export default function StatCard({ title, value, icon: Icon, color }) {
  return (
    <div className="stat-card">
      <div>
        <p>{title}</p>
        <h2>{value}</h2>
      </div>

      <div className={`stat-icon ${color}`}>
        <Icon size={23} />
      </div>
    </div>
  );
}