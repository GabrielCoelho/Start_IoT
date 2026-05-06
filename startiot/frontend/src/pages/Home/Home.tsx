import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Typography, Grid, Paper, Chip } from '@mui/material';
import EmojiEventsOutlinedIcon from '@mui/icons-material/EmojiEventsOutlined';
import CalendarMonthOutlinedIcon from '@mui/icons-material/CalendarMonthOutlined';
import GroupsOutlinedIcon from '@mui/icons-material/GroupsOutlined';
import PersonOutlinedIcon from '@mui/icons-material/PersonOutlined';
import DirectionsCarOutlinedIcon from '@mui/icons-material/DirectionsCarOutlined';
import BoltOutlinedIcon from '@mui/icons-material/BoltOutlined';
import LeaderboardOutlinedIcon from '@mui/icons-material/LeaderboardOutlined';
import TimerOutlinedIcon from '@mui/icons-material/TimerOutlined';
import AdminPanelSettingsOutlinedIcon from '@mui/icons-material/AdminPanelSettingsOutlined';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import { getSession, type PerfilUsuario } from '../../services/auth';

interface Module {
  label: string;
  description: string;
  icon: React.ElementType;
  path: string;
  color: string;
  allowedProfiles: PerfilUsuario[];
}

const MODULES: Module[] = [
  {
    label: 'Eventos',
    description: 'Cadastro e gestão dos eventos do campeonato',
    icon: EmojiEventsOutlinedIcon,
    path: '/eventos',
    color: '#C8102E',
    allowedProfiles: ['ADMIN'],
  },
  {
    label: 'Edições',
    description: 'Edições anuais, status e ciclo de vida',
    icon: CalendarMonthOutlinedIcon,
    path: '/edicoes',
    color: '#1565C0',
    allowedProfiles: ['ADMIN', 'ORGANIZADOR'],
  },
  {
    label: 'Equipes',
    description: 'Inscrições, aprovações e acompanhamento',
    icon: GroupsOutlinedIcon,
    path: '/equipes',
    color: '#2E7D32',
    allowedProfiles: ['ADMIN', 'ORGANIZADOR'],
  },
  {
    label: 'Membros',
    description: 'Integrantes de cada equipe participante',
    icon: PersonOutlinedIcon,
    path: '/membros',
    color: '#6A1B9A',
    allowedProfiles: ['ADMIN', 'ORGANIZADOR'],
  },
  {
    label: 'Vistoria Técnica',
    description: 'Cadastro e aprovação dos carrinhos IoT',
    icon: DirectionsCarOutlinedIcon,
    path: '/carrinhos',
    color: '#E65100',
    allowedProfiles: ['ADMIN', 'ORGANIZADOR'],
  },
  {
    label: 'Baterias',
    description: 'Fases da competição e grupos de corridas',
    icon: BoltOutlinedIcon,
    path: '/baterias',
    color: '#00838F',
    allowedProfiles: ['ADMIN', 'ORGANIZADOR'],
  },
  {
    label: 'Ranking',
    description: 'Classificação geral e resultados da edição',
    icon: LeaderboardOutlinedIcon,
    path: '/ranking',
    color: '#AD1457',
    allowedProfiles: ['ADMIN', 'ORGANIZADOR', 'CRONOMETRISTA'],
  },
  {
    label: 'Cronometragem',
    description: 'Registro de tempos em tempo real na pista',
    icon: TimerOutlinedIcon,
    path: '/cronometragem',
    color: '#1A1A2E',
    allowedProfiles: ['ADMIN', 'ORGANIZADOR', 'CRONOMETRISTA'],
  },
  {
    label: 'Usuários',
    description: 'Operadores, árbitros e administradores',
    icon: AdminPanelSettingsOutlinedIcon,
    path: '/usuarios',
    color: '#4E342E',
    allowedProfiles: ['ADMIN'],
  },
];

