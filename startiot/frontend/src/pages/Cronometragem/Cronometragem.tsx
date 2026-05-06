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

interface TeamRecord extends AlocacaoResponse {
  tempoMs: number | null;
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

  const [teams, setTeams]           = useState<TeamRecord[]>([]);
  const [loadingTeams, setLoadingTeams] = useState(true);
  const [elapsedMs, setElapsed]     = useState(0);
  const [running, setRunning]       = useState(false);
  const [enviando, setEnviando]     = useState(false);
  const [cancelando, setCancelando] = useState(false);
  const [erro, setErro]             = useState('');
  const [sucesso, setSucesso]       = useState(false);

  const timerRef  = useRef<ReturnType<typeof setInterval> | null>(null);
  const startRef  = useRef(0);

  // Carrega equipes e registros já existentes
  useEffect(() => {
    const carregar = async () => {
      setLoadingTeams(true);
      try {
        const [alocacoes, registros] = await Promise.all([
          listarAlocacoes(ctx.corrida.id),
          listarRegistrosCorrida(ctx.corrida.id),
        ]);
        const registradosIds = new Set(registros.map(r => r.equipeId));
        const registrosPorEquipe = new Map(registros.map(r => [r.equipeId, r.tempoMilissegundos]));

        setTeams(alocacoes.map(a => ({
          ...a,
          tempoMs: registradosIds.has(a.equipeId) ? (registrosPorEquipe.get(a.equipeId) ?? null) : null,
        })));
      } catch {
        setErro('Erro ao carregar equipes da corrida.');
      } finally {
        setLoadingTeams(false);
      }
    };
    carregar();
  }, [ctx.corrida.id]);

  // Cleanup do timer ao desmontar
  useEffect(() => {
    return () => { if (timerRef.current) clearInterval(timerRef.current); };
  }, []);

  const handleLargada = () => {
    startRef.current = Date.now();
    timerRef.current = setInterval(() => {
      setElapsed(Date.now() - startRef.current);
    }, 40);
    setRunning(true);
  };

  const registrarChegada = (equipeId: number) => {
    const tempoAtual = Date.now() - startRef.current;
    setTeams(prev => prev.map(t => t.equipeId === equipeId ? { ...t, tempoMs: tempoAtual } : t));
  };

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

  const handleEnviar = async () => {
    if (!session) return;
    setEnviando(true);
    setErro('');
    try {
      // Envia somente times que foram registrados localmente e ainda não existem no backend
      const paraEnviar = teams.filter(t => t.tempoMs !== null);

      // Carrega registros já existentes para evitar duplicatas
      const registrosExistentes = await listarRegistrosCorrida(ctx.corrida.id);
      const jaRegistrados = new Set(registrosExistentes.map(r => r.equipeId));

      const novos = paraEnviar.filter(t => !jaRegistrados.has(t.equipeId));

      await Promise.all(novos.map(t =>
        registrarTempo(
          { corridaId: ctx.corrida.id, equipeId: t.equipeId, tempoMilissegundos: t.tempoMs!, tipoRegistro: 'CHEGADA' },
          session.usuarioId,
        )
      ));

      await finalizarCorrida(ctx.corrida.id);

      if (timerRef.current) clearInterval(timerRef.current);
      setSucesso(true);
    } catch (err: any) {
      setErro(err?.response?.data?.message ?? 'Erro ao enviar resultados.');
      setEnviando(false);
    }
  };

  const registradas = teams.filter(t => t.tempoMs !== null).length;

