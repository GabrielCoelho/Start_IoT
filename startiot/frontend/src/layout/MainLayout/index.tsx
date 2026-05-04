import { Box } from '@mui/material';
import { Outlet } from 'react-router-dom';
import { Navbar } from '../../components/Navbar/Navbar';

const MainLayout: any = () => {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <Navbar />
      <Box component="main" sx={{ flexGrow: 1 }}>
        <Outlet /> 
      </Box>
    </Box>
  );
};

export default MainLayout;