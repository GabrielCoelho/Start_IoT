import React, { useCallback, useEffect, useRef, useState } from 'react';
import {
  Alert, Box, Button, Chip, CircularProgress, Divider, MenuItem,
  Paper, Stack, TextField, Typography,
} from '@mui/material';
import TimerOutlinedIcon from '@mui/icons-material/TimerOutlined';
import HourglassEmptyOutlinedIcon from '@mui/icons-material/HourglassEmptyOutlined';
import CancelOutlinedIcon from '@mui/icons-material/CancelOutlined';
import SendOutlinedIcon from '@mui/icons-material/SendOutlined';
import CheckCircleOutlinedIcon from '@mui/icons-material/CheckCircleOutlined';
import FlagOutlinedIcon from '@mui/icons-material/FlagOutlined';
import StopCircleOutlinedIcon from '@mui/icons-material/StopCircleOutlined';
import SwapHorizOutlinedIcon from '@mui/icons-material/SwapHorizOutlined';

import { listarEventos, type EventoResponse } from '../../services/eventos';
import { listarEdicoesPorEvento } from '../../services/edicoes';
import { listarBaterias } from '../../services/baterias';
import {
  listarCorridas, cancelarCorrida, finalizarCorrida,
  listarAlocacoes,
  type CorridaResponse, type AlocacaoResponse,
} from '../../services/corridas';
import { registrarTempo, listarRegistrosCorrida } from '../../services/registros';
import { getSession } from '../../services/auth';

// ─── TIPOS ────────────────────────────────────────────────────────────────────

type Phase = 'init' | 'select-event' | 'waiting' | 'countdown' | 'racing';
type RacingPhase = 'timing' | 'assigning';

interface ActiveContext {
  eventoNome: string;
  edicaoAno: number;
  bateriaNumero: number;
  bateriaTipo?: string;
  corrida: CorridaResponse;
}

interface WaitingInfo {
  edicaoAno?: number;
  bateriaNumero?: number;
  mensagem: string;
  ultimaVerificacao: string;
}

// ─── HELPERS ──────────────────────────────────────────────────────────────────

const formatMs = (ms: number) => {
  const min  = Math.floor(ms / 60000);
  const sec  = Math.floor((ms % 60000) / 1000);
  const cent = Math.floor((ms % 1000) / 10);
  return `${String(min).padStart(2, '0')}:${String(sec).padStart(2, '0')}.${String(cent).padStart(2, '0')}`;
};

const agora = () =>
  new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit', second: '2-digit' });

const posLabel = (i: number) => `${i + 1}º`;

// ─── TELA: SELETOR DE EVENTO ──────────────────────────────────────────────────

const EventoSelector: React.FC<{
  eventos: EventoResponse[];
  onSelecionado: (id: number) => void;
}> = ({ eventos, onSelecionado }) => {
  const [eventoId, setEventoId] = useState<number | ''>('');

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#F4F4F6', display: 'flex', alignItems: 'center', justifyContent: 'center', p: 4 }}>
      <Paper elevation={0} sx={{ p: 4, borderRadius: 3, border: '1px solid #E0E0E6', width: '100%', maxWidth: 420 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 3 }}>
          <TimerOutlinedIcon sx={{ color: '#C8102E', fontSize: 28 }} />
          <Box>
            <Typography variant="h6" sx={{ fontWeight: 900, color: '#1A1A2E', lineHeight: 1 }}>Cronometragem</Typography>
            <Typography variant="body2" color="text.secondary">Selecione o evento</Typography>
          </Box>
        </Box>
        <Stack spacing={2}>
          <TextField select fullWidth label="Evento" value={eventoId} size="small"
            onChange={e => setEventoId(Number(e.target.value))}>
            {eventos.map(ev => <MenuItem key={ev.id} value={ev.id}>{ev.nome}</MenuItem>)}
          </TextField>
          <Button variant="contained" fullWidth disabled={!eventoId}
            onClick={() => eventoId && onSelecionado(eventoId as number)}
            sx={{ py: 1.5, bgcolor: '#C8102E', '&:hover': { bgcolor: '#9B0D23' }, fontWeight: 700 }}>
            Entrar →
          </Button>
        </Stack>
      </Paper>
    </Box>
  );
};

// ─── TELA: AGUARDANDO CORRIDA ─────────────────────────────────────────────────

