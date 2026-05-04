import { Box, Typography, Button, Stack } from '@mui/material';
import { useNavigate, useLocation } from 'react-router-dom';
import LogoutIcon from '@mui/icons-material/Logout';

const Navbar: any = () => {
  const navigate: any = useNavigate();
  const location: any = useLocation();

  const handleLogout: any = () => {
    localStorage.removeItem('user_session'); 
    navigate('/');
  };

  const isActive: any = (path: any) => location.pathname === path;

  const NavButton: any = ({ label, active, onClick }: any) => (
  <Button 
    onClick={onClick}
    sx={{ 
      color: active ? '#fff' : 'rgba(255,255,255,0.7)', 
      bgcolor: active ? 'rgba(200,16,46,0.5)' : 'transparent',
      fontSize: 12,
      px: 1.5,
      borderRadius: 1.5,
      textTransform: 'none',
      '&:hover': { bgcolor: 'rgba(200,16,46,0.3)', color: '#fff' }
    }}
  >
    {label}
  </Button>
);

  return (
    <>
    <Box sx={{ 
      bgcolor: '#1A1A2E', 
      height: 52, 
      display: 'flex', 
      alignItems: 'center', 
      px: 2, 
      gap: 1,
      position: 'sticky',
      top: 0,
      zIndex: 1000
    }}>
      <Typography sx={{ color: '#C8102E', fontWeight: 800, fontSize: 15, mr: 2, letterSpacing: 0.5 }}>
        🏁 START IoT
      </Typography>

      <Stack direction="row" spacing={1} sx={{ flexGrow: 1 }}>
        <NavButton label="Cronometragem" path="/cronometragem" active={isActive('/cronometragem')} onClick={() => navigate('/cronometragem')} />
        <NavButton label="Ranking" path="/ranking" active={isActive('/ranking')} onClick={() => navigate('/ranking')} />
      </Stack>

      <Button 
        onClick={handleLogout}
        startIcon={<LogoutIcon />}
        sx={{ color: 'rgba(255,255,255,0.7)', fontSize: 12, '&:hover': { color: '#fff' } }}
      >
        Sair
      </Button>
    </Box>
    </>
  );
};



export {Navbar};