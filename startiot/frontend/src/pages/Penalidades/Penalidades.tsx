import React, { useEffect, useRef, useState } from 'react';
import {
  Alert, Box, Button, Chip, CircularProgress, Divider,
  MenuItem, Paper, Stack, TextField, Typography,
} from '@mui/material';
import WarningAmberOutlinedIcon from '@mui/icons-material/WarningAmberOutlined';
import ReportOutlinedIcon from '@mui/icons-material/ReportOutlined';
import CheckCircleOutlinedIcon from '@mui/icons-material/CheckCircleOutlined';
import BoltOutlinedIcon from '@mui/icons-material/BoltOutlined';
import HourglassEmptyOutlinedIcon from '@mui/icons-material/HourglassEmptyOutlined';

import { listarEventos, type EventoResponse } from '../../services/eventos';
import { listarEdicoesPorEvento, type EdicaoResponse } from '../../services/edicoes';
import {
  listarPendentes, validarTempo,
  type RegistroTempoResponse,
} from '../../services/registros';

const POLL_INTERVAL = 5000;

const formatMs = (ms: number) => {
  const min  = Math.floor(ms / 60000);
  const sec  = Math.floor((ms % 60000) / 1000);
  const cent = Math.floor((ms % 1000) / 10);
  return `${String(min).padStart(2, '0')}:${String(sec).padStart(2, '0')}.${String(cent).padStart(2, '0')}`;
};

// ─── LINHA DE REGISTRO ────────────────────────────────────────────────────────

interface RegistroRowProps {
  registro: RegistroTempoResponse;
  onValidado: (id: number) => void;
}

const RegistroRow: React.FC<RegistroRowProps> = ({ registro, onValidado }) => {
  const [salvando, setSalvando] = useState<null | 'ok' | 'simples' | 'grave'>(null);
  const [erro, setErro]         = useState('');

  const handleValidar = async (tipo: null | 'SIMPLES' | 'GRAVE') => {
    const key = tipo === null ? 'ok' : tipo === 'SIMPLES' ? 'simples' : 'grave';
    setSalvando(key);
    setErro('');
    try {
      await validarTempo(registro.id, tipo ? { tipoPenalidade: tipo } : undefined);
      onValidado(registro.id);
    } catch {
      setErro('Erro ao validar. Tente novamente.');
      setSalvando(null);
    }
  };

  const busy = salvando !== null;

  return (
    <Paper elevation={0} sx={{
      p: 2, borderRadius: 2, border: '1px solid #E0E0E6',
      borderLeft: '5px solid #00838F',
    }}>
      <Stack direction={{ xs: 'column', sm: 'row' }} alignItems={{ sm: 'center' }}
        justifyContent="space-between" gap={1.5}>

        {/* Equipe + tempo */}
        <Box>
          <Typography sx={{ fontWeight: 800, color: '#1A1A2E' }}>{registro.equipeNome}</Typography>
          <Typography variant="body2" sx={{ fontFamily: 'monospace', color: '#555', mt: 0.5 }}>
            Tempo registrado: <strong>{formatMs(registro.tempoMilissegundos)}</strong>
          </Typography>
          <Typography variant="caption" sx={{ color: '#9A9AAF' }}>
            Cronometrista: {registro.usuarioNome}
          </Typography>
        </Box>

        {/* Ações */}
        <Stack direction={{ xs: 'column', sm: 'row' }} gap={1} flexShrink={0}>
          <Button
            size="small" variant="contained"
            startIcon={salvando === 'ok' ? <CircularProgress size={14} sx={{ color: 'white' }} /> : <CheckCircleOutlinedIcon />}
            disabled={busy}
            onClick={() => handleValidar(null)}
            sx={{ bgcolor: '#22B573', '&:hover': { bgcolor: '#1a8f5a' }, fontWeight: 700, fontSize: '0.75rem' }}
          >
            Validar
          </Button>

          <Button
            size="small" variant="contained"
            startIcon={salvando === 'simples' ? <CircularProgress size={14} sx={{ color: 'white' }} /> : <WarningAmberOutlinedIcon />}
            disabled={busy}
            onClick={() => handleValidar('SIMPLES')}
            sx={{ bgcolor: '#F5A623', '&:hover': { bgcolor: '#d4891a' }, fontWeight: 700, fontSize: '0.75rem' }}
          >
            Simples +20s
          </Button>

          <Button
            size="small" variant="contained"
            startIcon={salvando === 'grave' ? <CircularProgress size={14} sx={{ color: 'white' }} /> : <ReportOutlinedIcon />}
            disabled={busy}
            onClick={() => handleValidar('GRAVE')}
            sx={{ bgcolor: '#C8102E', '&:hover': { bgcolor: '#9B0D23' }, fontWeight: 700, fontSize: '0.75rem' }}
          >
            Grave +2min
          </Button>
        </Stack>
      </Stack>

      {erro && <Alert severity="error" sx={{ mt: 1.5, borderRadius: 2 }}>{erro}</Alert>}
    </Paper>
  );
};

// ─── PÁGINA PRINCIPAL ────────────────────────────────────────────────────────

