import React, { useCallback, useEffect, useState } from 'react';
import {
  Alert, Box, Button, Chip, CircularProgress, Dialog, DialogActions,
  DialogContent, DialogTitle, Divider, MenuItem, Paper, Stack,
  TextField, Typography,
} from '@mui/material';
import CalendarMonthOutlinedIcon from '@mui/icons-material/CalendarMonthOutlined';
import AddOutlinedIcon from '@mui/icons-material/AddOutlined';
import BoltOutlinedIcon from '@mui/icons-material/BoltOutlined';
import GroupOutlinedIcon from '@mui/icons-material/GroupOutlined';
import DownloadOutlinedIcon from '@mui/icons-material/DownloadOutlined';
import { listarEventos, type EventoResponse } from '../../services/eventos';
import {
  listarEdicoesPorEvento, criarEdicao, atualizarStatusEdicao,
  type EdicaoResponse, type StatusEdicao,
} from '../../services/edicoes';
import { calcularRanking } from '../../services/ranking';

const STATUS_CHIP: Record<StatusEdicao, { label: string; color: 'default' | 'info' | 'warning' | 'success' | 'error' }> = {
  PLANEJADA:    { label: 'Planejada',    color: 'info'    },
  EM_ANDAMENTO: { label: 'Em andamento', color: 'warning' },
  FINALIZADA:   { label: 'Finalizada',   color: 'success' },
  CANCELADA:    { label: 'Cancelada',    color: 'error'   },
};

const TRANSICOES: Record<StatusEdicao, { label: string; status: StatusEdicao; color: string }[]> = {
  PLANEJADA:    [
    { label: 'Iniciar',   status: 'EM_ANDAMENTO', color: '#2E7D32' },
    { label: 'Cancelar',  status: 'CANCELADA',    color: '#C8102E' },
  ],
  EM_ANDAMENTO: [
    { label: 'Cancelar',  status: 'CANCELADA',    color: '#C8102E' },
  ],
  FINALIZADA:   [],
  CANCELADA:    [],
};

const ANO_ATUAL = new Date().getFullYear();
const ANOS = Array.from({ length: 6 }, (_, i) => ANO_ATUAL - 2 + i);

const EMPTY_FORM = { ano: String(ANO_ATUAL), dataEvento: '', status: 'PLANEJADA' as StatusEdicao };

const formatDate = (iso?: string) => {
  if (!iso) return null;
  const [y, m, d] = iso.split('-');
  return `${d}/${m}/${y}`;
};

const formatMs = (ms: number) => {
  const min  = Math.floor(ms / 60000);
  const sec  = Math.floor((ms % 60000) / 1000);
  const cent = Math.floor((ms % 1000) / 10);
  return `${String(min).padStart(2, '0')}:${String(sec).padStart(2, '0')}.${String(cent).padStart(2, '0')}`;
};

const escapeCsv = (v: string | number) => {
  const s = String(v);
  return s.includes(',') || s.includes('"') || s.includes('\n') ? `"${s.replace(/"/g, '""')}"` : s;
};

const gerarCsv = async (edicao: EdicaoResponse): Promise<void> => {
  const ranking = await calcularRanking(edicao.id);
  const baterias = ranking.baterias;

  const cabecalho = [
    'Pos', 'Equipe', 'Curso',
    'Melhor Tempo', 'Último Tempo', 'Total Descidas',
    ...baterias.map(b => b.tipo ? `Bat.${b.numero} - ${b.tipo}` : `Bat.${b.numero}`),
  ].map(escapeCsv).join(',');

  const linhas = ranking.classificacao.map(item => {
    const temposPorBateria = baterias.map(b => {
      const encontrado = item.porBateria.find(pb => pb.bateriaId === b.bateriaId);
      return encontrado ? escapeCsv(formatMs(encontrado.melhorTempo)) : '-';
    });
    return [
      item.posicao,
      escapeCsv(item.equipeNome),
      escapeCsv(item.equipeCurso),
      escapeCsv(formatMs(item.ultimoTempo)),
      escapeCsv(formatMs(item.tempoUltimaDescida)),
      item.totalDescidas,
      ...temposPorBateria,
    ].join(',');
  });

  const csv = [cabecalho, ...linhas].join('\n');
  const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `ranking_${ranking.nomeEvento.replace(/\s+/g, '_')}_${edicao.ano}.csv`;
  link.click();
  URL.revokeObjectURL(url);
};

