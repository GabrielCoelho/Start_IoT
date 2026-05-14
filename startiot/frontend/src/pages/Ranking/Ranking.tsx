import React, { useCallback, useEffect, useRef, useState } from 'react';
import {
  Alert, Box, Chip, CircularProgress, IconButton, MenuItem,
  Paper, Stack, Table, TableBody, TableCell, TableContainer, TableHead,
  TableRow, TextField, Typography,
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import LeaderboardOutlinedIcon from '@mui/icons-material/LeaderboardOutlined';

import { listarEventos, type EventoResponse } from '../../services/eventos';
import { listarEdicoesPorEvento, type EdicaoResponse } from '../../services/edicoes';
import { calcularRanking, type RankingResponse, type BateriaInfo } from '../../services/ranking';

const POLL_INTERVAL_MS = 20_000;

// ─── HELPERS ─────────────────────────────────────────────────────────────────

const formatMs = (ms: number) => {
  const min  = Math.floor(ms / 60000);
  const sec  = Math.floor((ms % 60000) / 1000);
  const cent = Math.floor((ms % 1000) / 10);
  return `${String(min).padStart(2, '0')}:${String(sec).padStart(2, '0')}.${String(cent).padStart(2, '0')}`;
};

const posColor = (pos: number) => {
  if (pos === 1) return '#F5A623';
  if (pos === 2) return '#A8A9AD';
  if (pos === 3) return '#CD7F32';
  return '#9A9AAF';
};

const bateriaLabel = (b: BateriaInfo) =>
  b.tipo ? `Bat. ${b.numero} — ${b.tipo}` : `Bat. ${b.numero}`;

const agora = () =>
  new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit', second: '2-digit' });

// ─── SELETOR MANUAL ───────────────────────────────────────────────────────────

interface SeletorManualProps {
  eventos: EventoResponse[];
  onConfirmar: (edicaoId: number, label: string) => void;
}

const SeletorManual: React.FC<SeletorManualProps> = ({ eventos, onConfirmar }) => {
  const [eventoId, setEventoId] = useState<number | ''>('');
  const [edicoes, setEdicoes]   = useState<EdicaoResponse[]>([]);
  const [edicaoId, setEdicaoId] = useState<number | ''>('');

  useEffect(() => {
    if (!eventoId) { setEdicoes([]); setEdicaoId(''); return; }
    listarEdicoesPorEvento(eventoId as number).then(setEdicoes).catch(() => {});
  }, [eventoId]);

  const handleOk = () => {
    if (!edicaoId) return;
    const ev = eventos.find(e => e.id === eventoId);
    const ed = edicoes.find(e => e.id === edicaoId);
    onConfirmar(edicaoId as number, `${ev?.nome ?? ''} · ${ed?.ano ?? ''}`);
  };

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#F4F4F6', display: 'flex', alignItems: 'center', justifyContent: 'center', p: 4 }}>
      <Paper elevation={0} sx={{ p: 4, borderRadius: 3, border: '1px solid #E0E0E6', width: '100%', maxWidth: 440 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 3 }}>
          <LeaderboardOutlinedIcon sx={{ color: '#AD1457', fontSize: 28 }} />
          <Box>
            <Typography variant="h6" sx={{ fontWeight: 900, color: '#1A1A2E', lineHeight: 1 }}>Ranking</Typography>
            <Typography variant="body2" color="text.secondary">Nenhuma edição em andamento detectada. Selecione manualmente.</Typography>
          </Box>
        </Box>
        <Stack spacing={2}>
          <TextField select fullWidth label="Evento" value={eventoId} size="small"
            onChange={e => { setEventoId(Number(e.target.value)); setEdicaoId(''); }}>
            {eventos.map(ev => <MenuItem key={ev.id} value={ev.id}>{ev.nome}</MenuItem>)}
          </TextField>
          <TextField select fullWidth label="Edição" value={edicaoId} size="small"
            disabled={!eventoId || edicoes.length === 0}
            onChange={e => setEdicaoId(Number(e.target.value))}>
            {edicoes.map(ed => <MenuItem key={ed.id} value={ed.id}>{ed.ano} — {ed.status}</MenuItem>)}
          </TextField>
          <Box
            component="button" onClick={handleOk} disabled={!edicaoId}
            sx={{
              py: 1.5, px: 2, borderRadius: 2, border: 'none', cursor: 'pointer',
              bgcolor: !edicaoId ? '#E0E0E6' : '#AD1457', color: 'white',
              fontWeight: 700, fontSize: '0.95rem', transition: '0.2s',
              '&:hover': { bgcolor: !edicaoId ? '#E0E0E6' : '#880E4F' },
            }}>
            Ver ranking →
          </Box>
        </Stack>
      </Paper>
    </Box>
  );
};

