import { Navigate } from 'react-router-dom'

export default function RutaProtegida({ usuario, children }) {
    if (!usuario) {
        return <Navigate to="/login" />
    }
    return children
}