const EdicoesPage: React.FC = () => {
  const [eventos, setEventos] = useState<EventoResponse[]>([]);
  const [eventoId, setEventoId] = useState<number | ''>('');
  const [edicoes, setEdicoes] = useState<EdicaoResponse[]>([]);
  const [loadingInicial, setLoadingInicial] = useState(true);
  const [loadingEdicoes, setLoadingEdicoes] = useState(false);
  const [dialogAberto, setDialogAberto] = useState(false);
  const [confirmandoFinalizar, setConfirmandoFinalizar] = useState<EdicaoResponse | null>(null);
  const [exportando, setExportando] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);
  const [atualizando, setAtualizando] = useState<number | null>(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    listarEventos()
      .then(evs => {
        setEventos(evs);
        if (evs.length === 1) setEventoId(evs[0].id);
      })
      .catch(() => {})
      .finally(() => setLoadingInicial(false));
  }, []);

  const reloadEdicoes = useCallback(() => {
    if (!eventoId) return;
    setLoadingEdicoes(true);
    listarEdicoesPorEvento(eventoId as number)
      .then(eds => setEdicoes(eds.sort((a, b) => b.ano - a.ano)))
      .catch(() => {})
      .finally(() => setLoadingEdicoes(false));
  }, [eventoId]);

  useEffect(() => { reloadEdicoes(); }, [reloadEdicoes]);

  const handleCriar = async () => {
    if (!eventoId) return;
    setError('');
    setSaving(true);
    try {
      await criarEdicao({
        eventoId: eventoId as number,
        ano: Number(form.ano),
        dataEvento: form.dataEvento || undefined,
        status: form.status,
      });
      setSuccess(`Edição ${form.ano} criada com sucesso.`);
      setDialogAberto(false);
      setForm(EMPTY_FORM);
      reloadEdicoes();
    } catch (err: any) {
      setError(err?.response?.data?.message ?? 'Erro ao criar edição.');
    } finally {
      setSaving(false);
    }
  };

  const handleMudarStatus = async (edicaoId: number, novoStatus: StatusEdicao) => {
    setAtualizando(edicaoId);
    try {
      await atualizarStatusEdicao(edicaoId, novoStatus);
      reloadEdicoes();
    } catch {} finally {
      setAtualizando(null);
    }
  };

  const handleConfirmarFinalizar = async (exportarCsv: boolean) => {
    if (!confirmandoFinalizar) return;
    const ed = confirmandoFinalizar;
    setConfirmandoFinalizar(null);
    if (exportarCsv) {
      setExportando(true);
      try { await gerarCsv(ed); } catch {} finally { setExportando(false); }
    }
    await handleMudarStatus(ed.id, 'FINALIZADA');
  };

  const abrirDialog = () => {
    setError('');
    setForm(EMPTY_FORM);
    setDialogAberto(true);
  };

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#F4F4F6', p: { xs: 2, md: 4 } }}>

      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 4 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <CalendarMonthOutlinedIcon sx={{ color: '#00838F', fontSize: 28 }} />
          <Box>
            <Typography variant="h5" sx={{ fontWeight: 900, color: '#1A1A2E', lineHeight: 1 }}>
              Edições
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Gerenciamento de edições e ciclo de vida do evento
            </Typography>
          </Box>
        </Box>
        {eventoId !== '' && (
          <Button variant="contained" startIcon={<AddOutlinedIcon />} onClick={abrirDialog}
            sx={{ bgcolor: '#00838F', '&:hover': { bgcolor: '#006064' }, fontWeight: 700 }}>
            Nova edição
          </Button>
        )}
      </Box>

      {success && (
        <Alert severity="success" sx={{ mb: 2, borderRadius: 2 }} onClose={() => setSuccess('')}>
          {success}
        </Alert>
      )}

      <Paper elevation={0} sx={{ borderRadius: 3, border: '1px solid #E0E0E6', p: 2.5, mb: 3 }}>
        {loadingInicial ? (
          <CircularProgress size={20} />
        ) : (
          <TextField select fullWidth label="Evento" value={eventoId} size="small"
            onChange={e => setEventoId(Number(e.target.value))}>
            {eventos.length === 0
              ? <MenuItem value="" disabled>Nenhum evento cadastrado</MenuItem>
              : eventos.map(ev => <MenuItem key={ev.id} value={ev.id}>{ev.nome}</MenuItem>)
            }
          </TextField>
        )}
      </Paper>

      {eventoId === '' ? (
        <Alert severity="info" sx={{ borderRadius: 2 }}>
          Selecione um evento para visualizar e gerenciar suas edições.
        </Alert>
      ) : loadingEdicoes ? (
        <Box sx={{ textAlign: 'center', py: 6 }}><CircularProgress /></Box>
      ) : edicoes.length === 0 ? (
        <Alert severity="info" sx={{ borderRadius: 2 }}>
          Nenhuma edição cadastrada para este evento.
        </Alert>
      ) : (
        <Stack spacing={2}>
          {edicoes.map(ed => {
            const chip = STATUS_CHIP[ed.status];
            const transicoes = TRANSICOES[ed.status] ?? [];
            const isUpdating = atualizando === ed.id;

            return (
              <Paper key={ed.id} elevation={0} sx={{ borderRadius: 3, border: '1px solid #E0E0E6', overflow: 'hidden' }}>
                <Box sx={{
                  bgcolor: '#1A1A2E', px: 3, py: 1.5,
                  display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                  flexWrap: 'wrap', gap: 1,
                }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                    <CalendarMonthOutlinedIcon sx={{ color: '#80DEEA', fontSize: 18 }} />
                    <Typography sx={{ fontWeight: 800, color: 'white', fontSize: 15 }}>
                      {ed.eventoNome} — {ed.ano}
                    </Typography>
                    <Chip label={chip.label} size="small" color={chip.color} />
                  </Box>

                  {(transicoes.length > 0 || ed.status === 'EM_ANDAMENTO') && (
                    <Stack direction="row" spacing={1}>
                      {ed.status === 'EM_ANDAMENTO' && (
                        <Button size="small" variant="outlined"
                          disabled={isUpdating || exportando}
                          onClick={() => setConfirmandoFinalizar(ed)}
                          sx={{ fontSize: 11, fontWeight: 700, borderColor: '#1565C0', color: '#1565C0',
                            '&:hover': { bgcolor: '#1565C018', borderColor: '#1565C0' } }}>
                          Finalizar
                        </Button>
                      )}
                      {transicoes.map(t => (
                        <Button key={t.status} size="small" variant="outlined"
                          disabled={isUpdating}
                          onClick={() => handleMudarStatus(ed.id, t.status)}
                          sx={{
                            fontSize: 11, fontWeight: 700,
                            borderColor: t.color, color: t.color,
                            '&:hover': { bgcolor: t.color + '18', borderColor: t.color },
                          }}>
                          {isUpdating ? <CircularProgress size={12} /> : t.label}
                        </Button>
                      ))}
                    </Stack>
                  )}
                </Box>

                <Stack direction={{ xs: 'column', sm: 'row' }} spacing={3}
                  sx={{ px: 3, py: 2, alignItems: { sm: 'center' } }}>
                  {ed.dataEvento && (
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
                      <CalendarMonthOutlinedIcon sx={{ fontSize: 16, color: '#00838F' }} />
                      <Typography variant="body2" color="text.secondary">
                        {formatDate(ed.dataEvento)}
                      </Typography>
                    </Box>
                  )}
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
                    <GroupOutlinedIcon sx={{ fontSize: 16, color: '#7B1FA2' }} />
                    <Typography variant="body2" color="text.secondary">
                      {ed.totalEquipes} equipe(s) inscritas
                    </Typography>
                  </Box>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
                    <BoltOutlinedIcon sx={{ fontSize: 16, color: '#F57C00' }} />
                    <Typography variant="body2" color="text.secondary">
                      {ed.totalBaterias} bateria(s)
                    </Typography>
                  </Box>
                </Stack>
              </Paper>
            );
          })}
        </Stack>
      )}

      {/* Dialog de confirmação de finalização */}
      <Dialog open={!!confirmandoFinalizar} onClose={() => setConfirmandoFinalizar(null)} maxWidth="xs" fullWidth>
        <DialogTitle sx={{ fontWeight: 800 }}>
          Finalizar edição {confirmandoFinalizar?.ano}
        </DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
            Deseja exportar o ranking em CSV antes de finalizar?
          </Typography>
          <Typography variant="caption" color="text.disabled">
            O CSV inclui posição, equipe, curso, tempos e resultados por bateria.
          </Typography>
        </DialogContent>
        <Divider />
        <DialogActions sx={{ px: 2, py: 1.5, gap: 1, flexDirection: { xs: 'column', sm: 'row' } }}>
          <Button onClick={() => setConfirmandoFinalizar(null)} sx={{ minWidth: 90 }}>
            Cancelar
          </Button>
          <Button variant="outlined" startIcon={<DownloadOutlinedIcon />}
            onClick={() => handleConfirmarFinalizar(true)}
            sx={{ minWidth: 180, borderColor: '#1565C0', color: '#1565C0',
              '&:hover': { bgcolor: '#1565C018', borderColor: '#1565C0' }, fontWeight: 700 }}>
            Exportar CSV e Finalizar
          </Button>
          <Button variant="contained" onClick={() => handleConfirmarFinalizar(false)}
            sx={{ minWidth: 90, bgcolor: '#1565C0', '&:hover': { bgcolor: '#0d47a1' }, fontWeight: 700 }}>
            Só Finalizar
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogAberto} onClose={() => !saving && setDialogAberto(false)} maxWidth="xs" fullWidth>
        <DialogTitle sx={{ fontWeight: 800 }}>Nova edição</DialogTitle>
        <DialogContent>
          <Stack spacing={2.5} sx={{ pt: 1 }}>
            <TextField select fullWidth label="Ano" size="small" value={form.ano}
              onChange={e => setForm(f => ({ ...f, ano: e.target.value }))}
              disabled={saving}>
              {ANOS.map(a => <MenuItem key={a} value={String(a)}>{a}</MenuItem>)}
            </TextField>

            <TextField fullWidth label="Data do evento" type="date" size="small"
              value={form.dataEvento}
              onChange={e => setForm(f => ({ ...f, dataEvento: e.target.value }))}
              slotProps={{ inputLabel: { shrink: true } }} disabled={saving}
              helperText="Opcional — dia em que o evento ocorrerá" />

            <TextField select fullWidth label="Status inicial" size="small" value={form.status}
              onChange={e => setForm(f => ({ ...f, status: e.target.value as StatusEdicao }))}
              disabled={saving}>
              {(['PLANEJADA', 'EM_ANDAMENTO'] as StatusEdicao[]).map(s => (
                <MenuItem key={s} value={s}>{STATUS_CHIP[s].label}</MenuItem>
              ))}
            </TextField>

            {error && <Alert severity="error" sx={{ borderRadius: 2 }}>{error}</Alert>}
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setDialogAberto(false)} disabled={saving}>Cancelar</Button>
          <Button variant="contained" onClick={handleCriar} disabled={saving}
            sx={{ bgcolor: '#00838F', '&:hover': { bgcolor: '#006064' }, fontWeight: 700 }}>
            {saving ? <CircularProgress size={18} sx={{ color: 'white' }} /> : 'Criar edição'}
          </Button>
        </DialogActions>
      </Dialog>

    </Box>
  );
};

export { EdicoesPage };
