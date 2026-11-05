export default function LoginForm({ email, setEmail, password, setPassword, handleSubmit, error }) {
  return (
    <form onSubmit={handleSubmit} style={{ border: '1px solid #ccc', padding: '20px', borderRadius: '8px' }}>
      {error && <p style={{ color: 'red', fontSize: '14px' }}>{error}</p>}

      <div style={{ marginBottom: '15px' }}>
        <label style={{ display: 'block', marginBottom: '5px' }}>Correo electrónico:</label>
        <input 
          type="email" 
          value={email} 
          onChange={(e) => setEmail(e.target.value)} 
          required 
          style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}
        />
      </div>

      <div style={{ marginBottom: '15px' }}>
        <label style={{ display: 'block', marginBottom: '5px' }}>Contraseña:</label>
        <input 
          type="password" 
          value={password} 
          onChange={(e) => setPassword(e.target.value)} 
          required 
          style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}
        />
      </div>

      <button 
        type="submit" 
        style={{ width: '100%', padding: '10px', backgroundColor: '#3182ce', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
      >
        Acceder
      </button>
    </form>
  )
}