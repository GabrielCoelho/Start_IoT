import React, { useCallback, useEffect, useState } from 'react';
import {
  Accordion, AccordionDetails, AccordionSummary, Alert, Box, Button, Chip,
  CircularProgress, Divider, IconButton, MenuItem, Paper, Stack,
  TextField, Tooltip, Typography,
} from '@mui/material';
import BoltOutlinedIcon from '@mui/icons-material/BoltOutlined';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import AddOutlinedIcon from '@mui/icons-material/AddOutlined';
import PlayArrowOutlinedIcon from '@mui/icons-material/PlayArrowOutlined';
import StopOutlinedIcon from '@mui/icons-material/StopOutlined';
import CancelOutlinedIcon from '@mui/icons-material/CancelOutlined';
import FlagOutlinedIcon from '@mui/icons-material/FlagOutlined';
import ContentCopyOutlinedIcon from '@mui/icons-material/ContentCopyOutlined';

import { listarEventos, type EventoResponse } from '../../services/eventos';
import { listarEdicoesPorEvento, type EdicaoResponse } from '../../services/edicoes';
import { listarEquipesPorEdicao, type EquipeResponse } from '../../services/equipes';
import {
  criarBateria, listarBaterias, iniciarBateria, finalizarBateria, cancelarBateria,
  type BateriaResponse,
} from '../../services/baterias';
import {
  criarCorrida, listarCorridas, iniciarCorrida, finalizarCorrida, cancelarCorrida,
  listarAlocacoes, alocarEquipe, removerAlocacao,
  type CorridaResponse, type AlocacaoResponse,
} from '../../services/corridas';

// ─── HELPERS ────────────────────────────────────────────────────────────────

type StatusBateria = BateriaResponse['status'];
type StatusCorrida = CorridaResponse['status'];

const BATERIA_CHIP: Record<StatusBateria, { label: string; color: 'default' | 'warning' | 'success' | 'error' }> = {
  AGUARDANDO:   { label: 'Aguardando',   color: 'default'  },
  EM_ANDAMENTO: { label: 'Em andamento', color: 'warning'  },
  FINALIZADA:   { label: 'Finalizada',   color: 'success'  },
  CANCELADA:    { label: 'Cancelada',    color: 'error'    },
};

const CORRIDA_CHIP: Record<StatusCorrida, { label: string; color: 'default' | 'warning' | 'success' | 'error' }> = {
  AGUARDANDO:     { label: 'Aguardando',      color: 'default' },
  EM_ANDAMENTO:   { label: 'Em andamento',    color: 'warning' },
  FINALIZADA:     { label: 'Finalizada',      color: 'success' },
  CANCELADA:      { label: 'Cancelada',       color: 'error'   },
  DESCLASSIFICADA:{ label: 'Desclassificada', color: 'error'   },
};

// ─── SELETOR DE CONTEXTO ────────────────────────────────────────────────────

interface ContextSelectorProps {
  edicaoId: number | null;
  onEdicaoChange: (id: number | null, dataEvento?: string) => void;
}

