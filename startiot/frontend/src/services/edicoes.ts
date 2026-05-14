import axios from 'axios';

export type StatusEdicao = 'PLANEJADA' | 'EM_ANDAMENTO' | 'FINALIZADA' | 'CANCELADA';

export interface EdicaoRequest {
  eventoId: number;
  ano: number;
  dataEvento?: string;
  status: StatusEdicao;
}

export interface EdicaoResponse {
  id: number;
  eventoId: number;
  eventoNome: string;
  ano: number;
  dataEvento?: string;
  status: StatusEdicao;
  totalEquipes: number;
  totalBaterias: number;
  dataCriacao: string;
  dataAtualizacao: string;
}

export const listarEdicoesPorEvento = (eventoId: number) =>
  axios.get<EdicaoResponse[]>(`/api/edicoes?eventoId=${eventoId}`).then(r => r.data);

export const criarEdicao = (data: EdicaoRequest) =>
  axios.post<EdicaoResponse>('/api/edicoes', data).then(r => r.data);

export const atualizarStatusEdicao = (id: number, novoStatus: StatusEdicao) =>
  axios.patch<EdicaoResponse>(`/api/edicoes/${id}/status?novoStatus=${novoStatus}`).then(r => r.data);