// ─── TABELA DE RANKING ────────────────────────────────────────────────────────

interface RankingTableProps {
  edicaoId: number;
  label: string;
  onTrocar: () => void;
}

const RankingTable: React.FC<RankingTableProps> = ({ edicaoId, label, onTrocar }) => {
  const [ranking, setRanking]       = useState<RankingResponse | null>(null);
  const [loading, setLoading]       = useState(true);
  const [erro, setErro]             = useState('');
  const [lastUpdate, setLastUpdate] = useState('');
  const [countdown, setCountdown]   = useState(POLL_INTERVAL_MS / 1000);

  const pollRef      = useRef<ReturnType<typeof setInterval> | null>(null);
  const countdownRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const carregar = useCallback(async () => {
    setErro('');
    try {
      const data = await calcularRanking(edicaoId);
      setRanking(data);
      setLastUpdate(agora());
    } catch {
      setErro('Não foi possível carregar o ranking.');
    } finally {
      setLoading(false);
    }
  }, [edicaoId]);

  const resetCountdown = useCallback(() => {
    setCountdown(POLL_INTERVAL_MS / 1000);
    if (countdownRef.current) clearInterval(countdownRef.current);
    countdownRef.current = setInterval(() => {
      setCountdown(prev => (prev <= 1 ? POLL_INTERVAL_MS / 1000 : prev - 1));
    }, 1000);
  }, []);

  useEffect(() => {
    carregar();
    resetCountdown();
    pollRef.current = setInterval(() => { carregar(); resetCountdown(); }, POLL_INTERVAL_MS);
    return () => {
      if (pollRef.current) clearInterval(pollRef.current);
      if (countdownRef.current) clearInterval(countdownRef.current);
    };
  }, [carregar, resetCountdown]);

  const handleRefresh = () => {
    setLoading(true);
    carregar();
    resetCountdown();
    if (pollRef.current) clearInterval(pollRef.current);
    pollRef.current = setInterval(() => { carregar(); resetCountdown(); }, POLL_INTERVAL_MS);
  };

  const baterias = ranking?.baterias ?? [];

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#F4F4F6', p: { xs: 1, md: 3 } }}>

      {/* HEADER */}
      <Paper elevation={0} sx={{
        p: 3, mb: 2, bgcolor: '#1A1A2E', color: 'white',
        display: 'flex', justifyContent: 'space-between', alignItems: 'center',
        borderRadius: 2,
      }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 900 }}>Ranking Geral</Typography>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5, flexWrap: 'wrap' }}>
            {ranking && (
              <Chip label={`${ranking.anoEdicao}`} size="small"
                sx={{ bgcolor: '#AD1457', color: 'white', fontWeight: 800, fontSize: '0.6rem' }} />
            )}
            {baterias.length > 0 && (
              <Chip label={`${baterias.length} bateria(s) com resultados`} size="small"
                sx={{ bgcolor: 'rgba(255,255,255,0.1)', color: 'rgba(255,255,255,0.7)', fontSize: '0.6rem' }} />
            )}
            <Typography variant="caption" sx={{ opacity: 0.6 }}>{label}</Typography>
          </Box>
        </Box>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexShrink: 0 }}>
          <Box sx={{ textAlign: 'right', display: { xs: 'none', sm: 'block' } }}>
            {lastUpdate && (
              <Typography variant="caption" sx={{ opacity: 0.45, display: 'block' }}>
                Atualizado às {lastUpdate}
              </Typography>
            )}
            <Typography variant="caption" sx={{ opacity: 0.3, fontSize: '0.65rem' }}>
              Próxima atualização em {countdown}s
            </Typography>
          </Box>

          <IconButton sx={{ color: 'white' }} onClick={handleRefresh} disabled={loading}>
            {loading ? <CircularProgress size={20} sx={{ color: 'white' }} /> : <RefreshIcon />}
          </IconButton>

          <Box component="button" onClick={onTrocar} sx={{
            px: 2, py: 0.75, borderRadius: 1.5, border: '1px solid rgba(255,255,255,0.2)',
            bgcolor: 'transparent', color: 'rgba(255,255,255,0.6)', cursor: 'pointer',
            fontSize: '0.8rem', fontWeight: 600, transition: '0.2s',
            '&:hover': { bgcolor: 'rgba(255,255,255,0.08)' },
          }}>
            Trocar
          </Box>
        </Box>
      </Paper>

      {erro && <Alert severity="warning" sx={{ mb: 2, borderRadius: 2 }}>{erro}</Alert>}

      {loading && !ranking && (
        <Box sx={{ textAlign: 'center', py: 8 }}><CircularProgress /></Box>
      )}

      {ranking && (
        <>
          {ranking.classificacao.length === 0 ? (
            <Alert severity="info" sx={{ borderRadius: 2 }}>
              Nenhum resultado registrado ainda. Os tempos aparecem aqui assim que corridas forem finalizadas.
            </Alert>
          ) : (
            <TableContainer component={Paper} elevation={0}
              sx={{ borderRadius: 2, border: '1px solid #E0E0E6', overflowX: 'auto' }}>
              <Table sx={{ minWidth: 500 }} size="small">
                <TableHead>
                  <TableRow sx={{ bgcolor: '#16213E' }}>
                    <TableCell sx={{ color: 'white', fontWeight: 800, width: 52, textAlign: 'center' }}>POS</TableCell>
                    <TableCell sx={{ color: 'white', fontWeight: 800 }}>EQUIPE</TableCell>
                    <TableCell sx={{ color: 'white', fontWeight: 800, display: { xs: 'none', sm: 'table-cell' } }}>CURSO</TableCell>
                    <TableCell sx={{ color: '#F5A623', fontWeight: 800, whiteSpace: 'nowrap', textAlign: 'center' }}>MELHOR TEMPO</TableCell>
                    <TableCell sx={{ color: '#90CAF9', fontWeight: 800, whiteSpace: 'nowrap', textAlign: 'center' }}>ÚLTIMO TEMPO</TableCell>

                    {/* Colunas dinâmicas — uma por bateria com resultados */}
                    {baterias.map(b => (
                      <TableCell key={b.bateriaId} sx={{ color: 'rgba(255,255,255,0.7)', fontWeight: 800, textAlign: 'center', whiteSpace: 'nowrap' }}>
                        {bateriaLabel(b)}
                      </TableCell>
                    ))}
                  </TableRow>
                </TableHead>

                <TableBody>
                  {ranking.classificacao.map(row => (
                    <TableRow key={row.equipeId} sx={{
                      '&:nth-of-type(odd)':  { bgcolor: '#FFFFFF' },
                      '&:nth-of-type(even)': { bgcolor: '#F9F9FB' },
                      '&:hover': { bgcolor: '#FCE4EC' },
                      transition: '0.2s',
                    }}>
                      <TableCell sx={{ textAlign: 'center' }}>
                        <Typography sx={{ fontWeight: 900, fontSize: '1.1rem', color: posColor(row.posicao) }}>
                          {row.posicao}º
                        </Typography>
                      </TableCell>

                      <TableCell>
                        <Typography sx={{ fontWeight: 800, color: '#1A1A2E' }}>{row.equipeNome}</Typography>
                      </TableCell>

                      <TableCell sx={{ display: { xs: 'none', sm: 'table-cell' } }}>
                        <Typography variant="body2" sx={{ color: '#9A9AAF', fontSize: '0.75rem' }}>
                          {row.equipeCurso}
                        </Typography>
                      </TableCell>

                      {/* Melhor tempo geral */}
                      <TableCell sx={{ textAlign: 'center' }}>
                        <Typography sx={{ fontWeight: 900, fontFamily: 'monospace', color: '#AD1457', fontSize: '0.95rem' }}>
                          {formatMs(row.melhorTempo)}
                        </Typography>
                      </TableCell>

                      {/* Último tempo (tempo da corrida mais recente) */}
                      <TableCell sx={{ textAlign: 'center' }}>
                        <Typography sx={{ fontWeight: 700, fontFamily: 'monospace', color: '#1565C0', fontSize: '0.9rem' }}>
                          {formatMs(row.tempoUltimaDescida)}
                        </Typography>
                      </TableCell>

                      {/* Tempo por bateria — vazio se a equipe não participou */}
                      {baterias.map(b => {
                        const item = row.porBateria.find(p => p.bateriaId === b.bateriaId);
                        return (
                          <TableCell key={b.bateriaId} sx={{ textAlign: 'center' }}>
                            {item ? (
                              <Box>
                                <Typography sx={{ fontFamily: 'monospace', fontWeight: 700, fontSize: '0.85rem', color: '#1A1A2E' }}>
                                  {formatMs(item.melhorTempo)}
                                </Typography>
                                {item.totalDescidas > 1 && (
                                  <Typography variant="caption" sx={{ color: '#9A9AAF', fontSize: '0.65rem' }}>
                                    {item.totalDescidas} descidas
                                  </Typography>
                                )}
                              </Box>
                            ) : (
                              <Typography sx={{ color: '#D0D0D8', fontSize: '0.8rem' }}>—</Typography>
                            )}
                          </TableCell>
                        );
                      })}
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}

          <Box sx={{ mt: 2, display: 'flex', justifyContent: 'space-between', px: 1 }}>
            <Typography variant="caption" sx={{ color: '#9A9AAF' }}>
              START IoT · Classificação pelo menor tempo entre todas as descidas finalizadas
            </Typography>
            <Typography variant="caption" sx={{ fontWeight: 700, color: '#1A1A2E' }}>
              FATEC MOGI MIRIM
            </Typography>
          </Box>
        </>
      )}
    </Box>
  );
};

// ─── PÁGINA PRINCIPAL ────────────────────────────────────────────────────────

type Phase = 'init' | 'select-manual' | 'showing';
interface Contexto { edicaoId: number; label: string; }

const RankingPage: React.FC = () => {
  const [phase, setPhase]       = useState<Phase>('init');
  const [eventos, setEventos]   = useState<EventoResponse[]>([]);
  const [contexto, setContexto] = useState<Contexto | null>(null);

  useEffect(() => {
    const detectar = async () => {
      try {
        const evs = await listarEventos();
        setEventos(evs);

        for (const ev of evs) {
          const edicoes = await listarEdicoesPorEvento(ev.id);
          const ativa   = edicoes.find(e => e.status === 'EM_ANDAMENTO');
          if (ativa) {
            setContexto({ edicaoId: ativa.id, label: `${ev.nome} · ${ativa.ano}` });
            setPhase('showing');
            return;
          }
        }

        setPhase('select-manual');
      } catch {
        setPhase('select-manual');
      }
    };
    detectar();
  }, []);

  if (phase === 'init') {
    return (
      <Box sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: '#F4F4F6' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (phase === 'select-manual') {
    return (
      <SeletorManual
        eventos={eventos}
        onConfirmar={(edicaoId, label) => { setContexto({ edicaoId, label }); setPhase('showing'); }}
      />
    );
  }

  if (phase === 'showing' && contexto) {
    return (
      <RankingTable
        key={contexto.edicaoId}
        edicaoId={contexto.edicaoId}
        label={contexto.label}
        onTrocar={() => setPhase('select-manual')}
      />
    );
  }

  return null;
};

export { RankingPage };
