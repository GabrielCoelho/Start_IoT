import React, { useCallback, useEffect, useState } from 'react';
import {
  Alert, Box, Button, CircularProgress, Dialog, DialogActions,
  DialogContent, DialogTitle, IconButton, Paper, Stack,
  TextField, Tooltip, Typography,
} from '@mui/material';
import CelebrationOutlinedIcon from '@mui/icons-material/CelebrationOutlined';
import AddOutlinedIcon from '@mui/icons-material/AddOutlined';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import EventNoteOutlinedIcon from '@mui/icons-material/EventNoteOutlined';
import {
  listarEventos, criarEvento, atualizarEvento,
  type EventoRequest, type EventoResponse,
} from '../../services/eventos';

const EMPTY: EventoRequest = { nome: '', descricao: '' };

const EventosPage: React.FC = () => {
  const [eventos, setEventos] = useState<EventoResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogAberto, setDialogAberto] = useState(false);
  const [editando, setEditando] = useState<EventoResponse | null>(null);
  const [form, setForm] = useState<EventoRequest>(EMPTY);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const reload = useCallback(() => {
    listarEventos().then(setEventos).catch(() => {}).finally(() => setLoading(false));
  }, []);

  useEffect(() => { reload(); }, [reload]);

  const abrirCriar = () => {
    setEditando(null);
    setForm(EMPTY);
    setError('');
    setDialogAberto(true);
  };

  const abrirEditar = (ev: EventoResponse) => {
    setEditando(ev);
    setForm({ nome: ev.nome, descricao: ev.descricao ?? '' });
    setError('');
    setDialogAberto(true);
  };

  const fechar = () => {
    if (saving) return;
    setDialogAberto(false);
    setEditando(null);
    setForm(EMPTY);
    setError('');
  };

  const handleSalvar = async () => {
    setError('');
    setSaving(true);
    try {
      const data: EventoRequest = {
        nome: form.nome.trim(),
        descricao: form.descricao?.trim() || undefined,
      };
      if (editando) {
        await atualizarEvento(editando.id, data);
        setSuccess(`Evento "${data.nome}" atualizado.`);
      } else {
        const novo = await criarEvento(data);
        setSuccess(`Evento "${novo.nome}" criado com sucesso.`);
      }
      fechar();
      reload();
    } catch (err: any) {
      setError(err?.response?.data?.message ?? 'Erro ao salvar evento.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#F4F4F6', p: { xs: 2, md: 4 } }}>

      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 4 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <CelebrationOutlinedIcon sx={{ color: '#7B1FA2', fontSize: 28 }} />
          <Box>
            <Typography variant="h5" sx={{ fontWeight: 900, color: '#1A1A2E', lineHeight: 1 }}>
              Eventos
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Gerenciamento de eventos do campeonato Start IoT
            </Typography>
          </Box>
        </Box>
        <Button variant="contained" startIcon={<AddOutlinedIcon />} onClick={abrirCriar}
          sx={{ bgcolor: '#7B1FA2', '&:hover': { bgcolor: '#6A1B9A' }, fontWeight: 700 }}>
          Novo evento
        </Button>
      </Box>

      {success && (
        <Alert severity="success" sx={{ mb: 2, borderRadius: 2 }} onClose={() => setSuccess('')}>
          {success}
        </Alert>
      )}

      {loading ? (
        <Box sx={{ textAlign: 'center', py: 8 }}><CircularProgress /></Box>
      ) : eventos.length === 0 ? (
        <Alert severity="info" sx={{ borderRadius: 2 }}>
          Nenhum evento cadastrado. Crie o primeiro evento para começar.
        </Alert>
      ) : (
        <Stack spacing={2}>
          {eventos.map(ev => (
            <Paper key={ev.id} elevation={0} sx={{ borderRadius: 3, border: '1px solid #E0E0E6', overflow: 'hidden' }}>
              <Box sx={{
                bgcolor: '#1A1A2E', px: 3, py: 1.5,
                display: 'flex', alignItems: 'center', justifyContent: 'space-between',
              }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <CelebrationOutlinedIcon sx={{ color: '#CE93D8', fontSize: 18 }} />
                  <Typography sx={{ fontWeight: 800, color: 'white', fontSize: 15 }}>
                    {ev.nome}
                  </Typography>
                </Box>
                <Tooltip title="Editar evento">
                  <IconButton size="small" onClick={() => abrirEditar(ev)}
                    sx={{ color: 'rgba(255,255,255,0.7)', '&:hover': { color: 'white' } }}>
                    <EditOutlinedIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
              </Box>

              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={3}
                sx={{ px: 3, py: 2, alignItems: { sm: 'center' } }}>
                {ev.descricao ? (
                  <Typography variant="body2" color="text.secondary" sx={{ flex: 1 }}>
                    {ev.descricao}
                  </Typography>
                ) : (
                  <Typography variant="body2" color="text.disabled" sx={{ flex: 1, fontStyle: 'italic' }}>
                    Sem descrição
                  </Typography>
                )}
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75, flexShrink: 0 }}>
                  <EventNoteOutlinedIcon sx={{ fontSize: 16, color: '#7B1FA2' }} />
                  <Typography variant="body2" color="text.secondary">
                    {ev.totalEdicoes} edição(ões)
                  </Typography>
                </Box>
              </Stack>
            </Paper>
          ))}
        </Stack>
      )}

      <Dialog open={dialogAberto} onClose={fechar} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ fontWeight: 800 }}>
          {editando ? 'Editar evento' : 'Novo evento'}
        </DialogTitle>
        <DialogContent>
          <Stack spacing={2.5} sx={{ pt: 1 }}>
            <TextField
              autoFocus fullWidth label="Nome do evento" value={form.nome}
              onChange={e => setForm(f => ({ ...f, nome: e.target.value }))}
              slotProps={{ htmlInput: { maxLength: 100 } }} disabled={saving}
              helperText={`${form.nome.length}/100`}
            />
            <TextField
              fullWidth multiline rows={3} label="Descrição (opcional)"
              value={form.descricao ?? ''}
              onChange={e => setForm(f => ({ ...f, descricao: e.target.value }))}
              disabled={saving}
            />
            {error && <Alert severity="error" sx={{ borderRadius: 2 }}>{error}</Alert>}
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={fechar} disabled={saving}>Cancelar</Button>
          <Button variant="contained" onClick={handleSalvar}
            disabled={form.nome.trim().length < 3 || saving}
            sx={{ bgcolor: '#7B1FA2', '&:hover': { bgcolor: '#6A1B9A' }, fontWeight: 700 }}>
            {saving
              ? <CircularProgress size={18} sx={{ color: 'white' }} />
              : editando ? 'Salvar alterações' : 'Criar evento'}
          </Button>
        </DialogActions>
      </Dialog>

    </Box>
  );
};

export { EventosPage };
