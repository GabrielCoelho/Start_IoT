import axios from 'axios';

export interface CarrinhoResponse {
  id: number;
  equipeId: number;
  equipeNome: string;
  identificacao?: string;
  aprovadoVistoria: boolean;
  penalideVistoria: boolean;
  observacoesVistoria?: string;
  dataVistoria?: string;
  dataCriacao: string;
  dataAtualizacao: string;
}

export interface VistoriaRequest {
  aprovado: boolean;
  penalidade?: boolean;
  observacoes?: string;
}

export const listarCarrinhosPorEdicao = (edicaoId: number) =>
  axios.get<CarrinhoResponse[]>(`/api/carrinhos?edicaoId=${edicaoId}`).then(r => r.data);

export const buscarCarrinhoPorEquipe = (equipeId: number) =>
  axios.get<CarrinhoResponse>(`/api/carrinhos/equipe/${equipeId}`).then(r => r.data);

export const registrarVistoria = (equipeId: number, data: VistoriaRequest) =>
  axios.patch<CarrinhoResponse>(`/api/carrinhos/equipe/${equipeId}/vistoria`, data).then(r => r.data);
