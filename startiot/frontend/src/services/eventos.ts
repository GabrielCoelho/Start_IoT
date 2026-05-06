import axios from 'axios';

export interface EventoResponse {
  id: number;
  nome: string;
  descricao?: string;
}

export const listarEventos = () =>
  axios.get<EventoResponse[]>('/api/eventos').then((r) => r.data);