const ContextSelector: React.FC<ContextSelectorProps> = ({ edicaoId, onEdicaoChange }) => {
  const [eventos, setEventos] = useState<EventoResponse[]>([]);
  const [edicoes, setEdicoes] = useState<EdicaoResponse[]>([]);
  const [eventoId, setEventoId] = useState<number | ''>('');

  // Carga inicial com auto-detecção de edição EM_ANDAMENTO
  useEffect(() => {
    listarEventos().then(async evs => {
      setEventos(evs);

      for (const ev of evs) {
        try {
          const eds = await listarEdicoesPorEvento(ev.id);
          const ativa = eds.find(e => e.status === 'EM_ANDAMENTO');
          if (ativa) {
            setEventoId(ev.id);
            setEdicoes(eds);
            onEdicaoChange(ativa.id, ativa.dataEvento ?? undefined);
            return;
          }
        } catch {}
      }

      // Nenhuma edição ativa — só auto-seleciona o evento se houver apenas um
      if (evs.length === 1) {
        setEventoId(evs[0].id);
        listarEdicoesPorEvento(evs[0].id).then(setEdicoes).catch(() => {});
      }
    }).catch(() => {});
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleEventoChange = (id: number) => {
    setEventoId(id);
    onEdicaoChange(null);
    listarEdicoesPorEvento(id).then(setEdicoes).catch(() => {});
  };

  const handleEdicaoChange = (id: number) => {
    const ed = edicoes.find(e => e.id === id);
    onEdicaoChange(id, ed?.dataEvento ?? undefined);
  };

  return (
    <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mb: 3 }}>
      <TextField select fullWidth label="Evento" value={eventoId} size="small"
        onChange={e => handleEventoChange(Number(e.target.value))}>
        {eventos.map(ev => <MenuItem key={ev.id} value={ev.id}>{ev.nome}</MenuItem>)}
      </TextField>
      <TextField select fullWidth label="Edição" value={edicaoId ?? ''} size="small"
        disabled={edicoes.length === 0}
        onChange={e => handleEdicaoChange(Number(e.target.value))}>
        {edicoes.map(ed => (
          <MenuItem key={ed.id} value={ed.id}>{ed.ano} — {ed.status}</MenuItem>
        ))}
      </TextField>
    </Stack>
  );
};

// ─── GERENCIADOR DE ALOCAÇÕES ────────────────────────────────────────────────

interface AlocacaoManagerProps {
  corrida: CorridaResponse;
  equipes: EquipeResponse[];
}

const AlocacaoManager: React.FC<AlocacaoManagerProps> = ({ corrida, equipes }) => {
  const [alocacoes, setAlocacoes] = useState<AlocacaoResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [equipeId, setEquipeId] = useState<number | ''>('');
  const [saving, setSaving] = useState(false);
  const [removing, setRemoving] = useState<number | null>(null);

  const reload = useCallback(() => {
    listarAlocacoes(corrida.id).then(setAlocacoes).catch(() => {}).finally(() => setLoading(false));
  }, [corrida.id]);

  useEffect(() => { reload(); }, [reload]);

  const alocadasIds = new Set(alocacoes.map(a => a.equipeId));
  const disponiveis = equipes.filter(e => e.statusInscricao === 'APROVADA' && !alocadasIds.has(e.id));

  const handleAlocar = async () => {
    if (!equipeId) return;
    setSaving(true);
    try {
      await alocarEquipe(corrida.id, equipeId as number);
      setEquipeId('');
      reload();
    } catch {} finally { setSaving(false); }
  };

  const handleRemover = async (eId: number) => {
    setRemoving(eId);
    try { await removerAlocacao(corrida.id, eId); reload(); }
    catch {} finally { setRemoving(null); }
  };

  const bloqueado = corrida.status !== 'AGUARDANDO';

  if (loading) return <CircularProgress size={16} />;

  return (
    <Box sx={{ mt: 1 }}>
      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5, mb: bloqueado ? 0 : 1 }}>
        {alocacoes.length === 0
          ? <Typography variant="caption" color="text.secondary">Nenhuma equipe alocada</Typography>
          : alocacoes.map(a => (
              <Chip key={a.equipeId} label={a.equipeNome} size="small"
                onDelete={bloqueado ? undefined : () => handleRemover(a.equipeId)}
                deleteIcon={removing === a.equipeId ? <CircularProgress size={12} /> : undefined}
                color="primary" variant="outlined"
              />
            ))
        }
      </Box>

      {!bloqueado && (
        <Stack direction="row" spacing={1} sx={{ mt: 1 }}>
          <TextField select size="small" label="Adicionar equipe" value={equipeId}
            onChange={e => setEquipeId(Number(e.target.value))}
            sx={{ minWidth: 200 }} disabled={disponiveis.length === 0}
          >
            {disponiveis.length === 0
              ? <MenuItem value="">Nenhuma equipe aprovada disponível</MenuItem>
              : disponiveis.map(e => <MenuItem key={e.id} value={e.id}>{e.nome}</MenuItem>)
            }
          </TextField>
          <Button variant="contained" size="small" onClick={handleAlocar}
            disabled={!equipeId || saving}
            sx={{ bgcolor: '#00838F', '&:hover': { bgcolor: '#006064' } }}
          >
            {saving ? <CircularProgress size={16} sx={{ color: 'white' }} /> : 'Alocar'}
          </Button>
        </Stack>
      )}
    </Box>
  );
};

