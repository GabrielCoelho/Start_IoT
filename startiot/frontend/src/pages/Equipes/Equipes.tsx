
import React, { useCallback, useEffect, useRef, useState } from 'react';
import {
  Alert, Box, Button, Chip, CircularProgress, Divider, MenuItem,
  Paper, Stack, Table, TableBody, TableCell, TableContainer, TableHead,
  TableRow, Tabs, Tab, TextField, Tooltip, Typography,
} from '@mui/material';
import GroupsOutlinedIcon from '@mui/icons-material/GroupsOutlined';
import UploadFileOutlinedIcon from '@mui/icons-material/UploadFileOutlined';
import DownloadOutlinedIcon from '@mui/icons-material/DownloadOutlined';
import CheckCircleOutlinedIcon from '@mui/icons-material/CheckCircleOutlined';
import CancelOutlinedIcon from '@mui/icons-material/CancelOutlined';
import { listarEventos, type EventoResponse } from '../../services/eventos';
import { listarEdicoesPorEvento, type EdicaoResponse } from '../../services/edicoes';
import {
  inscreverEquipe, listarEquipesPorEdicao, aprovarEquipe, reprovarEquipe,
  type EquipeResponse,
} from '../../services/equipes';

// ─── CSV ────────────────────────────────────────────────────────────────────

const CSV_COLS = ['nome', 'curso', 'categoria'] as const;
const CSV_REQUIRED = ['nome'] as const;

type CsvStatus = 'pendente' | 'enviando' | 'ok' | 'erro';

interface CsvRow {
  nome: string;
  curso: string;
  categoria: string;
  status: CsvStatus;
  erro?: string;
}

function parseCSV(text: string): { rows: CsvRow[]; warnings: string[] } {
  const lines = text.trim().split(/\r?\n/);
  const warnings: string[] = [];

  if (lines.length < 2) {
    return { rows: [], warnings: ['O arquivo deve ter ao menos uma linha de dados além do cabeçalho.'] };
  }

  const headers = lines[0].split(',').map((h) => h.trim().toLowerCase());

  const missing = CSV_REQUIRED.filter((c) => !headers.includes(c));
  if (missing.length > 0) {
    return { rows: [], warnings: [`Coluna obrigatória ausente: ${missing.join(', ')}. Verifique o modelo.`] };
  }

  const unknown = headers.filter((h) => !(CSV_COLS as readonly string[]).includes(h));
  if (unknown.length > 0) {
    warnings.push(`Colunas desconhecidas e ignoradas: ${unknown.join(', ')}.`);
  }

  const idx = (col: string) => headers.indexOf(col);

  const rows: CsvRow[] = lines.slice(1).map((line) => {
    const cells = line.split(',').map((c) => c.trim());
    return {
      nome: idx('nome') >= 0 ? cells[idx('nome')] ?? '' : '',
      curso: idx('curso') >= 0 ? cells[idx('curso')] ?? '' : '',
      categoria: idx('categoria') >= 0 ? cells[idx('categoria')] ?? '' : '',
      status: 'pendente',
    };
  }).filter((r) => r.nome !== '');

  if (rows.length === 0) {
    warnings.push('Nenhuma linha válida encontrada. Verifique se a coluna "nome" está preenchida.');
  }

  return { rows, warnings };
}

