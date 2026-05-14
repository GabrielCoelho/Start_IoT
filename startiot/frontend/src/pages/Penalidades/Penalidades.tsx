import React, { useEffect, useState } from 'react';
import {
  Alert, Box, Button, Chip, CircularProgress, Divider, MenuItem,
  Paper, Stack, TextField, Tooltip, Typography,
} from '@mui/material';
import WarningAmberOutlinedIcon from '@mui/icons-material/WarningAmberOutlined';
import ReportOutlinedIcon from '@mui/icons-material/ReportOutlined';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import BoltOutlinedIcon from '@mui/icons-material/BoltOutlined';

import { listarEventos, type EventoResponse } from '../../services/eventos';
import { listarEdicoesPorEvento, type EdicaoResponse } from '../../services/edicoes';
import {
  listarRegistrosEdicao, aplicarPenalidade, removerPenalidade,
  type RegistroTempoResponse, type TipoPenalidade,
  PENALIDADE_LABEL,
} from '../../services/registros';

// ─── HELPERS ─────────────────────────────────────────────────────────────────

const formatMs = (ms: number) => {
  const min  = Math.floor(ms / 60000);
  const sec  = Math.floor((ms % 60000) / 1000);
  const cent = Math.floor((ms % 1000) / 10);
  return `${String(min).padStart(2, '0')}:${String(sec).padStart(2, '0')}.${String(cent).padStart(2, '0')}`;
};

// ─── LINHA DE REGISTRO ────────────────────────────────────────────────────────

interface RegistroRowProps {
  registro: RegistroTempoResponse;
  onAtualizado: (r: RegistroTempoResponse) => void;
}

