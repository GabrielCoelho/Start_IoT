import axios from 'axios';

export interface EquipeRequest {
  edicaoId: number;
  nome: string;
  curso?: string;
  categoria?: string;
}

export interface EquipeResponse {
  id: number;
  edicaoId: number;
  nome: string;
  curso?: string;
  categoria?: string;
  statusInscricao: 'PENDENTE' | 'APROVADA' | 'REPROVADA' | 'CANCELADA';
  totalMembros: number;
  carrinhoAprovado: boolean;
}

export const inscreverEquipe = (data: EquipeRequest) =>
  axios.post<EquipeResponse>('/api/equipes', data).then((r) => r.data);

export const listarEquipesPorEdicao = (edicaoId: number) =>
  axios.get<EquipeResponse[]>(`/api/equipes?edicaoId=${edicaoId}`).then((r) => r.data);

export const aprovarEquipe = (id: number) =>
  axios.patch<EquipeResponse>(`/api/equipes/${id}/aprovar`).then((r) => r.data);

export const reprovarEquipe = (id: number) =>
  axios.patch<EquipeResponse>(`/api/equipes/${id}/reprovar`).then((r) => r.data);
