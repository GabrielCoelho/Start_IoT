import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { LoginPage } from '../../pages/Login/Login';
import { CronometragemPage } from '../../pages/Cronometragem/Cronometragem';
import { RankingPage } from '../../pages/Ranking/Ranking';
import MainLayout from '../../layout/MainLayout';
import { EquipesPage } from '../../pages/Equipes/Equipes';
import BateriasPage from '../../pages/Baterias/Baterias';
import { ExecucaoPage } from '../../pages/Execucao/Execucao';
import { ValidacaoPage } from '../../pages/Validacao/Validacao';

export const MENU_ITEMS = [
  { label: 'Equipes', path: '/equipes' },
  { label: 'Baterias', path: '/baterias' },
  // { label: 'Cronometragem', path: '/cronometragem' },
  { label: 'Ranking', path: '/ranking' },
  {label: 'Execução', path: '/execucao'},
  {label: 'Validação', path: '/validacao'}
];

export const PrivateRoute: any = ({ children }: any) => {
  const user = localStorage.getItem('user_session');
  return user ? children : <Navigate to="/" />;
};

export const AppRoutes: any = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LoginPage />} />

       <Route element={
          <PrivateRoute>
            <MainLayout />
          </PrivateRoute>
        }>
          <Route path="/cronometragem/:id" element={<CronometragemPage />} />
          <Route path="/ranking" element={<RankingPage />} />
          <Route path="/equipes" element={<EquipesPage />} />
          <Route path="/baterias" element={<BateriasPage />} />
          <Route path="/execucao" element={<ExecucaoPage />} />
          <Route path="/validacao" element={<ValidacaoPage />} />
        </Route>

        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </BrowserRouter>
  );
};