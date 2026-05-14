import React, { useEffect, useState } from 'react';
import {
  Alert, Box, Button, Chip, CircularProgress,
  MenuItem, Paper, Stack, TextField, Typography,
} from '@mui/material';
import DirectionsCarOutlinedIcon from '@mui/icons-material/DirectionsCarOutlined';
import CheckCircleOutlinedIcon from '@mui/icons-material/CheckCircleOutlined';
import CancelOutlinedIcon from '@mui/icons-material/CancelOutlined';
import WarningAmberOutlinedIcon from '@mui/icons-material/WarningAmberOutlined';
import HourglassEmptyOutlinedIcon from '@mui/icons-material/HourglassEmptyOutlined';

import { listarEventos, type EventoResponse } from '../../services/eventos';
import { listarEdicoesPorEvento, type EdicaoResponse } from '../../services/edicoes';
import {
  listarCarrinhosPorEdicao, registrarVistoria,
  type CarrinhoResponse,
} from '../../services/carrinhos';

type Acao = 'aprovado' | 'penalidade' | 'reprovado';

const statusColor = (c: CarrinhoResponse): 'default' | 'success' | 'warning' | 'error' => {
  if (!c.dataVistoria) return 'default';
  if (!c.aprovadoVistoria) return 'error';
  if (c.penalideVistoria) return 'warning';
  return 'success';
};

const statusLabel = (c: CarrinhoResponse): string => {
  if (!c.dataVistoria) return 'Pendente';
  if (!c.aprovadoVistoria) return 'Reprovado';
  if (c.penalideVistoria) return 'Aprovado +5s';
  return 'Aprovado';
};

const statusIcon = (c: CarrinhoResponse) => {
  if (!c.dataVistoria) return <HourglassEmptyOutlinedIcon sx={{ fontSize: 16 }} />;
  if (!c.aprovadoVistoria) return <CancelOutlinedIcon sx={{ fontSize: 16 }} />;
  if (c.penalideVistoria) return <WarningAmberOutlinedIcon sx={{ fontSize: 16 }} />;
  return <CheckCircleOutlinedIcon sx={{ fontSize: 16 }} />;
};

const borderColor = (c: CarrinhoResponse): string => {
  if (!c.dataVistoria) return '#C0C0CC';
  if (!c.aprovadoVistoria) return '#C8102E';
  if (c.penalideVistoria) return '#F5A623';
  return '#22B573';
};

// ─── LINHA DE CARRINHO ────────────────────────────────────────────────────────

interface CarrinhoRowProps {
  carrinho: CarrinhoResponse;
  onAtualizado: (c: CarrinhoResponse) => void;
}

const CarrinhoRow: React.FC<CarrinhoRowProps> = ({ carrinho, onAtualizado }) => {
  const [salvando, setSalvando] = useState<Acao | null>(null);
  const [erro, setErro] = useState('');

  const handleAcao = async (acao: Acao) => {
    setSalvando(acao);
    setErro('');
    try {
      const atualizado = await registrarVistoria(carrinho.equipeId, {
        aprovado: acao !== 'reprovado',
        penalidade: acao === 'penalidade',
      });
      onAtualizado(atualizado);
    } catch {
      setErro('Erro ao registrar. Tente novamente.');
    } finally {
      setSalvando(null);
    }
  };

  const busy = salvando !== null;

  return (
    <Paper elevation={0} sx={{
      p: 2, borderRadius: 2, border: '1px solid #E0E0E6',
      borderLeft: `5px solid ${borderColor(carrinho)}`,
    }}>
      {/* Equipe + status */}
      <Stack direction="row" alignItems="center" gap={1} mb={0.25}>
        <Typography sx={{ fontWeight: 800, color: '#1A1A2E' }}>{carrinho.equipeNome}</Typography>
        <Chip
          icon={statusIcon(carrinho)}
          label={statusLabel(carrinho)}
          size="small"
          color={statusColor(carrinho)}
          variant="outlined"
        />
      </Stack>
      {carrinho.identificacao && carrinho.identificacao !== carrinho.equipeNome && (
        <Typography variant="caption" sx={{ color: '#9A9AAF', display: 'block', mb: 1 }}>
          {carrinho.identificacao}
        </Typography>
      )}

      {/* Ações */}
      <Stack direction="row" gap={2} sx={{ mt: 1.5 }}>
        <Button
          variant="contained" fullWidth
          startIcon={salvando === 'aprovado' ? <CircularProgress size={14} sx={{ color: 'white' }} /> : <CheckCircleOutlinedIcon />}
          disabled={busy}
          onClick={() => handleAcao('aprovado')}
          sx={{ bgcolor: '#22B573', '&:hover': { bgcolor: '#1a8f5a' }, fontWeight: 700, fontSize: '0.75rem', py: 1 }}
        >
          Aprovado
        </Button>

        <Button
          variant="contained" fullWidth
          startIcon={salvando === 'penalidade' ? <CircularProgress size={14} sx={{ color: 'white' }} /> : <WarningAmberOutlinedIcon />}
          disabled={busy}
          onClick={() => handleAcao('penalidade')}
          sx={{ bgcolor: '#F5A623', '&:hover': { bgcolor: '#d4891a' }, fontWeight: 700, fontSize: '0.75rem', py: 1 }}
        >
          Aprovado +5s
        </Button>

        <Button
          variant="contained" fullWidth
          startIcon={salvando === 'reprovado' ? <CircularProgress size={14} sx={{ color: 'white' }} /> : <CancelOutlinedIcon />}
          disabled={busy}
          onClick={() => handleAcao('reprovado')}
          sx={{ bgcolor: '#C8102E', '&:hover': { bgcolor: '#9B0D23' }, fontWeight: 700, fontSize: '0.75rem', py: 1 }}
        >
          Reprovado
        </Button>
      </Stack>

      {erro && <Alert severity="error" sx={{ mt: 1.5, borderRadius: 2 }}>{erro}</Alert>}
    </Paper>
  );
};

