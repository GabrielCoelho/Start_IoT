import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Button,
  TextField,
  Typography,
  Paper,
  Stack,
  Alert,
  CircularProgress,
} from '@mui/material';
import { login } from '../../services/auth';

const LoginPage: React.FC = () => {
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async () => {
    setError('');
    setLoading(true);
    try {
      await login(email, senha);
      navigate('/home');
    } catch (err: any) {
      const msg = err?.response?.data?.message ?? 'E-mail ou senha incorretos.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') handleLogin();
  };

  return (
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

          <Stack spacing={2.5}>
            <TextField
              fullWidth
              label="E-mail institucional"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              onKeyDown={handleKeyDown}
              disabled={loading}
            />
            <TextField
              fullWidth
              label="Senha"
              type="password"
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
              onKeyDown={handleKeyDown}
              disabled={loading}
            />

            {error && (
              <Alert severity="error" sx={{ borderRadius: 2 }}>
                {error}
              </Alert>
            )}

            <Button
              variant="contained"
              fullWidth
              onClick={handleLogin}
              disabled={loading || !email || !senha}
              sx={{
                py: 1.5,
                bgcolor: '#C8102E',
                '&:hover': { bgcolor: '#9B0D23' },
                fontWeight: 700
              }}
            >
              {loading ? <CircularProgress size={22} sx={{ color: 'white' }} /> : 'Entrar →'}
            </Button>
          </Stack>

          <Alert severity="info" sx={{ mt: 4, borderRadius: 2 }}>
            Acesso restrito à equipe técnica.
          </Alert>
        </Paper>
      </Box>

    </Box>
  );
};

export { LoginPage };
