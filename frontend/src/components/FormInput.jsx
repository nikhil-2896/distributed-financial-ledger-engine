export default function FormInput({
  label,
  value,
  onChange,
  type = "text",
  placeholder,
  required = true,
}) {
  return (
    <div className="form-group">
      <label>{label}</label>
      <input
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        required={required}
      />
    </div>
  );
}