const WaitingScreen: React.FC<{ info: WaitingInfo | null }> = ({ info }) => (
  <Box sx={{
    minHeight: '100vh', bgcolor: '#1A1A2E', color: 'white',
    display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', p: 4,
  }}>
    <HourglassEmptyOutlinedIcon sx={{ fontSize: 64, opacity: 0.3, mb: 3 }} />

    <Typography variant="h5" sx={{ fontWeight: 900, mb: 1 }}>
      Aguardando corrida
    </Typography>

    <Typography variant="body2" sx={{ opacity: 0.6, mb: 4, textAlign: 'center' }}>
      {info?.mensagem ?? 'Verificando estado da competição...'}
    </Typography>

    {(info?.edicaoAno || info?.bateriaNumero) && (
      <Box sx={{ display: 'flex', gap: 2, mb: 4 }}>
        {info?.edicaoAno && (
          <Chip label={`Edição ${info.edicaoAno}`} size="small"
            sx={{ bgcolor: 'rgba(255,255,255,0.1)', color: 'white' }} />
        )}
        {info?.bateriaNumero && (
          <Chip label={`Bateria #${info.bateriaNumero}`} size="small"
            sx={{ bgcolor: '#C8102E', color: 'white' }} />
        )}
      </Box>
    )}

    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, opacity: 0.4 }}>
      <CircularProgress size={14} sx={{ color: 'white' }} />
      <Typography variant="caption">
        {info?.ultimaVerificacao
          ? `Última verificação: ${info.ultimaVerificacao} · Verificando a cada 5s`
          : 'Verificando...'}
      </Typography>
    </Box>
  </Box>
);

// ─── TELA: CONTAGEM REGRESSIVA ────────────────────────────────────────────────

const CountdownScreen: React.FC<{ ctx: ActiveContext; count: number }> = ({ ctx, count }) => (
  <Box sx={{
    minHeight: '100vh', bgcolor: '#1A1A2E', color: 'white',
    display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', p: 4,
  }}>
    <Typography variant="overline" sx={{ opacity: 0.5, letterSpacing: 3, mb: 1 }}>
      Bateria #{ctx.bateriaNumero}{ctx.bateriaTipo ? ` — ${ctx.bateriaTipo}` : ''} · Corrida {ctx.corrida.ordem}
    </Typography>

    <Typography variant="h6" sx={{ fontWeight: 700, mb: 4, opacity: 0.8 }}>
      {ctx.eventoNome} · {ctx.edicaoAno}
    </Typography>

    <Box sx={{
      width: 180, height: 180, borderRadius: '50%',
      border: '4px solid #C8102E',
      display: 'flex', alignItems: 'center', justifyContent: 'center', mb: 4,
      boxShadow: '0 0 60px #C8102E44',
    }}>
      <Typography sx={{ fontSize: '5rem', fontWeight: 900, fontFamily: 'monospace', color: '#C8102E' }}>
        {count}
      </Typography>
    </Box>

    <Typography variant="h6" sx={{ fontWeight: 700, letterSpacing: 4, opacity: 0.6 }}>
      PREPARE-SE
    </Typography>
  </Box>
);

// ─── PAINEL DE CRONOMETRAGEM ──────────────────────────────────────────────────

interface PainelProps {
  ctx: ActiveContext;
  onCancelado: () => void;
  onFinalizado: () => void;
}

