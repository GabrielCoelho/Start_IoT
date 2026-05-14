import axios from 'axios';

export interface EventoRequest {
  nome: string;
  descricao?: string;
}

export interface EventoResponse {
  id: number;
  nome: string;
  descricao?: string;
  totalEdicoes: number;
  dataCriacao: string;
  dataAtualizacao: string;
}

export const listarEventos   = () => axios.get<EventoResponse[]>('/api/eventos').then(r => r.data);
export const buscarEvento    = (id: number) => axios.get<EventoResponse>(`/api/eventos/${id}`).then(r => r.data);
export const criarEvento     = (data: EventoRequest) => axios.post<EventoResponse>('/api/eventos', data).then(r => r.data);
export const atualizarEvento = (id: number, data: EventoRequest) => axios.put<EventoResponse>(`/api/eventos/${id}`, data).then(r => r.data);