// ─── PÁGINA PRINCIPAL ────────────────────────────────────────────────────────

const VistoriaPage: React.FC = () => {
  const [eventos, setEventos]     = useState<EventoResponse[]>([]);
  const [edicoes, setEdicoes]     = useState<EdicaoResponse[]>([]);
  const [eventoId, setEventoId]   = useState<number | ''>('');
  const [edicaoId, setEdicaoId]   = useState<number | ''>('');
  const [carrinhos, setCarrinhos] = useState<CarrinhoResponse[]>([]);
  const [loading, setLoading]     = useState(false);
  const [erro, setErro]           = useState('');

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

  useEffect(() => {
    if (!edicaoId) { setCarrinhos([]); return; }
    setLoading(true);
    setErro('');
    listarCarrinhosPorEdicao(edicaoId as number)
      .then(setCarrinhos)
      .catch(() => setErro('Erro ao carregar carrinhos.'))
      .finally(() => setLoading(false));
  }, [edicaoId]);

  const handleAtualizado = (atualizado: CarrinhoResponse) => {
    setCarrinhos(prev => prev.map(c => c.id === atualizado.id ? atualizado : c));
  };

  const pendentes   = carrinhos.filter(c => !c.dataVistoria);
  const vistoriados = carrinhos.filter(c => !!c.dataVistoria);

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#F4F4F6', p: { xs: 2, md: 4 } }}>

      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 4 }}>
        <DirectionsCarOutlinedIcon sx={{ color: '#00838F', fontSize: 28 }} />
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 900, color: '#1A1A2E', lineHeight: 1 }}>
            Vistoria Técnica
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Registre o resultado da inspeção de cada carrinho antes das corridas.
          </Typography>
        </Box>
      </Box>

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
              <MenuItem key={ed.id} value={ed.id}>
                {ed.numero ? `${ed.numero}ª Edição — ${ed.ano}` : `${ed.ano}`} — {ed.status}
              </MenuItem>
            ))}
          </TextField>
        </Stack>

        {edicaoId && !loading && carrinhos.length > 0 && (
          <Stack direction="row" spacing={1} sx={{ mt: 2, flexWrap: 'wrap', gap: 1 }}>
            {pendentes.length > 0 && (
              <Chip icon={<HourglassEmptyOutlinedIcon />} label={`${pendentes.length} pendente(s)`} size="small" color="default" variant="outlined" />
            )}
            {carrinhos.filter(c => c.dataVistoria && c.aprovadoVistoria && !c.penalideVistoria).length > 0 && (
              <Chip icon={<CheckCircleOutlinedIcon />} label={`${carrinhos.filter(c => c.dataVistoria && c.aprovadoVistoria && !c.penalideVistoria).length} aprovado(s)`} size="small" color="success" variant="outlined" />
            )}
            {carrinhos.filter(c => c.penalideVistoria).length > 0 && (
              <Chip icon={<WarningAmberOutlinedIcon />} label={`${carrinhos.filter(c => c.penalideVistoria).length} com +5s`} size="small" color="warning" variant="outlined" />
            )}
            {carrinhos.filter(c => c.dataVistoria && !c.aprovadoVistoria).length > 0 && (
              <Chip icon={<CancelOutlinedIcon />} label={`${carrinhos.filter(c => c.dataVistoria && !c.aprovadoVistoria).length} reprovado(s)`} size="small" color="error" variant="outlined" />
            )}
          </Stack>
        )}
      </Paper>

      {erro && <Alert severity="error" sx={{ mb: 2, borderRadius: 2 }}>{erro}</Alert>}

      {loading && <Box sx={{ textAlign: 'center', py: 6 }}><CircularProgress /></Box>}

      {!loading && edicaoId && carrinhos.length === 0 && (
        <Alert severity="info" sx={{ borderRadius: 2 }}>Nenhum carrinho cadastrado nesta edição.</Alert>
      )}

      {!loading && carrinhos.length > 0 && (
        <Stack spacing={3}>
          {pendentes.length > 0 && (
            <Box>
              <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#9A9AAF', mb: 1.5, textTransform: 'uppercase', fontSize: '0.7rem', letterSpacing: 1 }}>
                Aguardando vistoria ({pendentes.length})
              </Typography>
              <Stack spacing={1.5}>
                {pendentes.map(c => <CarrinhoRow key={c.id} carrinho={c} onAtualizado={handleAtualizado} />)}
              </Stack>
            </Box>
          )}

          {vistoriados.length > 0 && (
            <Box>
              <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#9A9AAF', mb: 1.5, textTransform: 'uppercase', fontSize: '0.7rem', letterSpacing: 1 }}>
                Vistoriados ({vistoriados.length})
              </Typography>
              <Stack spacing={1.5}>
                {vistoriados.map(c => <CarrinhoRow key={c.id} carrinho={c} onAtualizado={handleAtualizado} />)}
              </Stack>
            </Box>
          )}
        </Stack>
      )}
    </Box>
  );
};

export { VistoriaPage };
