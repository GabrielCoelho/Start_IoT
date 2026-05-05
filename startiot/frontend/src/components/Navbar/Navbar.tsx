import { Box, Typography, Button, Stack } from '@mui/material';
import { useNavigate, useLocation } from 'react-router-dom';
import LogoutIcon from '@mui/icons-material/Logout';
import { MENU_ITEMS } from '../../routes/Routes';

const NavButton = ({ label, active, onClick }: {label: string; active: boolean; onClick: () => void}) => (
  <Button 
    onClick={onClick}
    sx={{ 
      color: active ? '#fff' : 'rgba(255,255,255,0.7)', 
      bgcolor: active ? 'rgba(200,16,46,0.5)' : 'transparent',
      fontSize: 12,
      px: 1.5,
      borderRadius: 1.5,
      textTransform: 'none',
      fontWeight: active ? 700 : 400,
      '&:hover': { bgcolor: 'rgba(200,16,46,0.3)', color: '#fff' }
    }}
  >
    {label}
  </Button>
);

const Navbar = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout: any = () => {
    localStorage.removeItem('user_session'); 
    navigate('/');
  };

  const isActive = (path: string) => location.pathname === path;

  return (
    <Box sx={{ 
      bgcolor: '#1A1A2E', 
      height: 52, 
      display: 'flex', 
      alignItems: 'center', 
      px: 2, 
      gap: 1,
      position: 'sticky',
      top: 0,
      zIndex: 1100
    }}>
      <Typography 
        onClick={() => navigate('/cronometragem')}
        sx={{ color: '#C8102E', fontWeight: 900, fontSize: 15, mr: 2, letterSpacing: 0.5, cursor: 'pointer' }}
      >
        🏁 START IoT
      </Typography>

      <Stack direction="row" spacing={1} sx={{ flexGrow: 1 }}>
        {MENU_ITEMS.map((item: any) => (
          <NavButton 
            key={item.path}
            label={item.label} 
            active={isActive(item.path)} 
            onClick={() => navigate(item.path)} 
          />
        ))}
      </Stack>

      <Button 
        onClick={handleLogout}
        startIcon={<LogoutIcon />}
        sx={{ color: 'rgba(255,255,255,0.7)', fontSize: 12, textTransform: 'none', '&:hover': { color: '#fff' } }}
      >
        Sair
      </Button>
    </Box>
  );
};

export { Navbar };