const PainelCorrida: React.FC<PainelProps> = ({ ctx, onCancelado, onFinalizado }) => {
  const session = getSession();

  // Equipes alocadas
  const [teams, setTeams]               = useState<AlocacaoResponse[]>([]);
  const [loadingTeams, setLoadingTeams] = useState(true);

  // Cronômetro
  const [elapsedMs, setElapsed] = useState(0);
  const [running, setRunning]   = useState(false);
  const timerRef  = useRef<ReturnType<typeof setInterval> | null>(null);
  const startRef  = useRef(0);

  // Fase da corrida
  const [racingPhase, setRacingPhase]   = useState<RacingPhase>('timing');
  const [stops, setStops]               = useState<number[]>([]);   // tempos capturados (ms)
  const [assignments, setAssignments]   = useState<(number | null)[]>([]);  // stopIndex → equipeId
  const [dragOverIndex, setDragOverIndex] = useState<number | null>(null);
  const [draggedEquipeId, setDraggedEquipeId] = useState<number | null>(null);

  // Submissão
  const [enviando, setEnviando]   = useState(false);
  const [cancelando, setCancelando] = useState(false);
  const [erro, setErro]           = useState('');
  const [sucesso, setSucesso]     = useState(false);

  useEffect(() => {
    const carregar = async () => {
      setLoadingTeams(true);
      try {
        const alocacoes = await listarAlocacoes(ctx.corrida.id);
        setTeams(alocacoes);
        setAssignments(new Array(alocacoes.length).fill(null));
      } catch {
        setErro('Erro ao carregar equipes da corrida.');
      } finally {
        setLoadingTeams(false);
      }
    };
    carregar();
  }, [ctx.corrida.id]);

  useEffect(() => {
    return () => { if (timerRef.current) clearInterval(timerRef.current); };
  }, []);

  const handleLargada = () => {
    startRef.current = Date.now();
    timerRef.current = setInterval(() => setElapsed(Date.now() - startRef.current), 40);
    setRunning(true);
  };

  const handleParar = () => {
    const tempoAtual = Date.now() - startRef.current;
    const newStops = [...stops, tempoAtual];
    setStops(newStops);

    if (newStops.length >= teams.length) {
      if (timerRef.current) clearInterval(timerRef.current);
      setRacingPhase('assigning');
    }
  };

  // ── Drag-and-drop ──

  const handleDragStart = (equipeId: number) => {
    setDraggedEquipeId(equipeId);
  };

  const handleDragEnd = () => {
    setDraggedEquipeId(null);
    setDragOverIndex(null);
  };

  const handleDropOnSlot = (stopIndex: number) => {
    if (draggedEquipeId === null) return;
    setAssignments(prev => {
      const next = [...prev];
      // remove equipe de qualquer slot anterior
      const prevSlot = next.indexOf(draggedEquipeId);
      if (prevSlot !== -1) next[prevSlot] = null;
      next[stopIndex] = draggedEquipeId;
      return next;
    });
    setDraggedEquipeId(null);
    setDragOverIndex(null);
  };

  const handleUnassign = (stopIndex: number) => {
    setAssignments(prev => {
      const next = [...prev];
      next[stopIndex] = null;
      return next;
    });
  };

  const assignedIds = new Set(assignments.filter((a): a is number => a !== null));
  const unassignedTeams = teams.filter(t => !assignedIds.has(t.equipeId));
  const allAssigned = stops.length === teams.length && assignments.every(a => a !== null);

  const teamById = (id: number) => teams.find(t => t.equipeId === id);

  // ── Cancelar ──

  const handleCancelar = async () => {
    if (!window.confirm('Cancelar esta corrida? Esta ação não pode ser desfeita.')) return;
    setCancelando(true);
    try {
      await cancelarCorrida(ctx.corrida.id);
      if (timerRef.current) clearInterval(timerRef.current);
      onCancelado();
    } catch (err: any) {
      setErro(err?.response?.data?.message ?? 'Erro ao cancelar corrida.');
      setCancelando(false);
    }
  };

  // ── Enviar ──

  const handleEnviar = async () => {
    if (!session || !allAssigned) return;
    setEnviando(true);
    setErro('');
    try {
      const registrosExistentes = await listarRegistrosCorrida(ctx.corrida.id);
      const jaRegistrados = new Set(registrosExistentes.map(r => r.equipeId));

      const novos = stops
        .map((tempoMs, i) => ({ tempoMs, equipeId: assignments[i]! }))
        .filter(r => !jaRegistrados.has(r.equipeId));

      await Promise.all(novos.map(r =>
        registrarTempo(
          { corridaId: ctx.corrida.id, equipeId: r.equipeId, tempoMilissegundos: r.tempoMs, tipoRegistro: 'CHEGADA' },
          session.usuarioId,
        )
      ));

      await finalizarCorrida(ctx.corrida.id);
      setSucesso(true);
    } catch (err: any) {
      setErro(err?.response?.data?.message ?? 'Erro ao enviar resultados.');
      setEnviando(false);
    }
  };

  // ── Tela de sucesso ──

  if (sucesso) {
    const resultados = stops
      .map((tempoMs, i) => ({ tempoMs, equipe: teamById(assignments[i]!) }))
      .sort((a, b) => a.tempoMs - b.tempoMs);

    return (
      <Box sx={{
        minHeight: '100vh', bgcolor: '#1A1A2E', color: 'white',
        display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', p: 4,
      }}>
        <CheckCircleOutlinedIcon sx={{ fontSize: 80, color: '#4ade80', mb: 3 }} />
        <Typography variant="h5" sx={{ fontWeight: 900, mb: 1 }}>Resultados enviados!</Typography>
        <Typography variant="body2" sx={{ opacity: 0.6, mb: 2 }}>
          {stops.length} tempo(s) registrado(s) · Corrida {ctx.corrida.ordem} finalizada
        </Typography>
        <Typography variant="body2" sx={{ opacity: 0.4, mb: 4, fontSize: '0.75rem' }}>
          Bateria #{ctx.bateriaNumero} · {ctx.eventoNome} {ctx.edicaoAno}
        </Typography>

        <Stack spacing={1.5} sx={{ width: '100%', maxWidth: 360 }}>
          {resultados.map(({ tempoMs, equipe }, i) => (
            <Box key={i} sx={{
              display: 'flex', alignItems: 'center', gap: 2,
              bgcolor: 'rgba(255,255,255,0.05)', borderRadius: 2, px: 2, py: 1.5,
            }}>
              <Typography sx={{
                fontWeight: 900, minWidth: 32, fontSize: '0.9rem',
                color: i === 0 ? '#F5A623' : i === 1 ? '#A8A9AD' : i === 2 ? '#CD7F32' : 'white',
              }}>
                {posLabel(i)}
              </Typography>
              <Box sx={{ flex: 1 }}>
                <Typography sx={{ fontWeight: 700, fontSize: '0.9rem' }}>
                  {equipe?.equipeNome ?? '—'}
                </Typography>
                {equipe?.equipeCurso && (
                  <Typography variant="caption" sx={{ opacity: 0.5 }}>{equipe.equipeCurso}</Typography>
                )}
              </Box>
              <Typography sx={{ fontFamily: 'monospace', fontWeight: 900, color: '#4ade80', fontSize: '0.9rem' }}>
                {formatMs(tempoMs)}
              </Typography>
            </Box>
          ))}
        </Stack>

        <Button variant="outlined" onClick={onFinalizado}
          sx={{ mt: 4, color: 'rgba(255,255,255,0.6)', borderColor: 'rgba(255,255,255,0.2)' }}>
          Próxima corrida
        </Button>
      </Box>
    );
  }

  // ── Fase de atribuição ──

  if (racingPhase === 'assigning') {
    // tempos já estão em ordem crescente (stops capturados sequencialmente)
    const sortedStops = [...stops].map((ms, i) => ({ ms, originalIndex: i })).sort((a, b) => a.ms - b.ms);

    return (
      <Box sx={{ minHeight: '100vh', bgcolor: '#1A1A2E', color: 'white', p: { xs: 2, md: 4 } }}>
        {/* Cabeçalho */}
        <Box sx={{ mb: 4 }}>
          <Typography variant="overline" sx={{ opacity: 0.5, letterSpacing: 2 }}>
            Bateria #{ctx.bateriaNumero} · Corrida {ctx.corrida.ordem}
          </Typography>
          <Typography variant="h5" sx={{ fontWeight: 900, mt: 0.5 }}>
            Atribuir equipes aos tempos
          </Typography>
          <Typography variant="body2" sx={{ opacity: 0.6, mt: 0.5 }}>
            Arraste os nomes das equipes para os tempos correspondentes. Todas as equipes devem ser atribuídas antes de enviar.
          </Typography>
        </Box>

        {erro && <Alert severity="error" sx={{ mb: 3, borderRadius: 2 }}>{erro}</Alert>}

        <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 3 }}>
          {/* ── Coluna esquerda: tempos (drop targets) ── */}
          <Box sx={{ flex: 1 }}>
            <Typography variant="overline" sx={{ opacity: 0.5, letterSpacing: 2, mb: 2, display: 'block' }}>
              Tempos registrados
            </Typography>
            <Stack spacing={1.5}>
              {sortedStops.map(({ ms, originalIndex }, displayIndex) => {
                const assignedEquipeId = assignments[originalIndex];
                const assignedTeam = assignedEquipeId !== null ? teamById(assignedEquipeId) : null;
                const isOver = dragOverIndex === originalIndex;

                return (
                  <Box
                    key={originalIndex}
                    onDragOver={e => { e.preventDefault(); setDragOverIndex(originalIndex); }}
                    onDragLeave={() => setDragOverIndex(null)}
                    onDrop={() => handleDropOnSlot(originalIndex)}
                    sx={{
                      display: 'flex', alignItems: 'center', gap: 2,
                      p: 2, borderRadius: 2,
                      border: '2px dashed',
                      borderColor: isOver ? '#4ade80' : assignedTeam ? '#22B573' : 'rgba(255,255,255,0.15)',
                      bgcolor: isOver ? 'rgba(74,222,128,0.08)' : assignedTeam ? 'rgba(34,181,115,0.08)' : 'rgba(255,255,255,0.04)',
                      transition: '0.15s',
                      cursor: 'default',
                    }}
                  >
                    {/* Posição */}
                    <Typography sx={{
                      fontWeight: 900, minWidth: 32, fontSize: '1rem',
                      color: displayIndex === 0 ? '#F5A623' : displayIndex === 1 ? '#A8A9AD' : displayIndex === 2 ? '#CD7F32' : 'rgba(255,255,255,0.5)',
                    }}>
                      {posLabel(displayIndex)}
                    </Typography>

                    {/* Tempo */}
                    <Typography sx={{ fontFamily: 'monospace', fontWeight: 900, fontSize: '1.1rem', color: '#4ade80', minWidth: 100 }}>
                      {formatMs(ms)}
                    </Typography>

                    {/* Zona de drop / equipe atribuída */}
                    <Box sx={{ flex: 1 }}>
                      {assignedTeam ? (
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Typography sx={{ fontWeight: 700 }}>{assignedTeam.equipeNome}</Typography>
                          {assignedTeam.equipeCurso && (
                            <Typography variant="caption" sx={{ opacity: 0.5 }}>{assignedTeam.equipeCurso}</Typography>
                          )}
                        </Box>
                      ) : (
                        <Typography sx={{ opacity: 0.3, fontStyle: 'italic', fontSize: '0.875rem' }}>
                          {isOver ? 'Solte aqui' : 'Arraste uma equipe aqui'}
                        </Typography>
                      )}
                    </Box>

                    {/* Botão remover atribuição */}
                    {assignedTeam && (
                      <Button
                        size="small"
                        onClick={() => handleUnassign(originalIndex)}
                        sx={{ minWidth: 0, color: 'rgba(255,255,255,0.4)', '&:hover': { color: '#C8102E' }, p: 0.5 }}
                      >
                        ✕
                      </Button>
                    )}
                  </Box>
                );
              })}
            </Stack>
          </Box>

          {/* ── Coluna direita: equipes não atribuídas ── */}
          <Box sx={{ width: { xs: '100%', md: 260 } }}>
            <Typography variant="overline" sx={{ opacity: 0.5, letterSpacing: 2, mb: 2, display: 'block' }}>
              Equipes ({unassignedTeams.length} restantes)
            </Typography>

            {unassignedTeams.length === 0 ? (
              <Box sx={{
                p: 3, borderRadius: 2, border: '2px dashed rgba(74,222,128,0.3)',
                bgcolor: 'rgba(74,222,128,0.05)', textAlign: 'center',
              }}>
                <CheckCircleOutlinedIcon sx={{ color: '#4ade80', fontSize: 32, mb: 1 }} />
                <Typography variant="body2" sx={{ color: '#4ade80', fontWeight: 700 }}>
                  Todas atribuídas!
                </Typography>
              </Box>
            ) : (
              <Stack spacing={1.5}>
                {unassignedTeams.map(team => (
                  <Box
                    key={team.equipeId}
                    draggable
                    onDragStart={() => handleDragStart(team.equipeId)}
                    onDragEnd={handleDragEnd}
                    sx={{
                      p: 2, borderRadius: 2,
                      bgcolor: draggedEquipeId === team.equipeId ? 'rgba(200,16,46,0.2)' : 'rgba(255,255,255,0.08)',
                      border: '1px solid',
                      borderColor: draggedEquipeId === team.equipeId ? '#C8102E' : 'rgba(255,255,255,0.1)',
                      cursor: 'grab',
                      display: 'flex', alignItems: 'center', gap: 1.5,
                      transition: '0.15s',
                      '&:active': { cursor: 'grabbing' },
                    }}
                  >
                    <SwapHorizOutlinedIcon sx={{ opacity: 0.4, fontSize: 18 }} />
                    <Box>
                      <Typography sx={{ fontWeight: 700, fontSize: '0.9rem' }}>{team.equipeNome}</Typography>
                      {team.equipeCurso && (
                        <Typography variant="caption" sx={{ opacity: 0.5 }}>{team.equipeCurso}</Typography>
                      )}
                    </Box>
                  </Box>
                ))}
              </Stack>
            )}
          </Box>
        </Box>

        {/* ── Rodapé com ações ── */}
        <Box sx={{ mt: 4, display: 'flex', gap: 2, flexDirection: { xs: 'column', sm: 'row' } }}>
          <Button
            variant="contained" size="large"
            startIcon={enviando ? undefined : <SendOutlinedIcon />}
            disabled={!allAssigned || enviando || cancelando}
            onClick={handleEnviar}
            sx={{
              flex: 1, py: 2, fontWeight: 800,
              bgcolor: allAssigned ? '#22B573' : 'rgba(255,255,255,0.1)',
              '&:hover': { bgcolor: '#1a9660' },
              '&.Mui-disabled': { bgcolor: 'rgba(255,255,255,0.1)', color: 'rgba(255,255,255,0.3)' },
            }}
          >
            {enviando
              ? <CircularProgress size={22} sx={{ color: 'white' }} />
              : allAssigned
                ? 'ENVIAR RESULTADOS'
                : `ENVIAR (${assignedIds.size}/${teams.length} atribuídas)`}
          </Button>

          <Button
            variant="outlined" size="large"
            startIcon={cancelando ? undefined : <CancelOutlinedIcon />}
            disabled={enviando || cancelando}
            onClick={handleCancelar}
            sx={{ py: 1.5, fontWeight: 700, color: '#C8102E', borderColor: '#C8102E44' }}
          >
            {cancelando ? <CircularProgress size={18} sx={{ color: '#C8102E' }} /> : 'Cancelar corrida'}
          </Button>
        </Box>
      </Box>
    );
  }

  // ── Fase de cronometragem ──

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', flexDirection: { xs: 'column', md: 'row' }, bgcolor: '#F4F4F6' }}>

      {/* ── PAINEL ESQUERDO: CRONÔMETRO ── */}
      <Box sx={{
        width: { xs: '100%', md: '360px' }, bgcolor: '#1A1A2E', color: 'white', p: 4,
        display: 'flex', flexDirection: 'column', justifyContent: 'space-between',
      }}>
        <Box>
          <Typography variant="overline" sx={{ opacity: 0.5, letterSpacing: 2 }}>
            Bateria #{ctx.bateriaNumero}{ctx.bateriaTipo ? ` — ${ctx.bateriaTipo}` : ''}
          </Typography>
          <Typography variant="h5" sx={{ fontWeight: 800, mb: 3 }}>
            Corrida {ctx.corrida.ordem}
          </Typography>

          <Divider sx={{ bgcolor: 'rgba(255,255,255,0.1)', mb: 4 }} />

          <Box sx={{ textAlign: 'center', mb: 4 }}>
            <Typography variant="h2" sx={{
              fontFamily: 'monospace', fontWeight: 900,
              color: running ? '#4ade80' : 'rgba(255,255,255,0.2)',
              fontSize: { xs: '3.5rem', md: '4rem' },
              transition: 'color 0.3s',
            }}>
              {formatMs(elapsedMs)}
            </Typography>
            <Typography variant="caption" sx={{ opacity: 0.4 }}>
              {running ? 'TEMPO DE PROVA' : 'AGUARDANDO LARGADA'}
            </Typography>
          </Box>

          <Stack spacing={2}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
              <Typography variant="body2" sx={{ opacity: 0.5 }}>Equipes na pista</Typography>
              <Typography variant="body2" sx={{ fontWeight: 700 }}>{teams.length}</Typography>
            </Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
              <Typography variant="body2" sx={{ opacity: 0.5 }}>Chegadas registradas</Typography>
              <Typography variant="body2" sx={{ fontWeight: 700, color: running ? '#4ade80' : 'white' }}>
                {stops.length} / {teams.length}
              </Typography>
            </Box>
          </Stack>

          {/* Mini-lista das chegadas registradas */}
          {stops.length > 0 && (
            <Box sx={{ mt: 3 }}>
              <Divider sx={{ bgcolor: 'rgba(255,255,255,0.1)', mb: 2 }} />
              <Typography variant="caption" sx={{ opacity: 0.4, display: 'block', mb: 1 }}>
                Tempos capturados
              </Typography>
              <Stack spacing={0.5}>
                {stops.map((ms, i) => (
                  <Box key={i} sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="caption" sx={{ opacity: 0.5 }}>{posLabel(i)}</Typography>
                    <Typography variant="caption" sx={{ fontFamily: 'monospace', color: '#4ade80', fontWeight: 700 }}>
                      {formatMs(ms)}
                    </Typography>
                  </Box>
                ))}
              </Stack>
            </Box>
          )}
        </Box>

        <Stack spacing={2} sx={{ mt: 4 }}>
          {!running ? (
            <Button
              variant="contained" fullWidth size="large"
              startIcon={<FlagOutlinedIcon />}
              disabled={loadingTeams || cancelando || teams.length === 0}
              onClick={handleLargada}
              sx={{
                bgcolor: '#C8102E', py: 2.5, fontWeight: 900, fontSize: '1.1rem',
                '&:hover': { bgcolor: '#9B0D23' },
                boxShadow: '0 0 30px #C8102E66',
              }}
            >
              LARGADA
            </Button>
          ) : (
            <Button
              variant="contained" fullWidth size="large"
              startIcon={<StopCircleOutlinedIcon />}
              disabled={cancelando}
              onClick={handleParar}
              sx={{
                bgcolor: '#F5A623', py: 2.5, fontWeight: 900, fontSize: '1.1rem',
                '&:hover': { bgcolor: '#d4891a' },
                boxShadow: '0 0 30px #F5A62366',
                animation: 'pulse 1.5s ease-in-out infinite',
                '@keyframes pulse': {
                  '0%, 100%': { boxShadow: '0 0 30px #F5A62366' },
                  '50%':      { boxShadow: '0 0 50px #F5A623AA' },
                },
              }}
            >
              PARAR ({stops.length + 1}ª chegada)
            </Button>
          )}

          <Button
            variant="outlined" fullWidth size="large"
            startIcon={cancelando ? undefined : <CancelOutlinedIcon />}
            disabled={enviando || cancelando}
            onClick={handleCancelar}
            sx={{ color: '#C8102E', borderColor: '#C8102E44', py: 1.5, fontWeight: 700 }}
          >
            {cancelando ? <CircularProgress size={18} sx={{ color: '#C8102E' }} /> : 'Cancelar corrida'}
          </Button>
        </Stack>
      </Box>

      {/* ── PAINEL DIREITO: INFO DAS EQUIPES ── */}
      <Box sx={{ flex: 1, p: { xs: 2, md: 4 }, overflowY: 'auto' }}>
        <Typography variant="h6" sx={{ mb: 0.5, fontWeight: 800, color: '#1A1A2E' }}>
          Equipes na Pista
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Pressione <strong>PARAR</strong> a cada vez que uma equipe cruzar a linha de chegada.
          Após a {teams.length > 0 ? `${teams.length}ª` : 'última'} chegada, você atribuirá os nomes aos tempos.
        </Typography>

        {erro && <Alert severity="error" sx={{ mb: 2, borderRadius: 2 }}>{erro}</Alert>}

        {loadingTeams
          ? <Box sx={{ textAlign: 'center', py: 6 }}><CircularProgress /></Box>
          : teams.length === 0
            ? <Alert severity="info" sx={{ borderRadius: 2 }}>Nenhuma equipe alocada nesta corrida.</Alert>
            : (
              <Stack spacing={2}>
                {teams.map((team, i) => {
                  const chegou = i < stops.length;
                  return (
                    <Paper key={team.equipeId} elevation={0} sx={{
                      p: 3, borderRadius: 3,
                      display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                      borderLeft: '6px solid',
                      borderColor: chegou ? '#22B573' : '#E0E0E6',
                      opacity: chegou ? 0.6 : 1,
                      transition: '0.2s',
                    }}>
                      <Box>
                        <Typography variant="subtitle1" sx={{ fontWeight: 800 }}>{team.equipeNome}</Typography>
                        {team.equipeCurso && (
                          <Typography variant="caption" color="text.secondary">{team.equipeCurso}</Typography>
                        )}
                      </Box>
                      <Chip
                        label={chegou ? `${posLabel(i)} chegou` : 'Na pista'}
                        size="small"
                        sx={{
                          bgcolor: chegou ? '#22B573' : '#E0E0E6',
                          color: chegou ? 'white' : '#666',
                          fontWeight: 700,
                        }}
                      />
                    </Paper>
                  );
                })}
              </Stack>
            )
        }
      </Box>
    </Box>
  );
};

