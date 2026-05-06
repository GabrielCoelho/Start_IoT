import axios from 'axios';

export type TipoRegistro = 'LARGADA' | 'CHEGADA' | 'MANUAL' | 'AUTOMATICO';

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
}

export interface RegistroTempoRequest {
  corridaId: number;
  equipeId: number;
  tempoMilissegundos: number;
  tipoRegistro: TipoRegistro;
  observacoes?: string;
}

export const registrarTempo = (data: RegistroTempoRequest, usuarioId: number) =>
  axios.post<RegistroTempoResponse>('/api/registros-tempo', data, {
    headers: { 'X-Usuario-Id': usuarioId },
  }).then(r => r.data);

export const listarRegistrosCorrida = (corridaId: number) =>
  axios.get<RegistroTempoResponse[]>(`/api/registros-tempo?corridaId=${corridaId}`).then(r => r.data);
