import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Box, 
  Button, 
  TextField, 
  Typography, 
  Paper, 
  Stack, 
  ToggleButton, 
  ToggleButtonGroup,
  Alert, 
  Snackbar
} from '@mui/material';
import TimerOutlinedIcon from '@mui/icons-material/TimerOutlined';
import SettingsOutlinedIcon from '@mui/icons-material/SettingsOutlined';
import AdminPanelSettingsOutlinedIcon from '@mui/icons-material/AdminPanelSettingsOutlined';
import { validateLogin } from '../../services/auth';

const LoginPage: React.FC<any> = () => {
  const handleRoleChange = (_event: any, newRole: any): any => {
    if (newRole !== null) setRole(newRole);
  };

  const navigate: any = useNavigate();
  
  const [email, setEmail] = useState<any>('operador@fatec.sp.gov.br');
  const [password, setPassword] = useState<any>('');
  const [role, setRole] = useState<any>('cronometrista');
  const [error, setError] = useState<any>('');
  const [open, setOpen] = useState<any>(false);

  const handleLogin  = () => {
    setError(''); 

    const result = validateLogin(email, password, role);

    if (result.success) {
      console.log("Login realizado com sucesso:", result.user);
      localStorage.setItem('user_session', JSON.stringify(result.user));
      
      navigate('/cronometragem');
    } else {
      setError(result.message);
    }
  };

  const handleClose: any = (_event: any, reason: any) => {
    if (reason === 'clickaway') return;
    setOpen(false);
  };

  return (
    <>
    <Box sx={{ 
      minHeight: '100vh', 
      display: 'flex', 
      flexDirection: { xs: 'column', md: 'row' }, 
      bgcolor: '#F4F4F6' 
    }}>
      
      <Box sx={{ 
        flex: 1,
        bgcolor: '#1A1A2E', 
        display: { xs: 'none', md: 'flex' }, 
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        color: 'white',
        p: 4 
      }}>
        <Typography variant="h1" sx={{ fontSize: '4rem', mb: 2 }}>🏁</Typography>
        <Typography variant="h3" sx={{ fontWeight: 900, mb: 1 }}>START IoT</Typography>
        <Typography sx={{ opacity: 0.7, textAlign: 'center', maxWidth: 350, mb: 4 }}>
          Sistema de cronometragem inteligente para a Descida da Ladeira FATEC Mogi Mirim
        </Typography>
        
        <Box sx={{ bgcolor: 'rgba(200, 16, 46, 0.15)', border: '1px solid #C8102E', p: 3, borderRadius: 2, textAlign: 'center' }}>
          <Typography sx={{ color: '#C8102E', fontWeight: 'bold' }}>
            11ª Descida da Ladeira Fioravante Nesto
          </Typography>
          <Typography variant="body2" sx={{ opacity: 0.8 }}>
            📅 16 de Maio de 2026 · 07:45h
          </Typography>
        </Box>
      </Box>

      <Box sx={{ 
        flex: 1, 
        display: 'flex', 
        alignItems: 'center', 
        justifyContent: 'center', 
        p: 2 
      }}>
        <Paper elevation={3} sx={{ 
          p: { xs: 3, sm: 5 }, 
          width: '100%', 
          maxWidth: 420, 
          borderRadius: 3 
        }}>
          <Box sx={{ mb: 3 }}>
            <Typography variant="h5" sx={{ fontWeight: 800, color: '#1A1A2E' }}>
              Acesse o sistema
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Entre com suas credenciais de operador
            </Typography>
          </Box>

          <ToggleButtonGroup
            value={role}
            exclusive
            onChange={handleRoleChange}
            fullWidth
            sx={{ mb: 3 }}
          >
            <ToggleButton value="cronometrista" sx={{ py: 1.5, gap: 1 }}>
              <TimerOutlinedIcon /> <Box component="span" sx={{ fontSize: '0.75rem' }}>Crono</Box>
            </ToggleButton>
            <ToggleButton value="organizador" sx={{ py: 1.5, gap: 1 }}>
              <SettingsOutlinedIcon /> <Box component="span" sx={{ fontSize: '0.75rem' }}>Org</Box>
            </ToggleButton>
            <ToggleButton value="admin" sx={{ py: 1.5, gap: 1 }}>
              <AdminPanelSettingsOutlinedIcon /> <Box component="span" sx={{ fontSize: '0.75rem' }}>Admin</Box>
            </ToggleButton>
          </ToggleButtonGroup>

          <Stack spacing={2.5}>
            <TextField 
              fullWidth 
              label="E-mail institucional" 
              defaultValue="operador@fatec.sp.gov.br"
              sx={{ fontSize: '14px' }}
              value={email}
        onChange={(e: any) => setEmail(e.target.value)}
            />
            <TextField 
              fullWidth 
              label="Senha" 
              type="password"
              value={password}
              onChange={(e: any) => setPassword(e.target.value)}
            />
            <Button 
              variant="contained" 
              fullWidth 
              onClick={handleLogin}
              sx={{ 
                py: 1.5, 
                bgcolor: '#C8102E', 
                '&:hover': { bgcolor: '#9B0D23' },
                fontWeight: 700 
              }}
            >
              Entrar →
            </Button>
          </Stack>

          <Alert severity="info" sx={{ mt: 4, borderRadius: 2 }}>
            Acesso restrito a equipe técnica.
          </Alert>
        </Paper>
      </Box>

    </Box>

    <Snackbar 
          open={open} 
          autoHideDuration={4000} 
          onClose={handleClose}
          anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
        >
          <Alert onClose={handleClose} severity="error" variant="filled" sx={{ width: '100%' }}>
            {error}
          </Alert>
        </Snackbar>
</>
  );
};

export {LoginPage};