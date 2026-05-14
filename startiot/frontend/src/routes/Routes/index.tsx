import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { LoginPage } from '../../pages/Login/Login';
import { HomePage } from '../../pages/Home/Home';
import { CronometragemPage } from '../../pages/Cronometragem/Cronometragem';
import { RankingPage } from '../../pages/Ranking/Ranking';
import { UsuariosPage } from '../../pages/Usuarios/Usuarios';
import { EmConstrucaoPage } from '../../pages/EmConstrucao/EmConstrucao';
import { EquipesPage } from '../../pages/Equipes/Equipes';
import { BateriasPage } from '../../pages/Baterias/Baterias';
import { PenalidadesPage } from '../../pages/Penalidades/Penalidades';
import { EventosPage } from '../../pages/Eventos/Eventos';
import { EdicoesPage } from '../../pages/Edicoes/Edicoes';
import MainLayout from '../../layout/MainLayout';
import { getSession, type PerfilUsuario } from '../../services/auth';

const ROUTE_PROFILES: Record<string, PerfilUsuario[]> = {
  '/home':          ['ADMIN', 'ORGANIZADOR', 'CRONOMETRISTA'],
  '/eventos':       ['ADMIN'],
  '/edicoes':       ['ADMIN', 'ORGANIZADOR'],
  '/equipes':       ['ADMIN', 'ORGANIZADOR'],
  '/membros':       ['ADMIN', 'ORGANIZADOR'],
  '/carrinhos':     ['ADMIN', 'ORGANIZADOR'],
  '/baterias':      ['ADMIN', 'ORGANIZADOR'],
  '/penalidades':   ['ADMIN', 'ORGANIZADOR'],
  '/corridas':      ['ADMIN', 'ORGANIZADOR', 'CRONOMETRISTA'],
  '/cronometragem': ['ADMIN', 'ORGANIZADOR', 'CRONOMETRISTA'],
  '/ranking':       ['ADMIN', 'ORGANIZADOR', 'CRONOMETRISTA'],
  '/usuarios':      ['ADMIN'],
};

const PrivateRoute = ({ children }: { children: React.ReactNode }) => {
  const session = getSession();
  return session ? <>{children}</> : <Navigate to="/" />;
};

const ProfileRoute = ({ path, children }: { path: string; children: React.ReactNode }) => {
  const session = getSession();
  if (!session) return <Navigate to="/" />;

  const allowed = ROUTE_PROFILES[path] ?? [];
  if (!allowed.includes(session.perfil)) return <Navigate to="/home" />;

  return <>{children}</>;
};

export const AppRoutes = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LoginPage />} />

        <Route element={<PrivateRoute><MainLayout /></PrivateRoute>}>
          <Route path="/home"          element={<ProfileRoute path="/home"><HomePage /></ProfileRoute>} />
          <Route path="/cronometragem" element={<ProfileRoute path="/cronometragem"><CronometragemPage /></ProfileRoute>} />
          <Route path="/ranking"       element={<ProfileRoute path="/ranking"><RankingPage /></ProfileRoute>} />
          <Route path="/usuarios"      element={<ProfileRoute path="/usuarios"><UsuariosPage /></ProfileRoute>} />
          <Route path="/equipes"       element={<ProfileRoute path="/equipes"><EquipesPage /></ProfileRoute>} />
          <Route path="/baterias"      element={<ProfileRoute path="/baterias"><BateriasPage /></ProfileRoute>} />
          <Route path="/penalidades"   element={<ProfileRoute path="/penalidades"><PenalidadesPage /></ProfileRoute>} />
          <Route path="/corridas"      element={<ProfileRoute path="/corridas"><Navigate to="/baterias" /></ProfileRoute>} />
          <Route path="/eventos"       element={<ProfileRoute path="/eventos"><EventosPage /></ProfileRoute>} />
          <Route path="/edicoes"       element={<ProfileRoute path="/edicoes"><EdicoesPage /></ProfileRoute>} />
          <Route path="/membros"       element={<ProfileRoute path="/membros"><EmConstrucaoPage titulo="Gestão de Membros" /></ProfileRoute>} />
          <Route path="/carrinhos"     element={<ProfileRoute path="/carrinhos"><EmConstrucaoPage titulo="Vistoria Técnica de Carrinhos" /></ProfileRoute>} />
        </Route>

        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </BrowserRouter>
  );
};