// ─── PÁGINA PRINCIPAL (máquina de estados) ────────────────────────────────────

const CronometragemPage: React.FC = () => {
  const [phase, setPhase]           = useState<Phase>('init');
  const [eventos, setEventos]       = useState<EventoResponse[]>([]);
  const [eventoId, setEventoId]     = useState<number | null>(null);
  const [ctx, setCtx]               = useState<ActiveContext | null>(null);
  const [waitingInfo, setWaitingInfo] = useState<WaitingInfo | null>(null);
  const [countdown, setCountdown]   = useState(5);

  const detectar = useCallback(async (evId: number, eventosLista: EventoResponse[]) => {
    try {
      const edicoes = await listarEdicoesPorEvento(evId);
      const edicao  = edicoes.find(e => e.status === 'EM_ANDAMENTO');
      if (!edicao) {
        setWaitingInfo({ mensagem: 'Nenhuma edição em andamento.', ultimaVerificacao: agora() });
        return null;
      }

      const baterias = await listarBaterias(edicao.id);
      const bateria  = baterias.find(b => b.status === 'EM_ANDAMENTO');
      if (!bateria) {
        setWaitingInfo({
          edicaoAno: edicao.ano,
          mensagem: 'Nenhuma bateria em andamento.',
          ultimaVerificacao: agora(),
        });
        return null;
      }

      const corridas = await listarCorridas(bateria.id);
      const corrida  = corridas.find(c => c.status === 'EM_ANDAMENTO');
      if (!corrida) {
        setWaitingInfo({
          edicaoAno: edicao.ano,
          bateriaNumero: bateria.numero,
          mensagem: 'Aguardando corrida ser iniciada...',
          ultimaVerificacao: agora(),
        });
        return null;
      }

      const ev = eventosLista.find(e => e.id === evId);
      return {
        eventoNome: ev?.nome ?? `Evento #${evId}`,
        edicaoAno: edicao.ano,
        bateriaNumero: bateria.numero,
        bateriaTipo: bateria.tipo,
        corrida,
      } satisfies ActiveContext;
    } catch {
      setWaitingInfo(prev => ({
        ...prev,
        mensagem: 'Erro ao verificar. Tentando novamente...',
        ultimaVerificacao: agora(),
      }));
      return null;
    }
  }, []);

  useEffect(() => {
    listarEventos().then(evs => {
      setEventos(evs);
      if (evs.length === 0) {
        setWaitingInfo({ mensagem: 'Nenhum evento cadastrado.', ultimaVerificacao: agora() });
        setPhase('waiting');
      } else if (evs.length === 1) {
        setEventoId(evs[0].id);
        setPhase('waiting');
      } else {
        setPhase('select-event');
      }
    }).catch(() => {
      setWaitingInfo({ mensagem: 'Erro ao carregar eventos.', ultimaVerificacao: agora() });
      setPhase('waiting');
    });
  }, []);

  useEffect(() => {
    if (phase !== 'waiting' || !eventoId) return;

    const poll = async () => {
      const found = await detectar(eventoId, eventos);
      if (found) {
        setCtx(found);
        setPhase('countdown');
      }
    };

    poll();
    const id = setInterval(poll, 5000);
    return () => clearInterval(id);
  }, [phase, eventoId, detectar, eventos]);

  useEffect(() => {
    if (phase !== 'countdown') return;
    setCountdown(5);
    let c = 5;
    const id = setInterval(() => {
      c -= 1;
      setCountdown(c);
      if (c <= 0) { clearInterval(id); setPhase('racing'); }
    }, 1000);
    return () => clearInterval(id);
  }, [phase]);

  const handleEventoSelecionado = (id: number) => {
    setEventoId(id);
    setPhase('waiting');
  };

  const handleCancelado = () => {
    setCtx(null);
    setPhase('waiting');
  };

  const handleFinalizado = () => {
    setCtx(null);
    setPhase('waiting');
  };

  if (phase === 'init') {
    return (
      <Box sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: '#1A1A2E' }}>
        <CircularProgress sx={{ color: 'white' }} />
      </Box>
    );
  }

  if (phase === 'select-event') {
    return <EventoSelector eventos={eventos} onSelecionado={handleEventoSelecionado} />;
  }

  if (phase === 'waiting') {
    return <WaitingScreen info={waitingInfo} />;
  }

  if (phase === 'countdown' && ctx) {
    return <CountdownScreen ctx={ctx} count={countdown} />;
  }

  if (phase === 'racing' && ctx) {
    return (
      <PainelCorrida
        key={ctx.corrida.id}
        ctx={ctx}
        onCancelado={handleCancelado}
        onFinalizado={handleFinalizado}
      />
    );
  }

  return null;
};

export { CronometragemPage };