// ─── LINHA DE CORRIDA ────────────────────────────────────────────────────────

interface CorridaRowProps {
  corrida: CorridaResponse;
  equipes: EquipeResponse[];
  bateriaId: number;
  nextOrdem: number;
  onChange: (updated: CorridaResponse) => void;
  onReplicada: (nova: CorridaResponse) => void;
}

const CorridaRow: React.FC<CorridaRowProps> = ({ corrida, equipes, bateriaId, nextOrdem, onChange, onReplicada }) => {
  const [acting, setActing] = useState(false);
  const [replicando, setReplicando] = useState(false);

  const handleReplicar = async () => {
    setReplicando(true);
    try {
      const alocacoes = await listarAlocacoes(corrida.id);
      const nova = await criarCorrida(bateriaId, nextOrdem);
      await Promise.all(alocacoes.map(a => alocarEquipe(nova.id, a.equipeId)));
      onReplicada(nova);
    } catch {} finally { setReplicando(false); }
  };

  const act = async (fn: () => Promise<CorridaResponse>) => {
    setActing(true);
    try { onChange(await fn()); } catch {} finally { setActing(false); }
  };

  const chip = CORRIDA_CHIP[corrida.status];

  return (
    <Paper elevation={0} sx={{
      p: 2, borderRadius: 2, border: '1px solid #E0E0E6',
      borderLeft: '4px solid', borderLeftColor:
        corrida.status === 'EM_ANDAMENTO' ? '#ED6C02' :
        corrida.status === 'FINALIZADA'   ? '#2E7D32' :
        corrida.status === 'AGUARDANDO'   ? '#9A9AAF' : '#C8102E',
    }}>
      <Stack direction="row" alignItems="center" justifyContent="space-between" flexWrap="wrap" gap={1}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <FlagOutlinedIcon sx={{ fontSize: 16, color: '#AD1457' }} />
          <Typography variant="body2" sx={{ fontWeight: 800 }}>
            Corrida {corrida.ordem}
          </Typography>
          <Chip label={chip.label} size="small" color={chip.color} />
          {corrida.totalRegistros > 0 && (
            <Chip label={`${corrida.totalRegistros} tempo(s)`} size="small" variant="outlined" />
          )}
        </Box>

        <Stack direction="row" spacing={0.5}>
          {corrida.status === 'AGUARDANDO' && (
            <Tooltip title="Iniciar corrida">
              <span>
                <IconButton size="small" disabled={acting} onClick={() => act(() => iniciarCorrida(corrida.id))}
                  sx={{ color: '#2E7D32' }}>
                  {acting ? <CircularProgress size={16} /> : <PlayArrowOutlinedIcon fontSize="small" />}
                </IconButton>
              </span>
            </Tooltip>
          )}
          {corrida.status === 'EM_ANDAMENTO' && (
            <Tooltip title="Finalizar corrida">
              <span>
                <IconButton size="small" disabled={acting} onClick={() => act(() => finalizarCorrida(corrida.id))}
                  sx={{ color: '#1565C0' }}>
                  <StopOutlinedIcon fontSize="small" />
                </IconButton>
              </span>
            </Tooltip>
          )}
          {(corrida.status === 'AGUARDANDO' || corrida.status === 'EM_ANDAMENTO') && (
            <Tooltip title="Cancelar corrida">
              <span>
                <IconButton size="small" disabled={acting} onClick={() => act(() => cancelarCorrida(corrida.id))}
                  sx={{ color: '#C8102E' }}>
                  <CancelOutlinedIcon fontSize="small" />
                </IconButton>
              </span>
            </Tooltip>
          )}
          <Tooltip title="Replicar corrida (mesmas equipes)">
            <span>
              <IconButton size="small" disabled={replicando || acting} onClick={handleReplicar}
                sx={{ color: '#7B1FA2' }}>
                {replicando ? <CircularProgress size={16} /> : <ContentCopyOutlinedIcon fontSize="small" />}
              </IconButton>
            </span>
          </Tooltip>
        </Stack>
      </Stack>

      <AlocacaoManager corrida={corrida} equipes={equipes} />
    </Paper>
  );
};

