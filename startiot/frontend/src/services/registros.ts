import axios from 'axios';

export type TipoRegistro = 'LARGADA' | 'CHEGADA' | 'MANUAL' | 'AUTOMATICO';
export type TipoPenalidade = 'SIMPLES' | 'GRAVE';

export const PENALIDADE_MS: Record<TipoPenalidade, number> = {
  SIMPLES: 20_000,
  GRAVE:  120_000,
};

export const PENALIDADE_LABEL: Record<TipoPenalidade, string> = {
  SIMPLES: '+20s — Simples',
  GRAVE:   '+2min — Grave',
};

export interface RegistroTempoResponse {
  id: number;
  corridaId: number;
  equipeId: number;
  equipeNome: string;
  usuarioId: number;
  usuarioNome: string;
  tempoMilissegundos: number;
  tipoRegistro: TipoRegistro;
  validado: boolean;
  observacoes?: string;
  tipoPenalidade?: TipoPenalidade;
  motivoPenalidade?: string;
  tempoEfetivo: number;
}

export interface RegistroTempoRequest {
  corridaId: number;
  equipeId: number;
  tempoMilissegundos: number;
  tipoRegistro: TipoRegistro;
  observacoes?: string;
}

export interface PenalidadeRequest {
  tipo: TipoPenalidade;
  motivo?: string;
}

export const registrarTempo = (data: RegistroTempoRequest, usuarioId: number) =>
  axios.post<RegistroTempoResponse>('/api/registros-tempo', data, {
    headers: { 'X-Usuario-Id': usuarioId },
  }).then(r => r.data);

export const listarRegistrosCorrida = (corridaId: number) =>
  axios.get<RegistroTempoResponse[]>(`/api/registros-tempo?corridaId=${corridaId}`).then(r => r.data);

export const listarRegistrosEdicao = (edicaoId: number) =>
  axios.get<RegistroTempoResponse[]>(`/api/registros-tempo?edicaoId=${edicaoId}`).then(r => r.data);

export const aplicarPenalidade = (id: number, data: PenalidadeRequest) =>
  axios.patch<RegistroTempoResponse>(`/api/registros-tempo/${id}/penalidade`, data).then(r => r.data);

export const removerPenalidade = (id: number) =>
  axios.delete<RegistroTempoResponse>(`/api/registros-tempo/${id}/penalidade`).then(r => r.data);
