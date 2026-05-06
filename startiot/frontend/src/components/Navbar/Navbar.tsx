import { Box, Typography, Button, Stack } from '@mui/material';
import { useNavigate, useLocation } from 'react-router-dom';
import LogoutIcon from '@mui/icons-material/Logout';
import HomeOutlinedIcon from '@mui/icons-material/HomeOutlined';
import TimerOutlinedIcon from '@mui/icons-material/TimerOutlined';
import EmojiEventsOutlinedIcon from '@mui/icons-material/EmojiEventsOutlined';
import { logout } from '../../services/auth';

const Navbar: any = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const isActive = (path: string) => location.pathname === path;

  const NavButton: any = ({ label, path, icon: Icon }: any) => (
    <Button
      onClick={() => navigate(path)}
      startIcon={<Icon sx={{ fontSize: 16 }} />}
      sx={{
        color: isActive(path) ? '#fff' : 'rgba(255,255,255,0.7)',
        bgcolor: isActive(path) ? 'rgba(200,16,46,0.5)' : 'transparent',
        fontSize: 12,
        px: 1.5,
        borderRadius: 1.5,
        textTransform: 'none',
        '&:hover': { bgcolor: 'rgba(200,16,46,0.3)', color: '#fff' },
      }}
    >
      {label}
    </Button>
  );

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
      zIndex: 1000,
    }}>
      <Typography
        sx={{ color: '#C8102E', fontWeight: 800, fontSize: 15, mr: 2, letterSpacing: 0.5, cursor: 'pointer' }}
        onClick={() => navigate('/home')}
      >
        🏁 START IoT
      </Typography>

      <Stack direction="row" spacing={0.5} sx={{ flexGrow: 1 }}>
        <NavButton label="Início"         path="/home"          icon={HomeOutlinedIcon} />
        <NavButton label="Cronometragem"  path="/cronometragem" icon={TimerOutlinedIcon} />
        <NavButton label="Ranking"        path="/ranking"       icon={EmojiEventsOutlinedIcon} />
      </Stack>

      <Button
        onClick={handleLogout}
        startIcon={<LogoutIcon />}
        sx={{ color: 'rgba(255,255,255,0.7)', fontSize: 12, '&:hover': { color: '#fff' } }}
      >
        Sair
      </Button>
    </Box>
  );
};

export { Navbar };
