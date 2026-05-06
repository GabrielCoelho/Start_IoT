import React, { useState } from 'react';
import {
  Box,
  Typography,
  Paper,
  Stack,
  TextField,
  MenuItem,
  Button,
  Alert,
  CircularProgress,
  Divider,
} from '@mui/material';
import AdminPanelSettingsOutlinedIcon from '@mui/icons-material/AdminPanelSettingsOutlined';
import CheckCircleOutlinedIcon from '@mui/icons-material/CheckCircleOutlined';
import { criarUsuario, PERFIL_LABELS } from '../../services/usuarios';
import type { PerfilUsuario } from '../../services/auth';

const PERFIS: PerfilUsuario[] = ['ADMIN', 'ORGANIZADOR', 'CRONOMETRISTA'];

const EMPTY = { nome: '', email: '', senha: '', perfil: '' as PerfilUsuario | '' };

const UsuariosPage: React.FC = () => {
  const [form, setForm] = useState(EMPTY);
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  const set = (field: keyof typeof EMPTY) =>
    (e: React.ChangeEvent<HTMLInputElement>) =>
      setForm((prev) => ({ ...prev, [field]: e.target.value }));

  const valid =
    form.nome.trim().length > 0 &&
    form.email.trim().length > 0 &&
    form.senha.length >= 8 &&
    form.perfil !== '';

  const handleSubmit = async () => {
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      const criado = await criarUsuario({
        nome: form.nome.trim(),
        email: form.email.trim(),
        senha: form.senha,
        perfil: form.perfil as PerfilUsuario,
      });
      setSuccess(`Usuário "${criado.nome}" cadastrado com sucesso.`);
      setForm(EMPTY);
    } catch (err: any) {
      const msg = err?.response?.data?.message ?? 'Erro ao cadastrar usuário.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#F4F4F6', p: { xs: 2, md: 4 } }}>

      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 4 }}>
        <AdminPanelSettingsOutlinedIcon sx={{ color: '#4E342E', fontSize: 28 }} />
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 900, color: '#1A1A2E', lineHeight: 1 }}>
            Usuários
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Cadastro de operadores, árbitros e administradores do sistema
          </Typography>
        </Box>
      </Box>

      <Paper
        elevation={0}
        sx={{ maxWidth: 520, borderRadius: 3, border: '1px solid #E0E0E6', overflow: 'hidden' }}
      >
        <Box sx={{ bgcolor: '#1A1A2E', px: 3, py: 2 }}>
          <Typography variant="subtitle2" sx={{ color: 'white', fontWeight: 700 }}>
            Novo usuário
          </Typography>
        </Box>

        <Divider />

        <Stack spacing={2.5} sx={{ p: 3 }}>
          <TextField
            fullWidth
            label="Nome completo"
            value={form.nome}
            onChange={set('nome')}
            disabled={loading}
            inputProps={{ maxLength: 100 }}
          />

          <TextField
            fullWidth
            label="E-mail institucional"
            type="email"
            value={form.email}
            onChange={set('email')}
            disabled={loading}
            inputProps={{ maxLength: 150 }}
          />

          <TextField
            fullWidth
            label="Senha"
            type="password"
            value={form.senha}
            onChange={set('senha')}
            disabled={loading}
            helperText="Mínimo de 8 caracteres"
          />

          <TextField
            fullWidth
            select
            label="Tipo de usuário"
            value={form.perfil}
            onChange={set('perfil')}
            disabled={loading}
          >
            {PERFIS.map((p) => (
              <MenuItem key={p} value={p}>
                {PERFIL_LABELS[p]}
              </MenuItem>
            ))}
          </TextField>

          {success && (
            <Alert icon={<CheckCircleOutlinedIcon />} severity="success" sx={{ borderRadius: 2 }}>
              {success}
            </Alert>
          )}

          {error && (
            <Alert severity="error" sx={{ borderRadius: 2 }}>
              {error}
            </Alert>
          )}

          <Button
            variant="contained"
            fullWidth
            onClick={handleSubmit}
            disabled={!valid || loading}
            sx={{
              py: 1.5,
              bgcolor: '#4E342E',
              '&:hover': { bgcolor: '#3E2723' },
              fontWeight: 700,
            }}
          >
            {loading ? <CircularProgress size={22} sx={{ color: 'white' }} /> : 'Cadastrar usuário'}
          </Button>
        </Stack>
      </Paper>

    </Box>
  );
};

export { UsuariosPage };