// ─── CARD DE BATERIA ─────────────────────────────────────────────────────────

interface BateriaCardProps {
  bateria: BateriaResponse;
  equipes: EquipeResponse[];
  onChange: (updated: BateriaResponse) => void;
}

const BateriaCard: React.FC<BateriaCardProps> = ({ bateria, equipes, onChange }) => {
  const [corridas, setCorridas] = useState<CorridaResponse[]>([]);
  const [loadingCorridas, setLoadingCorridas] = useState(true);
  const [acting, setActing] = useState(false);
  const [addingCorrida, setAddingCorrida] = useState(false);

  const reloadCorridas = useCallback(() => {
    listarCorridas(bateria.id)
      .then(c => setCorridas(c.sort((a, b) => a.ordem - b.ordem)))
      .catch(() => {})
      .finally(() => setLoadingCorridas(false));
  }, [bateria.id]);

  useEffect(() => {
    reloadCorridas();
    const id = setInterval(reloadCorridas, 5_000);
    return () => clearInterval(id);
  }, [reloadCorridas]);

  const actBateria = async (fn: () => Promise<BateriaResponse>) => {
    setActing(true);
    try { onChange(await fn()); } catch {} finally { setActing(false); }
  };

  const handleAddCorrida = async () => {
    setAddingCorrida(true);
    try {
      const ordem = corridas.length + 1;
      const nova = await criarCorrida(bateria.id, ordem);
      setCorridas(prev => [...prev, nova]);
    } catch {} finally { setAddingCorrida(false); }
  };

  const updateCorrida = (updated: CorridaResponse) =>
    setCorridas(prev => prev.map(c => c.id === updated.id ? updated : c));

  const addCorrida = (nova: CorridaResponse) =>
    setCorridas(prev => [...prev, nova].sort((a, b) => a.ordem - b.ordem));

  const chip = BATERIA_CHIP[bateria.status];
  const podeIniciar   = bateria.status === 'AGUARDANDO';
  const podeFinalizar = bateria.status === 'EM_ANDAMENTO';
  const podeCancelar  = bateria.status === 'AGUARDANDO' || bateria.status === 'EM_ANDAMENTO';
  const podeAddCorrida = bateria.status === 'AGUARDANDO' || bateria.status === 'EM_ANDAMENTO';

  return (
    <Accordion elevation={0} sx={{ border: '1px solid #E0E0E6', borderRadius: '12px !important', '&:before': { display: 'none' } }}>
      <AccordionSummary expandIcon={<ExpandMoreIcon />} sx={{ px: 3 }}>
        <Stack direction="row" alignItems="center" justifyContent="space-between"
          sx={{ width: '100%', pr: 1 }} flexWrap="wrap" gap={1}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <BoltOutlinedIcon sx={{ color: '#00838F' }} />
            <Typography sx={{ fontWeight: 900, color: '#1A1A2E' }}>
              Bateria #{bateria.numero}
              {bateria.tipo && ` — ${bateria.tipo}`}
            </Typography>
            <Chip label={chip.label} size="small" color={chip.color} />
            <Typography variant="caption" color="text.secondary">
              {bateria.totalCorridas} corrida(s)
            </Typography>
          </Box>

          <Stack direction="row" spacing={0.5} onClick={e => e.stopPropagation()}>
            {podeIniciar && (
              <Tooltip title="Iniciar bateria">
                <span>
                  <IconButton size="small" disabled={acting} onClick={() => actBateria(() => iniciarBateria(bateria.id))}
                    sx={{ color: '#2E7D32' }}>
                    {acting ? <CircularProgress size={16} /> : <PlayArrowOutlinedIcon fontSize="small" />}
                  </IconButton>
                </span>
              </Tooltip>
            )}
            {podeFinalizar && (
              <Tooltip title="Finalizar bateria">
                <span>
                  <IconButton size="small" disabled={acting} onClick={() => actBateria(() => finalizarBateria(bateria.id))}
                    sx={{ color: '#1565C0' }}>
                    <StopOutlinedIcon fontSize="small" />
                  </IconButton>
                </span>
              </Tooltip>
            )}
            {podeCancelar && (
              <Tooltip title="Cancelar bateria">
                <span>
                  <IconButton size="small" disabled={acting} onClick={() => actBateria(() => cancelarBateria(bateria.id))}
                    sx={{ color: '#C8102E' }}>
                    <CancelOutlinedIcon fontSize="small" />
                  </IconButton>
                </span>
              </Tooltip>
            )}
          </Stack>
        </Stack>
      </AccordionSummary>

      <AccordionDetails sx={{ px: 3, pb: 3 }}>
        <Divider sx={{ mb: 2 }} />

        {loadingCorridas
          ? <CircularProgress size={20} />
          : (
            <Stack spacing={1.5}>
              {corridas.length === 0
                ? <Typography variant="body2" color="text.secondary">Nenhuma corrida cadastrada.</Typography>
                : corridas.map(c => (
                    <CorridaRow
                      key={c.id}
                      corrida={c}
                      equipes={equipes}
                      bateriaId={bateria.id}
                      nextOrdem={corridas.length + 1}
                      onChange={updateCorrida}
                      onReplicada={addCorrida}
                    />
                  ))
              }

              {podeAddCorrida && (
                <Button variant="outlined" startIcon={addingCorrida ? <CircularProgress size={14} /> : <AddOutlinedIcon />}
                  onClick={handleAddCorrida} disabled={addingCorrida} size="small"
                  sx={{ alignSelf: 'flex-start', borderColor: '#AD1457', color: '#AD1457',
                        '&:hover': { borderColor: '#880E4F' } }}
                >
                  Adicionar corrida
                </Button>
              )}
            </Stack>
          )
        }
      </AccordionDetails>
    </Accordion>
  );
};

