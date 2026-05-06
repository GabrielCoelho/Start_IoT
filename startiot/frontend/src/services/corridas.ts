import axios from 'axios';

export interface CorridaResponse {
  id: number;
  bateriaId: number;
  bateriaNumero: number;
  ordem: number;
  horarioInicio?: string;
  horarioFim?: string;
  status: 'AGUARDANDO' | 'EM_ANDAMENTO' | 'FINALIZADA' | 'CANCELADA' | 'DESCLASSIFICADA';
  totalRegistros: number;
}

export interface AlocacaoResponse {
  id: number;
  corridaId: number;
  equipeId: number;
  equipeNome: string;
  equipeCurso?: string;
}

export const criarCorrida       = (bateriaId: number, ordem: number) =>
  axios.post<CorridaResponse>('/api/corridas', { bateriaId, ordem }).then(r => r.data);

export const listarCorridas     = (bateriaId: number) =>
  axios.get<CorridaResponse[]>(`/api/corridas?bateriaId=${bateriaId}`).then(r => r.data);

export const iniciarCorrida     = (id: number) => axios.patch<CorridaResponse>(`/api/corridas/${id}/iniciar`).then(r => r.data);
export const finalizarCorrida   = (id: number) => axios.patch<CorridaResponse>(`/api/corridas/${id}/finalizar`).then(r => r.data);
export const cancelarCorrida    = (id: number) => axios.patch<CorridaResponse>(`/api/corridas/${id}/cancelar`).then(r => r.data);

export const listarAlocacoes    = (corridaId: number) =>
  axios.get<AlocacaoResponse[]>(`/api/corridas/${corridaId}/equipes`).then(r => r.data);

export const alocarEquipe       = (corridaId: number, equipeId: number) =>
  axios.post<AlocacaoResponse>(`/api/corridas/${corridaId}/equipes`, { equipeId }).then(r => r.data);

export const removerAlocacao    = (corridaId: number, equipeId: number) =>
  axios.delete(`/api/corridas/${corridaId}/equipes/${equipeId}`);
