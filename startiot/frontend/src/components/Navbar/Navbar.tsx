import { useState } from 'react';
import {
  Box, Typography, Button, Stack,
  Drawer, List, ListItemButton, ListItemIcon, ListItemText,
  IconButton, Divider,
} from '@mui/material';
import { useNavigate, useLocation } from 'react-router-dom';
import LogoutIcon from '@mui/icons-material/Logout';
import HomeOutlinedIcon from '@mui/icons-material/HomeOutlined';
import TimerOutlinedIcon from '@mui/icons-material/TimerOutlined';
import EmojiEventsOutlinedIcon from '@mui/icons-material/EmojiEventsOutlined';
import WarningAmberOutlinedIcon from '@mui/icons-material/WarningAmberOutlined';
import CalendarMonthOutlinedIcon from '@mui/icons-material/CalendarMonthOutlined';
import BoltOutlinedIcon from '@mui/icons-material/BoltOutlined';
import SportsScoreIcon from '@mui/icons-material/SportsScore';
import CloseIcon from '@mui/icons-material/Close';
import { logout, getSession } from '../../services/auth';

const Navbar: any = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const session = getSession();
  const isOrganizador = session?.perfil === 'ADMIN' || session?.perfil === 'ORGANIZADOR';
  const [drawerOpen, setDrawerOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const isActive = (path: string) => location.pathname === path;

  const navItems = [
    { label: 'Início',        path: '/home',          icon: HomeOutlinedIcon,          show: true },
    { label: 'Cronometragem', path: '/cronometragem', icon: TimerOutlinedIcon,          show: true },
    { label: 'Ranking',       path: '/ranking',       icon: EmojiEventsOutlinedIcon,    show: true },
    { label: 'Edições',       path: '/edicoes',       icon: CalendarMonthOutlinedIcon,  show: isOrganizador },
    { label: 'Baterias',      path: '/baterias',      icon: BoltOutlinedIcon,           show: isOrganizador },
    { label: 'Validação',     path: '/penalidades',   icon: WarningAmberOutlinedIcon,   show: isOrganizador },
  ].filter(i => i.show);

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
        whiteSpace: 'nowrap',
        '&:hover': { bgcolor: 'rgba(200,16,46,0.3)', color: '#fff' },
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
        zIndex: 1000,
      }}>
        <Typography
          sx={{ color: '#C8102E', fontWeight: 800, fontSize: 15, letterSpacing: 0.5, cursor: 'pointer', flexShrink: 0 }}
          onClick={() => navigate('/home')}
        >
          🏁 START IoT
        </Typography>

        {/* Desktop */}
        <Stack direction="row" spacing={0.5} sx={{ flexGrow: 1, display: { xs: 'none', md: 'flex' } }}>
          {navItems.map(item => (
            <NavButton key={item.path} label={item.label} path={item.path} icon={item.icon} />
          ))}
        </Stack>

        <Box sx={{ flexGrow: 1, display: { xs: 'flex', md: 'none' } }} />

        {/* Desktop logout */}
        <Button
          onClick={handleLogout}
          startIcon={<LogoutIcon />}
          sx={{
            color: 'rgba(255,255,255,0.7)', fontSize: 12,
            '&:hover': { color: '#fff' },
            display: { xs: 'none', md: 'flex' },
          }}
        >
          Sair
        </Button>

        {/* Mobile hamburguer */}
        <IconButton
          onClick={() => setDrawerOpen(true)}
          sx={{
            color: '#fff', display: { xs: 'flex', md: 'none' },
            flexDirection: 'column', gap: 0, borderRadius: 1.5, px: 1,
          }}
        >
          <SportsScoreIcon sx={{ fontSize: 20 }} />
          <Typography sx={{ fontSize: 9, color: 'rgba(255,255,255,0.7)', lineHeight: 1, mt: '2px' }}>
            Menu
          </Typography>
        </IconButton>
      </Box>

      {/* Mobile Drawer */}
      <Drawer
        anchor="right"
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        PaperProps={{ sx: { width: 240 } }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', px: 2, py: 1.5 }}>
          <Typography sx={{ color: '#C8102E', fontWeight: 800, fontSize: 15, letterSpacing: 0.5 }}>
            🏁 START IoT
          </Typography>
          <IconButton onClick={() => setDrawerOpen(false)} sx={{ color: '#1A1A2E' }}>
            <CloseIcon />
          </IconButton>
        </Box>

        <Divider />

        <List sx={{ pt: 1 }}>
          {navItems.map(({ label, path, icon: Icon }) => (
            <ListItemButton
              key={path}
              onClick={() => { navigate(path); setDrawerOpen(false); }}
              sx={{
                borderRadius: 1.5, mx: 1, mb: 0.5,
                bgcolor: isActive(path) ? 'rgba(200,16,46,0.12)' : 'transparent',
                '&:hover': { bgcolor: 'rgba(200,16,46,0.08)' },
              }}
            >
              <ListItemIcon sx={{ minWidth: 36 }}>
                <Icon sx={{ fontSize: 18, color: isActive(path) ? '#C8102E' : '#1A1A2E' }} />
              </ListItemIcon>
              <ListItemText
                primary={label}
                primaryTypographyProps={{
                  fontSize: 14, fontWeight: isActive(path) ? 700 : 400,
                  color: isActive(path) ? '#C8102E' : '#1A1A2E',
                }}
              />
            </ListItemButton>
          ))}
        </List>

        <Divider sx={{ mt: 'auto' }} />

        <Box sx={{ p: 2 }}>
          <Button
            fullWidth onClick={() => { handleLogout(); setDrawerOpen(false); }}
            startIcon={<LogoutIcon />}
            sx={{
              color: '#1A1A2E', justifyContent: 'flex-start',
              textTransform: 'none', fontSize: 14,
              '&:hover': { bgcolor: 'rgba(0,0,0,0.05)' },
            }}
          >
            Sair
          </Button>
        </Box>
      </Drawer>
    </>
  );
};

export { Navbar };