// ─── FORMULÁRIO NOVA BATERIA ─────────────────────────────────────────────────

interface NovaBateriaFormProps {
  edicaoId: number;
  proximoNumero: number;
  dataEvento?: string;
  onCriada: (b: BateriaResponse) => void;
}

const formatDateBR = (iso?: string) => {
  if (!iso) return '—';
  const [y, m, d] = iso.split('-');
  return `${d}/${m}/${y}`;
};

const NovaBateriaForm: React.FC<NovaBateriaFormProps> = ({ edicaoId, proximoNumero, dataEvento, onCriada }) => {
  const [form, setForm] = useState({ numero: String(proximoNumero), tipo: '', horario: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => { setForm(f => ({ ...f, numero: String(proximoNumero) })); }, [proximoNumero]);

  const set = (f: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm(p => ({ ...p, [f]: e.target.value }));

  const handleSubmit = async () => {
    setError(''); setLoading(true);
    try {
      const horarioPrevisto = dataEvento && form.horario
        ? `${dataEvento}T${form.horario}:00`
        : undefined;
      const nova = await criarBateria({
        edicaoId,
        numero: Number(form.numero),
        tipo: form.tipo || undefined,
        horarioPrevisto,
      });
      onCriada(nova);
      setForm(f => ({ ...f, numero: String(Number(f.numero) + 1), tipo: '', horario: '' }));
    } catch (err: any) {
      setError(err?.response?.data?.message ?? 'Erro ao criar bateria.');
    } finally { setLoading(false); }
  };

  return (
    <Paper elevation={0} sx={{ borderRadius: 3, border: '1px solid #E0E0E6', overflow: 'hidden', mb: 3 }}>
      <Box sx={{ bgcolor: '#1A1A2E', px: 3, py: 1.5 }}>
        <Typography variant="subtitle2" sx={{ color: 'white', fontWeight: 700 }}>Nova bateria</Typography>
      </Box>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ p: 3 }} alignItems="flex-start">
        <TextField label="Número" type="number" size="small" value={form.numero}
          onChange={set('numero')} sx={{ width: 100 }} />
        <TextField label="Tipo" size="small" value={form.tipo} onChange={set('tipo')}
          placeholder="Ex: Eliminatória, Final" inputProps={{ maxLength: 50 }} sx={{ flex: 1 }} />
        <TextField label="Data do evento" size="small" value={formatDateBR(dataEvento)}
          disabled InputLabelProps={{ shrink: true }} sx={{ width: 160 }} />
        <TextField label="Horário previsto" type="time" size="small"
          value={form.horario} onChange={set('horario')}
          InputLabelProps={{ shrink: true }} sx={{ width: 150 }} />
        <Box>
          {error && <Alert severity="error" sx={{ mb: 1, borderRadius: 2 }}>{error}</Alert>}
          <Button variant="contained" onClick={handleSubmit}
            disabled={!form.numero || loading}
            startIcon={loading ? <CircularProgress size={16} sx={{ color: 'white' }} /> : <AddOutlinedIcon />}
            sx={{ bgcolor: '#00838F', '&:hover': { bgcolor: '#006064' }, fontWeight: 700, whiteSpace: 'nowrap' }}
          >
            Criar bateria
          </Button>
        </Box>
      </Stack>
    </Paper>
  );
};

