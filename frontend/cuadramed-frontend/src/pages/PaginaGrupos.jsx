import { useEffect, useState } from 'react'
import cliente from '../api/cliente'

export default function PaginaGrupos() {
    const [grupos, setGrupos] = useState([])
    const [error, setError] = useState('')
    const [especialidad, setEspecialidad] = useState('')
    const [especialidades, setEspecialidades] = useState([])
    const [idResponsable, setIdResponsable] = useState('')
    const [medicos, setMedicos] = useState([])
    const [nombre, setNombre] = useState('')

    useEffect(() => {
        async function cargar() {
            try{
                const respuesta = await cliente.get('/grupo')
                setGrupos(respuesta.data)

                const respuestaEspecialidades = await cliente.get('/especialidad')
                setEspecialidades(respuestaEspecialidades.data)

                const respuestaMedicos = await cliente.get('/medico')
                setMedicos(respuestaMedicos.data)
            } catch (err) {
                setError(err.response?.data?.error ?? 'No se pudieron cargar los datos')
            }
        }
        cargar()
    },[])

    async function crearGrupo(e) {
        e.preventDefault()
        setError('')

        try{
            const respuesta = await cliente.post('/grupo/alta', {
                nombre,
                especialidad,
                idResponsable: Number(idResponsable)
            })

            setGrupos([...grupos, respuesta.data])
            setNombre('')
            setIdResponsable('')
            setEspecialidad('')
        }catch (err){
            setError(err.response?.data?.error ?? 'No se pudo crear el grupo')
        }
    }

    return (
        <div>
            <h3>Grupos</h3>

            <form onSubmit={crearGrupo} style={{ marginBottom: '20px' }}>
                <input
                    value={nombre}
                    onChange={e => setNombre(e.target.value)}
                    placeholder="Nombre del grupo"
                    required
                />

                <select value={especialidad} onChange={e => setEspecialidad(e.target.value)} required>
                    <option value="">Elige una especialidad</option>
                    {especialidades.map(esp => <option key={esp} value={esp}>{esp}</option>)}
                </select>

                <select value={idResponsable} onChange={e => setIdResponsable(e.target.value)} required>
                    <option value="">Elige un responsable</option>
                    {medicos.map(m => <option key={m.id} value={m.id}>{m.nombre}</option>)}
                </select>

                <button type="submit">Crear grupo</button>
            </form>





            {error && <p style={{ color: 'red' }}>{error}</p>}

            {grupos.length === 0 && !error && <p>No hay grupos todavía.</p>}

            {grupos.length > 0 && (
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                    <thead>
                    <tr>
                        <th style={{ textAlign: 'left' }}>Nombre</th>
                        <th style={{ textAlign: 'left' }}>Especialidad</th>
                        <th style={{ textAlign: 'left' }}>Responsable</th>
                    </tr>
                    </thead>
                    <tbody>
                    {grupos.map(g => (
                        <tr key={g.id}>
                            <td>{g.nombre}</td>
                            <td>{g.especialidad}</td>
                            <td>{g.responsable?.nombre}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
        </div>
    )
}