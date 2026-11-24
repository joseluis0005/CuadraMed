import { useEffect, useState } from 'react'
import cliente from '../api/cliente'

export default function PaginaGrupo() {
    const[grupos, setGrupos] = useState([])
    const [error, setError] = useState('')

    useEffect(() => {
        async function cargar() {
            try{
                const respuesta = await cliente.get('/grupo')
                setGrupos(respuesta.data)
            } catch (err) {
                setError(err.response?.data?.error ?? 'No se pudieron cargar los grupos')
            }
        }
        cargar()
    },[])

    return (
        <div>
            <h3>Grupos</h3>
            {error && <p style={{ color: 'red' }}>{error}</p>}

            {grupos.length === 0 && !error && <p>No hay grupos todavía.</p>}

            {grupos.length > 0 && (
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                    <thead>
                    <tr>
                        <th style={{ textAlign: 'left' }}>Nombre</th>
                        <th style={{ textAlign: 'left' }}>Especialidad</th>
                        <th style={{ textAlign: 'left' }}>Responsable</th>
                        <th style={{ textAlign: 'right' }}>Miembros</th>
                    </tr>
                    </thead>
                    <tbody>
                    {grupos.map(g => (
                        <tr key={g.id}>
                            <td>{g.nombre}</td>
                            <td>{g.especialidad}</td>
                            <td>{g.responsable.nombre}</td>
                            <td style={{ textAlign: 'right' }}>{g.miembros.length}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
        </div>
    )
}