const RegistroRow: React.FC<RegistroRowProps> = ({ registro, onAtualizado }) => {
  const [abrirForm, setAbrirForm] = useState(false);
  const [tipo, setTipo]           = useState<TipoPenalidade>('SIMPLES');
  const [motivo, setMotivo]       = useState('');
  const [salvando, setSalvando]   = useState(false);
  const [removendo, setRemoving]  = useState(false);
  const [erro, setErro]           = useState('');

  const temPenalidade = !!registro.tipoPenalidade;
  const delta = registro.tempoEfetivo - registro.tempoMilissegundos;

  const handleAplicar = async () => {
    setSalvando(true);
    setErro('');
    try {
      const atualizado = await aplicarPenalidade(registro.id, { tipo, motivo: motivo || undefined });
      onAtualizado(atualizado);
      setAbrirForm(false);
      setMotivo('');
    } catch {
      setErro('Erro ao aplicar penalidade.');
    } finally {
      setSalvando(false);
    }
  };

  const handleRemover = async () => {
    setRemoving(true);
    setErro('');
    try {
      const atualizado = await removerPenalidade(registro.id);
      onAtualizado(atualizado);
    } catch {
      setErro('Erro ao remover penalidade.');
    } finally {
      setRemoving(false);
    }
  };

  return (
    <Paper elevation={0} sx={{
      p: 2, borderRadius: 2, border: '1px solid',
      borderColor: temPenalidade
        ? (registro.tipoPenalidade === 'GRAVE' ? '#C8102E44' : '#F5A62344')
        : '#E0E0E6',
      borderLeft: '5px solid',
      borderLeftColor: temPenalidade
        ? (registro.tipoPenalidade === 'GRAVE' ? '#C8102E' : '#F5A623')
        : '#E0E0E6',
    }}>
      <Stack direction={{ xs: 'column', sm: 'row' }} alignItems={{ sm: 'center' }} justifyContent="space-between" gap={1.5}>

        {/* Equipe + tempo */}
        <Box>
          <Typography sx={{ fontWeight: 800, color: '#1A1A2E' }}>{registro.equipeNome}</Typography>
          <Stack direction="row" gap={1.5} alignItems="center" mt={0.5} flexWrap="wrap">
            <Typography variant="body2" sx={{ fontFamily: 'monospace', color: '#555' }}>
              Original: {formatMs(registro.tempoMilissegundos)}
            </Typography>

            {temPenalidade && (
              <>
                <Chip
                  size="small"
                  icon={registro.tipoPenalidade === 'GRAVE'
                    ? <ReportOutlinedIcon fontSize="small" />
                    : <WarningAmberOutlinedIcon fontSize="small" />}
                  label={PENALIDADE_LABEL[registro.tipoPenalidade!]}
                  sx={{
                    bgcolor: registro.tipoPenalidade === 'GRAVE' ? '#C8102E' : '#F5A623',
                    color: 'white',
                    fontWeight: 700,
                    fontSize: '0.7rem',
                  }}
                />
                <Typography variant="body2" sx={{ fontFamily: 'monospace', fontWeight: 900, color: '#C8102E' }}>
                  Efetivo: {formatMs(registro.tempoEfetivo)}
                  <Typography component="span" variant="caption" sx={{ ml: 0.5, opacity: 0.6 }}>
                    (+{formatMs(delta)})
                  </Typography>
                </Typography>
              </>
            )}

            {!temPenalidade && (
              <Chip size="small" icon={<CheckCircleOutlineIcon fontSize="small" />}
                label="Sem penalidade" variant="outlined"
                sx={{ fontSize: '0.7rem', color: '#22B573', borderColor: '#22B573' }} />
            )}
          </Stack>

          {registro.motivoPenalidade && (
            <Typography variant="caption" sx={{ color: '#9A9AAF', mt: 0.5, display: 'block' }}>
              Motivo: {registro.motivoPenalidade}
            </Typography>
          )}
        </Box>

        {/* Ações */}
        <Stack direction="row" gap={1} flexShrink={0}>
          {temPenalidade && (
            <Tooltip title="Remover penalidade">
              <span>
                <Button size="small" variant="outlined" color="error"
                  startIcon={removendo ? <CircularProgress size={12} /> : <DeleteOutlineIcon />}
                  disabled={removendo || salvando}
                  onClick={handleRemover}
                  sx={{ fontWeight: 700, fontSize: '0.75rem' }}>
                  Remover
                </Button>
              </span>
            </Tooltip>
          )}
          <Button size="small" variant={temPenalidade ? 'outlined' : 'contained'}
            startIcon={<WarningAmberOutlinedIcon />}
            onClick={() => { setAbrirForm(f => !f); setErro(''); }}
            sx={{
              fontWeight: 700, fontSize: '0.75rem',
              bgcolor: temPenalidade ? undefined : '#F5A623',
              borderColor: temPenalidade ? '#F5A623' : undefined,
              color: temPenalidade ? '#F5A623' : 'white',
              '&:hover': { bgcolor: temPenalidade ? '#FFF8E1' : '#d4891a' },
            }}>
            {temPenalidade ? 'Alterar' : 'Penalizar'}
          </Button>
        </Stack>
      </Stack>

      {/* Formulário inline */}
      {abrirForm && (
        <Box sx={{ mt: 2, pt: 2, borderTop: '1px solid #E0E0E6' }}>
          {erro && <Alert severity="error" sx={{ mb: 1.5, borderRadius: 2 }}>{erro}</Alert>}
          <Stack direction={{ xs: 'column', sm: 'row' }} gap={1.5} alignItems="flex-start">
            <TextField select size="small" label="Tipo de penalidade" value={tipo}
              onChange={e => setTipo(e.target.value as TipoPenalidade)}
              sx={{ minWidth: 200 }}>
              <MenuItem value="SIMPLES">
                <Stack direction="row" alignItems="center" gap={1}>
                  <WarningAmberOutlinedIcon fontSize="small" sx={{ color: '#F5A623' }} />
                  <Box>
                    <Typography variant="body2" fontWeight={700}>Simples</Typography>
                    <Typography variant="caption" color="text.secondary">+20 segundos</Typography>
                  </Box>
                </Stack>
              </MenuItem>
              <MenuItem value="GRAVE">
                <Stack direction="row" alignItems="center" gap={1}>
                  <ReportOutlinedIcon fontSize="small" sx={{ color: '#C8102E' }} />
                  <Box>
                    <Typography variant="body2" fontWeight={700}>Grave</Typography>
                    <Typography variant="caption" color="text.secondary">+2 minutos</Typography>
                  </Box>
                </Stack>
              </MenuItem>
            </TextField>

            <TextField size="small" label="Motivo (opcional)" value={motivo}
              onChange={e => setMotivo(e.target.value)}
              placeholder="Ex: pisou na linha de saída"
              sx={{ flex: 1, minWidth: 200 }}
              inputProps={{ maxLength: 200 }}
            />

            <Stack direction="row" gap={1}>
              <Button size="small" variant="contained"
                disabled={salvando}
                onClick={handleAplicar}
                sx={{ bgcolor: '#C8102E', '&:hover': { bgcolor: '#9B0D23' }, fontWeight: 700 }}>
                {salvando ? <CircularProgress size={16} sx={{ color: 'white' }} /> : 'Confirmar'}
              </Button>
              <Button size="small" variant="outlined" onClick={() => setAbrirForm(false)}>
                Cancelar
              </Button>
            </Stack>
          </Stack>
        </Box>
      )}
    </Paper>
  );
};

