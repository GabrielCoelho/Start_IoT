import axios from 'axios';

export interface EdicaoResponse {
  id: number;
  eventoId: number;
  ano: number;
  dataEvento?: string;
  status: string;
}

export const listarEdicoesPorEvento = (eventoId: number) =>
  axios.get<EdicaoResponse[]>(`/api/edicoes?eventoId=${eventoId}`).then((r) => r.data);
