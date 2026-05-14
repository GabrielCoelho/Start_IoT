import axios from 'axios';
import type { EquipeResponse } from './equipes';

export interface BateriaRequest {
  edicaoId: number;
  numero: number;
  tipo?: string;
  horarioPrevisto?: string;
}

export interface BateriaResponse {
  id: number;
  edicaoId: number;
  numero: number;
  tipo?: string;
  horarioPrevisto?: string;
  status: 'AGUARDANDO' | 'EM_ANDAMENTO' | 'FINALIZADA' | 'CANCELADA';
  totalCorridas: number;
}

export const criarBateria     = (data: BateriaRequest)  => axios.post<BateriaResponse>('/api/baterias', data).then(r => r.data);
export const listarBaterias   = (edicaoId: number)       => axios.get<BateriaResponse[]>(`/api/baterias?edicaoId=${edicaoId}`).then(r => r.data);
export const iniciarBateria   = (id: number)             => axios.patch<BateriaResponse>(`/api/baterias/${id}/iniciar`).then(r => r.data);
export const finalizarBateria = (id: number, posicaoCorte?: number) =>
  axios.patch<BateriaResponse>(`/api/baterias/${id}/finalizar`, posicaoCorte ? { posicaoCorte } : {}).then(r => r.data);
export const cancelarBateria  = (id: number)             => axios.patch<BateriaResponse>(`/api/baterias/${id}/cancelar`).then(r => r.data);
export const listarEquipesDisponiveisBateria = (bateriaId: number) =>
  axios.get<EquipeResponse[]>(`/api/baterias/${bateriaId}/equipes-disponiveis`).then(r => r.data);
