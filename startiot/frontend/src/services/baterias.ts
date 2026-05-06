import axios from 'axios';

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
export const finalizarBateria = (id: number)             => axios.patch<BateriaResponse>(`/api/baterias/${id}/finalizar`).then(r => r.data);
export const cancelarBateria  = (id: number)             => axios.patch<BateriaResponse>(`/api/baterias/${id}/cancelar`).then(r => r.data);
