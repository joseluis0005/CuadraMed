import { useState } from 'react'
import axios from 'axios'
import LoginForm from './components/LoginForm'
import UserPanel from './components/UserPanel'
import './App.css'

function sesionDesdeToken() {
    const token = localStorage.getItem('token')
    if (!token) return null

    const trozoDatos = token.split('.')[1]
    const datos = JSON.parse(atob(trozoDatos.replace(/-/g, '+').replace(/_/g, '/')))

    if (datos.exp * 1000 < Date.now()) {
        localStorage.removeItem('token')
        return null
    }

    return { id: datos.id, rol: datos.roles, token }
}

function App() {
  const [email, setEmail] = useState('admin@cuadramed.es')
  const [password, setPassword] = useState('admin123')
  const [usuario, setUsuario] = useState(sesionDesdeToken)
  const [error, setError] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    try {
      const respuesta = await axios.post('http://localhost:8080/api/auth/login', {
        email: email,
        password: password
      })

      setUsuario(respuesta.data)
      localStorage.setItem('token', respuesta.data.token)
    } catch (err) {
      if (err.response && err.response.status === 401) {
        setError('El usuario o la contraseña no son válidos')
      } else {
        setError('Error al conectar con el servidor Spring Boot')
      }
    }
  }

  const handleLogout = () => {
    setUsuario(null)
    localStorage.removeItem('token')
  }

  return (
    <div style={{ maxWidth: '400px', margin: '50px auto', fontFamily: 'sans-serif' }}>
      <h2>CuadraMed - HU-01 Login</h2>

      {usuario ? (
        <UserPanel 
          usuario={usuario} 
          handleLogout={handleLogout} 
        />
      ) : (
        <LoginForm 
          email={email}
          setEmail={setEmail}
          password={password}
          setPassword={setPassword}
          handleSubmit={handleSubmit}
          error={error}
        />
      )}
    </div>
  )
}

export default App