// ─── PÁGINA PRINCIPAL ────────────────────────────────────────────────────────

const BateriasPage: React.FC = () => {
  const [edicaoId, setEdicaoId] = useState<number | null>(null);
  const [dataEvento, setDataEvento] = useState<string | undefined>();
  const [baterias, setBaterias] = useState<BateriaResponse[]>([]);
  const [equipes, setEquipes] = useState<EquipeResponse[]>([]);
  const [loading, setLoading] = useState(false);

  const handleEdicaoChange = (id: number | null, data?: string) => {
    setEdicaoId(id);
    setDataEvento(data);
  };

  useEffect(() => {
    if (!edicaoId) { setBaterias([]); setEquipes([]); return; }

    const carregarTudo = (inicial = false) => {
      if (inicial) setLoading(true);
      Promise.all([
        listarBaterias(edicaoId),
        listarEquipesPorEdicao(edicaoId),
      ]).then(([b, e]) => {
        setBaterias(b.sort((a, b) => a.numero - b.numero));
        setEquipes(e);
      }).catch(() => {}).finally(() => { if (inicial) setLoading(false); });
    };

    carregarTudo(true);
    const id = setInterval(() => carregarTudo(false), 5_000);
    return () => clearInterval(id);
  }, [edicaoId]);

  const handleBateriaAtualizada = (updated: BateriaResponse) =>
    setBaterias(prev => prev.map(b => b.id === updated.id ? updated : b));

  const handleBateriaCriada = (nova: BateriaResponse) =>
    setBaterias(prev => [...prev, nova].sort((a, b) => a.numero - b.numero));

  const proximoNumero = baterias.length > 0 ? Math.max(...baterias.map(b => b.numero)) + 1 : 1;

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#F4F4F6', p: { xs: 2, md: 4 } }}>

      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 4 }}>
        <BoltOutlinedIcon sx={{ color: '#00838F', fontSize: 28 }} />
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 900, color: '#1A1A2E', lineHeight: 1 }}>
            Baterias
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Gestão de baterias, corridas e alocação de equipes
          </Typography>
        </Box>
      </Box>

      <ContextSelector edicaoId={edicaoId} onEdicaoChange={handleEdicaoChange} />

      {!edicaoId ? (
        <Alert severity="info" sx={{ borderRadius: 2 }}>
          Selecione um evento e uma edição para gerenciar as baterias.
        </Alert>
      ) : loading ? (
        <Box sx={{ textAlign: 'center', py: 6 }}><CircularProgress /></Box>
      ) : (
        <>
          <NovaBateriaForm edicaoId={edicaoId} proximoNumero={proximoNumero} dataEvento={dataEvento} onCriada={handleBateriaCriada} />

          {baterias.length === 0
            ? <Alert severity="info" sx={{ borderRadius: 2 }}>Nenhuma bateria cadastrada para esta edição.</Alert>
            : (
              <Stack spacing={2}>
                {baterias.map(b => (
                  <BateriaCard key={b.id} bateria={b} equipes={equipes} onChange={handleBateriaAtualizada} />
                ))}
              </Stack>
            )
          }
        </>
      )}
    </Box>
  );
};

export { BateriasPage };