const PenalidadesPage: React.FC = () => {
  const [eventos, setEventos]     = useState<EventoResponse[]>([]);
  const [edicoes, setEdicoes]     = useState<EdicaoResponse[]>([]);
  const [eventoId, setEventoId]   = useState<number | ''>('');
  const [edicaoId, setEdicaoId]   = useState<number | ''>('');
  const [pendentes, setPendentes] = useState<RegistroTempoResponse[]>([]);
  const [loading, setLoading]     = useState(false);
  const [erro, setErro]           = useState('');
  const pollRef = useRef<ReturnType<typeof setInterval> | null>(null);

  // Carga inicial — auto-detecta edição EM_ANDAMENTO
  useEffect(() => {
    listarEventos().then(async evs => {
      setEventos(evs);
      for (const ev of evs) {
        const eds = await listarEdicoesPorEvento(ev.id).catch(() => []);
        const ativa = eds.find(e => e.status === 'EM_ANDAMENTO');
        if (ativa) {
          setEventoId(ev.id);
          setEdicoes(eds);
          setEdicaoId(ativa.id);
          return;
        }
      }
      if (evs.length === 1) {
        setEventoId(evs[0].id);
        listarEdicoesPorEvento(evs[0].id).then(setEdicoes).catch(() => {});
      }
    }).catch(() => {});
  }, []);

  useEffect(() => {
    if (!eventoId) { setEdicoes([]); setEdicaoId(''); return; }
    listarEdicoesPorEvento(eventoId as number).then(setEdicoes).catch(() => {});
  }, [eventoId]);

  // Polling de pendentes
  useEffect(() => {
    if (pollRef.current) clearInterval(pollRef.current);
    if (!edicaoId) { setPendentes([]); return; }

    const buscar = (showLoader: boolean) => {
      if (showLoader) setLoading(true);
      setErro('');
      listarPendentes(edicaoId as number)
        .then(setPendentes)
        .catch(() => setErro('Erro ao buscar tempos pendentes.'))
        .finally(() => setLoading(false));
    };

    buscar(true);
    pollRef.current = setInterval(() => buscar(false), POLL_INTERVAL);
    return () => { if (pollRef.current) clearInterval(pollRef.current); };
  }, [edicaoId]);

  const handleValidado = (id: number) => {
    setPendentes(prev => prev.filter(r => r.id !== id));
  };

  // Agrupar por corridaId
  const porCorrida = pendentes.reduce<Record<number, RegistroTempoResponse[]>>((acc, r) => {
    if (!acc[r.corridaId]) acc[r.corridaId] = [];
    acc[r.corridaId].push(r);
    return acc;
  }, {});

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#F4F4F6', p: { xs: 2, md: 4 } }}>

      {/* Cabeçalho */}
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 4 }}>
        <CheckCircleOutlinedIcon sx={{ color: '#00838F', fontSize: 28 }} />
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 900, color: '#1A1A2E', lineHeight: 1 }}>
            Validação de Tempos
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Valide os tempos registrados pelo cronometrista. Tempos validados entram no ranking.
          </Typography>
        </Box>
      </Box>

      {/* Seletores */}
      <Paper elevation={0} sx={{ p: 3, borderRadius: 3, border: '1px solid #E0E0E6', mb: 3 }}>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
          <TextField select fullWidth label="Evento" value={eventoId} size="small"
            onChange={e => { setEventoId(Number(e.target.value)); setEdicaoId(''); }}>
            {eventos.map(ev => <MenuItem key={ev.id} value={ev.id}>{ev.nome}</MenuItem>)}
          </TextField>
          <TextField select fullWidth label="Edição" value={edicaoId} size="small"
            disabled={edicoes.length === 0}
            onChange={e => setEdicaoId(Number(e.target.value))}>
            {edicoes.map(ed => (
              <MenuItem key={ed.id} value={ed.id}>{ed.ano} — {ed.status}</MenuItem>
            ))}
          </TextField>
        </Stack>
      </Paper>

      {erro && <Alert severity="error" sx={{ mb: 2, borderRadius: 2 }}>{erro}</Alert>}

      {loading && (
        <Box sx={{ textAlign: 'center', py: 6 }}><CircularProgress /></Box>
      )}

      {/* Estado vazio — aguardando */}
      {!loading && edicaoId && pendentes.length === 0 && (
        <Paper elevation={0} sx={{
          p: 6, borderRadius: 3, border: '1px solid #E0E0E6',
          display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2,
        }}>
          <HourglassEmptyOutlinedIcon sx={{ fontSize: 48, color: '#C0C0CC' }} />
          <Typography variant="h6" sx={{ fontWeight: 700, color: '#9A9AAF' }}>
            Nenhum tempo aguardando validação
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Aguardando o cronometrista registrar tempos de chegada...
          </Typography>
          <Chip label={`Atualizando a cada ${POLL_INTERVAL / 1000}s`} size="small"
            variant="outlined" sx={{ color: '#9A9AAF', borderColor: '#C0C0CC' }} />
        </Paper>
      )}

      {/* Lista de pendentes agrupados por corrida */}
      {!loading && pendentes.length > 0 && (
        <Stack spacing={3}>
          {Object.entries(porCorrida).map(([corridaId, regs]) => (
            <Box key={corridaId}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1.5 }}>
                <BoltOutlinedIcon sx={{ fontSize: 16, color: '#00838F' }} />
                <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#1A1A2E' }}>
                  Corrida #{corridaId}
                </Typography>
                <Chip label={`${regs.length} pendente(s)`} size="small" variant="outlined"
                  sx={{ color: '#F5A623', borderColor: '#F5A623' }} />
              </Box>

              <Stack spacing={1.5}>
                {regs.map(r => (
                  <RegistroRow key={r.id} registro={r} onValidado={handleValidado} />
                ))}
              </Stack>

              <Divider sx={{ mt: 3 }} />
            </Box>
          ))}
        </Stack>
      )}
    </Box>
  );
};

export { PenalidadesPage };