function downloadTemplate() {
  const content = 'nome,curso,categoria\nNome da Equipe,ADS,Iniciante';
  const blob = new Blob([content], { type: 'text/csv' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'modelo_equipes.csv';
  a.click();
  URL.revokeObjectURL(url);
}

// ─── STATUS CHIP ────────────────────────────────────────────────────────────

const STATUS_COLORS: Record<string, 'warning' | 'success' | 'error' | 'default'> = {
  PENDENTE: 'warning', APROVADA: 'success', REPROVADA: 'error', CANCELADA: 'default',
};
const STATUS_LABELS: Record<string, string> = {
  PENDENTE: 'Pendente', APROVADA: 'Aprovada', REPROVADA: 'Reprovada', CANCELADA: 'Cancelada',
};

// ─── SELETOR DE CONTEXTO ────────────────────────────────────────────────────

interface ContextSelectorProps {
  edicaoId: number | null;
  onEdicaoChange: (id: number) => void;
}

const ContextSelector: React.FC<ContextSelectorProps> = ({ edicaoId, onEdicaoChange }) => {
  const [eventos, setEventos] = useState<EventoResponse[]>([]);
  const [edicoes, setEdicoes] = useState<EdicaoResponse[]>([]);
  const [eventoId, setEventoId] = useState<number | ''>('');

  // Carga inicial com auto-detecção de edição EM_ANDAMENTO
  useEffect(() => {
    listarEventos().then(async (evs) => {
      setEventos(evs);

      for (const ev of evs) {
        try {
          const eds = await listarEdicoesPorEvento(ev.id);
          const ativa = eds.find((e) => e.status === 'EM_ANDAMENTO');
          if (ativa) {
            setEventoId(ev.id);
            setEdicoes(eds);
            onEdicaoChange(ativa.id);
            return;
          }
        } catch {}
      }

      // Nenhuma edição ativa — auto-seleciona o evento se houver apenas um
      if (evs.length === 1) {
        setEventoId(evs[0].id);
        listarEdicoesPorEvento(evs[0].id).then(setEdicoes).catch(() => {});
      }
    }).catch(() => {});
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleEventoChange = (id: number) => {
    setEventoId(id);
    setEdicoes([]);
    onEdicaoChange(0 as any);
    listarEdicoesPorEvento(id).then(setEdicoes).catch(() => {});
  };

  return (
    <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mb: 3 }}>
      <TextField select fullWidth label="Evento" value={eventoId}
        onChange={(e) => handleEventoChange(Number(e.target.value))}
        size="small"
      >
        {eventos.map((ev) => <MenuItem key={ev.id} value={ev.id}>{ev.nome}</MenuItem>)}
      </TextField>

      <TextField select fullWidth label="Edição" value={edicaoId ?? ''} size="small"
        disabled={edicoes.length === 0}
        onChange={(e) => onEdicaoChange(Number(e.target.value))}
      >
        {edicoes.map((ed) => (
          <MenuItem key={ed.id} value={ed.id}>{ed.ano} — {ed.status}</MenuItem>
        ))}
      </TextField>
    </Stack>
  );
};

// ─── ABA FORMULÁRIO ─────────────────────────────────────────────────────────

interface FormTabProps { edicaoId: number; onCadastro: () => void; }

const FormTab: React.FC<FormTabProps> = ({ edicaoId, onCadastro }) => {
  const EMPTY = { nome: '', curso: '', categoria: '' };
  const [form, setForm] = useState(EMPTY);
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  const set = (f: keyof typeof EMPTY) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm((p) => ({ ...p, [f]: e.target.value }));

  const handleSubmit = async () => {
    setError(''); setSuccess(''); setLoading(true);
    try {
      const eq = await inscreverEquipe({ edicaoId, ...form });
      setSuccess(`Equipe "${eq.nome}" cadastrada com sucesso.`);
      setForm(EMPTY);
      onCadastro();
    } catch (err: any) {
      setError(err?.response?.data?.message ?? 'Erro ao cadastrar equipe.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Stack spacing={2.5}>
      <TextField fullWidth label="Nome da equipe" value={form.nome}
        onChange={set('nome')} disabled={loading} inputProps={{ maxLength: 100 }} />
      <TextField fullWidth label="Curso" value={form.curso}
        onChange={set('curso')} disabled={loading} inputProps={{ maxLength: 100 }} />
      <TextField fullWidth label="Categoria" value={form.categoria}
        onChange={set('categoria')} disabled={loading} inputProps={{ maxLength: 50 }} />

      {success && <Alert severity="success" icon={<CheckCircleOutlinedIcon />} sx={{ borderRadius: 2 }}>{success}</Alert>}
      {error   && <Alert severity="error" sx={{ borderRadius: 2 }}>{error}</Alert>}

      <Button variant="contained" fullWidth onClick={handleSubmit}
        disabled={!form.nome.trim() || loading}
        sx={{ py: 1.5, bgcolor: '#2E7D32', '&:hover': { bgcolor: '#1B5E20' }, fontWeight: 700 }}
      >
        {loading ? <CircularProgress size={22} sx={{ color: 'white' }} /> : 'Cadastrar equipe'}
      </Button>
    </Stack>
  );
};

// ─── ABA CSV ─────────────────────────────────────────────────────────────────

interface CsvTabProps { edicaoId: number; onCadastro: () => void; }

const CsvTab: React.FC<CsvTabProps> = ({ edicaoId, onCadastro }) => {
  const inputRef = useRef<HTMLInputElement>(null);
  const [rows, setRows] = useState<CsvRow[]>([]);
  const [warnings, setWarnings] = useState<string[]>([]);
  const [importing, setImporting] = useState(false);

  const handleFile = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    if (!file.name.endsWith('.csv')) {
      setWarnings(['O arquivo selecionado não é um CSV.']); setRows([]); return;
    }
    const reader = new FileReader();
    reader.onload = (ev) => {
      const { rows: parsed, warnings: warns } = parseCSV(ev.target?.result as string);
      setRows(parsed); setWarnings(warns);
    };
    reader.readAsText(file);
    e.target.value = '';
  };

  const handleImport = useCallback(async () => {
    setImporting(true);
    for (let i = 0; i < rows.length; i++) {
      if (rows[i].status === 'ok') continue;
      setRows((prev) => prev.map((r, idx) => idx === i ? { ...r, status: 'enviando' } : r));
      try {
        await inscreverEquipe({ edicaoId, nome: rows[i].nome, curso: rows[i].curso, categoria: rows[i].categoria });
        setRows((prev) => prev.map((r, idx) => idx === i ? { ...r, status: 'ok' } : r));
        onCadastro();
      } catch (err: any) {
        const msg = err?.response?.data?.message ?? 'Erro ao cadastrar.';
        setRows((prev) => prev.map((r, idx) => idx === i ? { ...r, status: 'erro', erro: msg } : r));
      }
    }
    setImporting(false);
  }, [rows, edicaoId, onCadastro]);

  const pendentes = rows.filter((r) => r.status === 'pendente' || r.status === 'erro').length;

  const rowColor = (s: CsvStatus) =>
    ({ pendente: 'inherit', enviando: '#FFF8E1', ok: '#F1F8E9', erro: '#FFEBEE' })[s];

  const rowChip = (row: CsvRow) => {
    if (row.status === 'pendente')  return <Chip label="Pendente"  size="small" color="default" />;
    if (row.status === 'enviando') return <Chip label="Enviando…" size="small" color="warning" />;
    if (row.status === 'ok')       return <Chip label="Cadastrada" size="small" color="success" />;
    return (
      <Tooltip title={row.erro ?? 'Erro'}>
        <Chip label="Erro" size="small" color="error" />
      </Tooltip>
    );
  };

  return (
    <Stack spacing={2}>
      <Stack direction="row" spacing={1.5}>
        <Button variant="outlined" startIcon={<DownloadOutlinedIcon />}
          onClick={downloadTemplate} size="small"
          sx={{ borderColor: '#2E7D32', color: '#2E7D32', '&:hover': { borderColor: '#1B5E20' } }}
        >
          Baixar modelo
        </Button>
        <Button variant="outlined" startIcon={<UploadFileOutlinedIcon />}
          onClick={() => inputRef.current?.click()} size="small"
          disabled={importing}
          sx={{ borderColor: '#1565C0', color: '#1565C0' }}
        >
          Selecionar CSV
        </Button>
        <input ref={inputRef} type="file" accept=".csv" hidden onChange={handleFile} />
      </Stack>

      {warnings.map((w, i) => (
        <Alert key={i} severity="warning" sx={{ borderRadius: 2 }}>{w}</Alert>
      ))}

      {rows.length > 0 && (
        <>
          <TableContainer component={Paper} elevation={0}
            sx={{ border: '1px solid #E0E0E6', borderRadius: 2, maxHeight: 320, overflow: 'auto' }}
          >
            <Table size="small" stickyHeader>
              <TableHead>
                <TableRow sx={{ bgcolor: '#1A1A2E' }}>
                  {['Nome', 'Curso', 'Categoria', 'Status'].map((h) => (
                    <TableCell key={h} sx={{ bgcolor: '#1A1A2E', color: 'white', fontWeight: 700 }}>{h}</TableCell>
                  ))}
                </TableRow>
              </TableHead>
              <TableBody>
                {rows.map((row, i) => (
                  <TableRow key={i} sx={{ bgcolor: rowColor(row.status) }}>
                    <TableCell sx={{ fontWeight: 700 }}>{row.nome}</TableCell>
                    <TableCell>{row.curso || '—'}</TableCell>
                    <TableCell>{row.categoria || '—'}</TableCell>
                    <TableCell>{rowChip(row)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>

          <Button variant="contained" onClick={handleImport}
            disabled={importing || pendentes === 0}
            sx={{ py: 1.5, bgcolor: '#1565C0', '&:hover': { bgcolor: '#0D47A1' }, fontWeight: 700 }}
          >
            {importing
              ? <><CircularProgress size={18} sx={{ color: 'white', mr: 1 }} /> Importando...</>
              : `Importar ${pendentes} equipe${pendentes !== 1 ? 's' : ''}`}
          </Button>
        </>
      )}
    </Stack>
  );
};

// ─── LISTA DE EQUIPES ────────────────────────────────────────────────────────

interface EquipeListProps { edicaoId: number; refreshKey: number; }

const EquipeList: React.FC<EquipeListProps> = ({ edicaoId, refreshKey }) => {
  const [equipes, setEquipes] = useState<EquipeResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [actionId, setActionId] = useState<number | null>(null);

  useEffect(() => {
    setLoading(true);
    listarEquipesPorEdicao(edicaoId)
      .then(setEquipes)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [edicaoId, refreshKey]);

  const handleAction = async (id: number, action: 'aprovar' | 'reprovar') => {
    setActionId(id);
    try {
      const updated = await (action === 'aprovar' ? aprovarEquipe(id) : reprovarEquipe(id));
      setEquipes((prev) => prev.map((e) => e.id === id ? updated : e));
    } catch {} finally {
      setActionId(null);
    }
  };

  if (loading) return <Box sx={{ textAlign: 'center', py: 4 }}><CircularProgress /></Box>;
  if (equipes.length === 0) return (
    <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', py: 4 }}>
      Nenhuma equipe cadastrada para esta edição.
    </Typography>
  );

  return (
    <TableContainer component={Paper} elevation={0}
      sx={{ border: '1px solid #E0E0E6', borderRadius: 2 }}
    >
      <Table size="small">
        <TableHead>
          <TableRow sx={{ bgcolor: '#1A1A2E' }}>
            {['#', 'Nome', 'Curso', 'Categoria', 'Status', 'Ações'].map((h) => (
              <TableCell key={h} sx={{ bgcolor: '#1A1A2E', color: 'white', fontWeight: 700 }}>{h}</TableCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          {equipes.map((eq, i) => (
            <TableRow key={eq.id}
              sx={{ '&:nth-of-type(odd)': { bgcolor: '#FAFAFA' }, '&:hover': { bgcolor: '#F1F8E9' } }}
            >
              <TableCell sx={{ color: '#9A9AAF', fontWeight: 700 }}>{i + 1}</TableCell>
              <TableCell sx={{ fontWeight: 800 }}>{eq.nome}</TableCell>
              <TableCell>{eq.curso || '—'}</TableCell>
              <TableCell>{eq.categoria || '—'}</TableCell>
              <TableCell>
                <Chip label={STATUS_LABELS[eq.statusInscricao]} size="small"
                  color={STATUS_COLORS[eq.statusInscricao]} />
              </TableCell>
              <TableCell>
                <Stack direction="row" spacing={0.5}>
                  {eq.statusInscricao !== 'APROVADA' && (
                    <Tooltip title="Aprovar">
                      <span>
                        <Button size="small" onClick={() => handleAction(eq.id, 'aprovar')}
                          disabled={actionId === eq.id}
                          sx={{ minWidth: 0, color: '#2E7D32' }}
                        >
                          {actionId === eq.id ? <CircularProgress size={14} /> : <CheckCircleOutlinedIcon fontSize="small" />}
                        </Button>
                      </span>
                    </Tooltip>
                  )}
                  {eq.statusInscricao !== 'REPROVADA' && (
                    <Tooltip title="Reprovar">
                      <span>
                        <Button size="small" onClick={() => handleAction(eq.id, 'reprovar')}
                          disabled={actionId === eq.id}
                          sx={{ minWidth: 0, color: '#C8102E' }}
                        >
                          <CancelOutlinedIcon fontSize="small" />
                        </Button>
                      </span>
                    </Tooltip>
                  )}
                </Stack>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

// ─── PÁGINA PRINCIPAL ────────────────────────────────────────────────────────

const EquipesPage: React.FC = () => {
  const [edicaoId, setEdicaoId] = useState<number | null>(null);
  const [tab, setTab] = useState(0);
  const [refreshKey, setRefreshKey] = useState(0);

  const refresh = useCallback(() => setRefreshKey((k) => k + 1), []);

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#F4F4F6', p: { xs: 2, md: 4 } }}>

      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 4 }}>
        <GroupsOutlinedIcon sx={{ color: '#2E7D32', fontSize: 28 }} />
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 900, color: '#1A1A2E', lineHeight: 1 }}>
            Equipes
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Cadastro e gestão de equipes participantes
          </Typography>
        </Box>
      </Box>

      <ContextSelector edicaoId={edicaoId} onEdicaoChange={setEdicaoId} />

      {!edicaoId ? (
        <Alert severity="info" sx={{ borderRadius: 2 }}>
          Selecione um evento e uma edição para gerenciar as equipes.
        </Alert>
      ) : (
        <Stack spacing={3}>
          <Paper elevation={0} sx={{ borderRadius: 3, border: '1px solid #E0E0E6', overflow: 'hidden' }}>
            <Tabs value={tab} onChange={(_, v) => setTab(v)}
              sx={{ bgcolor: '#1A1A2E', '& .MuiTab-root': { color: 'rgba(255,255,255,0.6)', fontWeight: 700 },
                    '& .Mui-selected': { color: 'white' },
                    '& .MuiTabs-indicator': { bgcolor: '#C8102E' } }}
            >
              <Tab label="Formulário" />
              <Tab label="Importar CSV" />
            </Tabs>
            <Divider />
            <Box sx={{ p: 3 }}>
              {tab === 0
                ? <FormTab edicaoId={edicaoId} onCadastro={refresh} />
                : <CsvTab  edicaoId={edicaoId} onCadastro={refresh} />}
            </Box>
          </Paper>

          <Box>
            <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#1A1A2E', mb: 1.5 }}>
              Equipes cadastradas
            </Typography>
            <EquipeList edicaoId={edicaoId} refreshKey={refreshKey} />
          </Box>
        </Stack>
      )}
    </Box>
  );
};

export { EquipesPage };