// ─── PÁGINA PRINCIPAL ────────────────────────────────────────────────────────

const PenalidadesPage: React.FC = () => {
  const [eventos, setEventos]     = useState<EventoResponse[]>([]);
  const [edicoes, setEdicoes]     = useState<EdicaoResponse[]>([]);
  const [eventoId, setEventoId]   = useState<number | ''>('');
  const [edicaoId, setEdicaoId]   = useState<number | ''>('');
  const [registros, setRegistros] = useState<RegistroTempoResponse[]>([]);
  const [loading, setLoading]     = useState(false);
  const [erro, setErro]           = useState('');

  useEffect(() => {
    listarEventos().then(async evs => {
      setEventos(evs);
      // Auto-seleciona edição EM_ANDAMENTO
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

  useEffect(() => {
    if (!edicaoId) { setRegistros([]); return; }
    setLoading(true);
    setErro('');
    listarRegistrosEdicao(edicaoId as number)
      .then(setRegistros)
      .catch(() => setErro('Erro ao carregar registros.'))
      .finally(() => setLoading(false));
  }, [edicaoId]);

  const handleAtualizado = (atualizado: RegistroTempoResponse) => {
    setRegistros(prev => prev.map(r => r.id === atualizado.id ? atualizado : r));
  };

  // Agrupar por corridaId para exibição
  const porCorrida = registros.reduce<Record<number, RegistroTempoResponse[]>>((acc, r) => {
    if (!acc[r.corridaId]) acc[r.corridaId] = [];
    acc[r.corridaId].push(r);
    return acc;
  }, {});

  const comPenalidade = registros.filter(r => r.tipoPenalidade).length;

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#F4F4F6', p: { xs: 2, md: 4 } }}>

      {/* Cabeçalho */}
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 4 }}>
        <WarningAmberOutlinedIcon sx={{ color: '#F5A623', fontSize: 28 }} />
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 900, color: '#1A1A2E', lineHeight: 1 }}>
            Penalidades
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Aplicar penalidades simples (+20s) ou graves (+2min) aos tempos registrados
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

      {!loading && edicaoId && registros.length === 0 && (
        <Alert severity="info" sx={{ borderRadius: 2 }}>
          Nenhum tempo de chegada registrado nesta edição ainda.
        </Alert>
      )}

      {!loading && registros.length > 0 && (
        <>
          {/* Resumo */}
          <Box sx={{ display: 'flex', gap: 2, mb: 3, flexWrap: 'wrap' }}>
            <Chip label={`${registros.length} tempo(s) registrado(s)`} variant="outlined" />
            {comPenalidade > 0 && (
              <Chip
                icon={<WarningAmberOutlinedIcon fontSize="small" />}
                label={`${comPenalidade} penalidade(s) ativa(s)`}
                sx={{ bgcolor: '#FFF3E0', color: '#E65100', borderColor: '#F5A623', fontWeight: 700 }}
                variant="outlined"
              />
            )}
          </Box>

          {/* Registros agrupados por corrida */}
          <Stack spacing={3}>
            {Object.entries(porCorrida).map(([corridaId, regs]) => (
              <Box key={corridaId}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1.5 }}>
                  <BoltOutlinedIcon sx={{ fontSize: 16, color: '#00838F' }} />
                  <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#1A1A2E' }}>
                    Corrida #{corridaId}
                  </Typography>
                  <Chip label={`${regs.length} equipe(s)`} size="small" variant="outlined" />
                </Box>

                <Stack spacing={1.5}>
                  {regs.map(r => (
                    <RegistroRow key={r.id} registro={r} onAtualizado={handleAtualizado} />
                  ))}
                </Stack>

                <Divider sx={{ mt: 3 }} />
              </Box>
            ))}
          </Stack>
        </>
      )}
    </Box>
  );
};

export { PenalidadesPage };
