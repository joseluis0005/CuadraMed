export default function UserPanel({ usuario, handleLogout }) {
  return (
    <div style={{ backgroundColor: '#e6fffa', padding: '15px', borderRadius: '8px' }}>
      <h3>¡Sesión iniciada con éxito!</h3>
      <p><strong>ID:</strong> {usuario.id}</p>
      <p><strong>Rol asignado:</strong> {usuario.rol}</p>
      <p style={{ wordBreak: 'break-all', fontSize: '12px' }}>
        <strong>Token JWT:</strong> {usuario.token}
      </p>
      <button 
        onClick={handleLogout}
        style={{ width: '100%', padding: '10px', backgroundColor: '#e53e3e', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
      >
        Cerrar Sesión
      </button>
    </div>
  )
}