const ModuleCard: React.FC<{ mod: Module; blocked?: boolean }> = ({ mod, blocked = false }) => {
  const navigate = useNavigate();
  const Icon = mod.icon;

  return (
    <Paper
      elevation={0}
      onClick={() => !blocked && navigate(mod.path)}
      sx={{
        p: 4,
        borderRadius: 3,
        border: '1px solid',
        borderColor: blocked ? '#E0E0E6' : '#E0E0E6',
        cursor: blocked ? 'not-allowed' : 'pointer',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'flex-start',
        gap: 2,
        opacity: blocked ? 0.45 : 1,
        transition: 'all 0.2s',
        position: 'relative',
        ...(!blocked && {
          '&:hover': {
            borderColor: mod.color,
            boxShadow: `0 4px 20px ${mod.color}22`,
            transform: 'translateY(-2px)',
          },
        }),
      }}
    >
      <Box
        sx={{
          width: 52,
          height: 52,
          borderRadius: 2,
          bgcolor: blocked ? '#F0F0F0' : `${mod.color}18`,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <Icon sx={{ color: blocked ? '#BDBDBD' : mod.color, fontSize: 28 }} />
      </Box>

      <Box sx={{ flex: 1 }}>
        <Typography variant="subtitle1" sx={{ fontWeight: 800, color: blocked ? '#9E9E9E' : '#1A1A2E', mb: 0.5 }}>
          {mod.label}
        </Typography>
        <Typography variant="body2" sx={{ color: '#9A9AAF', lineHeight: 1.4 }}>
          {mod.description}
        </Typography>
      </Box>

      {blocked && (
        <LockOutlinedIcon
          sx={{
            position: 'absolute',
            top: 14,
            right: 14,
            fontSize: 16,
            color: '#BDBDBD',
          }}
        />
      )}
    </Paper>
  );
};

const HomePage: React.FC = () => {
  const session = getSession();
  const perfil = session?.perfil;

  const allowed = MODULES.filter((m) => perfil && m.allowedProfiles.includes(perfil));
  const blocked = MODULES.filter((m) => !perfil || !m.allowedProfiles.includes(perfil));

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#F4F4F6', p: { xs: 2, md: 4 } }}>

      <Box sx={{ mb: 4 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
          <Typography variant="h5" sx={{ fontWeight: 900, color: '#1A1A2E' }}>
            Painel Principal
          </Typography>
          {perfil && (
            <Chip
              label={perfil}
              size="small"
              sx={{ bgcolor: '#C8102E', color: 'white', fontWeight: 700, fontSize: '0.65rem' }}
            />
          )}
        </Box>
        <Typography variant="body2" color="text.secondary">
          {session ? `Bem-vindo, ${session.nomeUsuario}. ` : ''}
          Selecione um módulo para começar.
        </Typography>
      </Box>

      <Grid container spacing={2.5}>
        {allowed.map((mod) => (
          <Grid key={mod.path} size={{ xs: 12, sm: 6, md: 4 }}>
            <ModuleCard mod={mod} />
          </Grid>
        ))}

        {blocked.length > 0 && (
          <>
            <Grid size={{ xs: 12 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mt: 1 }}>
                <Box sx={{ flex: 1, height: '1px', bgcolor: '#E0E0E6' }} />
                <Typography variant="caption" sx={{ color: '#BDBDBD', fontWeight: 700, letterSpacing: 1, whiteSpace: 'nowrap' }}>
                  SEM PERMISSÃO DE ACESSO
                </Typography>
                <Box sx={{ flex: 1, height: '1px', bgcolor: '#E0E0E6' }} />
              </Box>
            </Grid>

            {blocked.map((mod) => (
              <Grid key={mod.path} size={{ xs: 12, sm: 6, md: 4 }}>
                <ModuleCard mod={mod} blocked />
              </Grid>
            ))}
          </>
        )}
      </Grid>

    </Box>
  );
};

export { HomePage };