  if (sucesso) {
    return (
      <Box sx={{
        minHeight: '100vh', bgcolor: '#1A1A2E', color: 'white',
        display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', p: 4,
      }}>
        <CheckCircleOutlinedIcon sx={{ fontSize: 80, color: '#4ade80', mb: 3 }} />
        <Typography variant="h5" sx={{ fontWeight: 900, mb: 1 }}>Resultados enviados!</Typography>
        <Typography variant="body2" sx={{ opacity: 0.6, mb: 2 }}>
          {registradas} tempo(s) registrado(s) · Corrida {ctx.corrida.ordem} finalizada
        </Typography>
        <Typography variant="body2" sx={{ opacity: 0.4, mb: 4, fontSize: '0.75rem' }}>
          Bateria #{ctx.bateriaNumero} · {ctx.eventoNome} {ctx.edicaoAno}
        </Typography>

        <Stack spacing={1.5} sx={{ width: '100%', maxWidth: 320 }}>
          {teams.filter(t => t.tempoMs !== null).sort((a, b) => a.tempoMs! - b.tempoMs!).map((t, i) => (
            <Box key={t.equipeId} sx={{ display: 'flex', alignItems: 'center', gap: 2,
              bgcolor: 'rgba(255,255,255,0.05)', borderRadius: 2, px: 2, py: 1.5 }}>
              <Typography sx={{ fontWeight: 900, color: i === 0 ? '#F5A623' : i === 1 ? '#A8A9AD' : '#CD7F32',
                minWidth: 28, fontSize: '0.9rem' }}>
                {i + 1}º
              </Typography>
              <Box sx={{ flex: 1 }}>
                <Typography sx={{ fontWeight: 700, fontSize: '0.9rem' }}>{t.equipeNome}</Typography>
              </Box>
              <Typography sx={{ fontFamily: 'monospace', fontWeight: 900, color: '#4ade80', fontSize: '0.9rem' }}>
                {formatMs(t.tempoMs!)}
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

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', flexDirection: { xs: 'column', md: 'row' }, bgcolor: '#F4F4F6' }}>

      {/* ── PAINEL ESQUERDO: CRONÔMETRO ── */}
      <Box sx={{ width: { xs: '100%', md: '360px' }, bgcolor: '#1A1A2E', color: 'white', p: 4,
        display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
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
                {registradas} / {teams.length}
              </Typography>
            </Box>
          </Stack>
        </Box>

        <Stack spacing={2} sx={{ mt: 4 }}>
          {!running && (
            <Button
              variant="contained" fullWidth size="large"
              startIcon={<FlagOutlinedIcon />}
              disabled={loadingTeams || cancelando}
              onClick={handleLargada}
              sx={{ bgcolor: '#C8102E', py: 2.5, fontWeight: 900, fontSize: '1.1rem',
                '&:hover': { bgcolor: '#9B0D23' },
                boxShadow: '0 0 30px #C8102E66' }}>
              LARGADA
            </Button>
          )}

          {running && (
            <Button
              variant="contained" fullWidth size="large"
              startIcon={enviando ? undefined : <SendOutlinedIcon />}
              disabled={enviando || cancelando || registradas === 0}
              onClick={handleEnviar}
              sx={{ bgcolor: '#22B573', py: 2, fontWeight: 800, '&:hover': { bgcolor: '#1a9660' } }}>
              {enviando ? <CircularProgress size={22} sx={{ color: 'white' }} /> : 'ENVIAR RESULTADOS'}
            </Button>
          )}

          <Button
            variant="outlined" fullWidth size="large"
            startIcon={cancelando ? undefined : <CancelOutlinedIcon />}
            disabled={enviando || cancelando}
            onClick={handleCancelar}
            sx={{ color: '#C8102E', borderColor: '#C8102E44', py: 1.5, fontWeight: 700 }}>
            {cancelando ? <CircularProgress size={18} sx={{ color: '#C8102E' }} /> : 'Cancelar corrida'}
          </Button>
        </Stack>
      </Box>

      {/* ── PAINEL DIREITO: EQUIPES ── */}
      <Box sx={{ flex: 1, p: { xs: 2, md: 4 }, overflowY: 'auto' }}>
        <Typography variant="h6" sx={{ mb: 0.5, fontWeight: 800, color: '#1A1A2E' }}>
          Equipes na Pista
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Clique em CHEGOU! assim que a equipe cruzar a linha de chegada.
        </Typography>

        {erro && <Alert severity="error" sx={{ mb: 2, borderRadius: 2 }}>{erro}</Alert>}

        {loadingTeams
          ? <Box sx={{ textAlign: 'center', py: 6 }}><CircularProgress /></Box>
          : teams.length === 0
            ? <Alert severity="info" sx={{ borderRadius: 2 }}>Nenhuma equipe alocada nesta corrida.</Alert>
            : (
              <Stack spacing={2}>
                {teams.map(team => (
                  <Paper key={team.equipeId} elevation={0} sx={{
                    p: 3, borderRadius: 3,
                    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                    borderLeft: '6px solid',
                    borderColor: team.tempoMs !== null ? '#22B573' : '#C8102E',
                    opacity: team.tempoMs !== null ? 0.7 : 1,
                    transition: '0.2s',
                  }}>
                    <Box>
                      <Typography variant="subtitle1" sx={{ fontWeight: 800 }}>{team.equipeNome}</Typography>
                      {team.equipeCurso && (
                        <Typography variant="caption" color="text.secondary">{team.equipeCurso}</Typography>
                      )}
                    </Box>

                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                      {team.tempoMs !== null && (
                        <Typography variant="h6" sx={{ fontWeight: 900, color: '#22B573', fontFamily: 'monospace' }}>
                          {formatMs(team.tempoMs)}
                        </Typography>
                      )}
                      <Button
                        variant={team.tempoMs !== null ? 'outlined' : 'contained'}
                        color={team.tempoMs !== null ? 'success' : 'error'}
                        disabled={!running || team.tempoMs !== null || enviando}
                        onClick={() => registrarChegada(team.equipeId)}
                        sx={{ borderRadius: 2, px: 3, fontWeight: 800, minWidth: 110,
                          ...(team.tempoMs === null && running && { fontSize: '1rem', py: 1.5 }) }}>
                        {team.tempoMs !== null ? 'CHEGOU ✓' : 'CHEGOU!'}
                      </Button>
                    </Box>
                  </Paper>
                ))}
              </Stack>
            )
        }

        {registradas > 0 && registradas < teams.length && (
          <Alert severity="info" sx={{ mt: 3, borderRadius: 2 }}>
            {teams.length - registradas} equipe(s) ainda não chegaram. Você pode enviar os resultados parciais
            a qualquer momento.
          </Alert>
        )}
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

  // Tenta detectar corrida EM_ANDAMENTO a partir de um eventoId
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

  // Carga inicial: busca eventos
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

  // Polling a cada 5s enquanto em espera
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

  // Contagem regressiva
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
