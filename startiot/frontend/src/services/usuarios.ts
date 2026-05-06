import axios from 'axios';
import type { PerfilUsuario } from './auth';

export interface UsuarioRequest {
  nome: string;
  email: string;
  senha: string;
  perfil: PerfilUsuario;
}

export interface UsuarioResponse {
  id: number;
  nome: string;
  email: string;
  perfil: PerfilUsuario;
  ativo: boolean;
}

export const PERFIL_LABELS: Record<PerfilUsuario, string> = {
  ADMIN: 'Administrador',
  ORGANIZADOR: 'Organizador',
  CRONOMETRISTA: 'Cronometrista',
};

export const criarUsuario = (data: UsuarioRequest) =>
  axios.post<UsuarioResponse>('/api/usuarios', data).then((r) => r.data);
