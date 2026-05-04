import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { LoginPage } from '../../pages/Login/Login';
import { CronometragemPage } from '../../pages/Cronometragem/Cronometragem';
import { RankingPage } from '../../pages/Ranking/Ranking';
import MainLayout from '../../layout/MainLayout';

// const Placeholder: any = ({ title }: any) => (
//   <div style={{ padding: '20px' }}>
//     <h1>{title}</h1>
//     <p>Página em construção...</p>
//   </div>
// );

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
          <Route path="/cronometragem" element={<CronometragemPage />} />
          <Route path="/ranking" element={<RankingPage />} />
        </Route>

        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </BrowserRouter>
  );
};