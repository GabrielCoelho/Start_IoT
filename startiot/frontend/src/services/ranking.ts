import axios from 'axios';

export interface BateriaInfo {
  bateriaId: number;
  numero: number;
  tipo?: string;
}

export interface BateriaTempoItem {
  bateriaId: number;
  bateriaNumero: number;
  bateriaTipo?: string;
  melhorTempo: number;
  totalDescidas: number;
}

export interface RankingItemResponse {
  posicao: number;
  equipeId: number;
  equipeNome: string;
  equipeCurso: string;
  totalDescidas: number;
  melhorTempo: number;
  ultimoTempo: number;
  mediaTempo: number;
  porBateria: BateriaTempoItem[];
}

export interface RankingResponse {
  edicaoId: number;
  anoEdicao: number;
  nomeEvento: string;
  baterias: BateriaInfo[];
  classificacao: RankingItemResponse[];
}

export const calcularRanking = (edicaoId: number) =>
  axios.get<RankingResponse>(`/api/ranking/${edicaoId}`).then(r => r.data);
