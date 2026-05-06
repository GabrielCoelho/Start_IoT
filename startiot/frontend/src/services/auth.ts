import axios from 'axios';

export type PerfilUsuario = 'ORGANIZADOR' | 'CRONOMETRISTA' | 'ADMIN';

export interface SessionUser {
  token: string;
  usuarioId: number;
  nomeUsuario: string;
  perfil: PerfilUsuario;
}

export async function login(email: string, senha: string): Promise<SessionUser> {
  const { data } = await axios.post('/api/auth/login', { email, senha });
  const session: SessionUser = {
    token: data.token,
    usuarioId: data.usuarioId,
    nomeUsuario: data.nomeUsuario,
    perfil: data.perfil,
  };
  localStorage.setItem('user_session', JSON.stringify(session));
  return session;
}

export function getSession(): SessionUser | null {
  const raw = localStorage.getItem('user_session');
  return raw ? JSON.parse(raw) : null;
}

export function logout(): void {
  localStorage.removeItem('user_session');
}
