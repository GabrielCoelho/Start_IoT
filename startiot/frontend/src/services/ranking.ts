import axios from 'axios';

export interface RankingItemResponse {
  posicao: number;
  equipeId: number;
  equipeNome: string;
  equipeCurso: string;
  totalDescidas: number;
  melhorTempo: number;
  mediaTempo: number;
}

export interface RankingResponse {
  edicaoId: number;
  anoEdicao: number;
  nomeEvento: string;
  classificacao: RankingItemResponse[];
}

export const calcularRanking = (edicaoId: number) =>
  axios.get<RankingResponse>(`/api/ranking/${edicaoId}`).then(r => r.data);
