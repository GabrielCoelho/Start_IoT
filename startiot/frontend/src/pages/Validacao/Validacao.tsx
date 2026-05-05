import  { useState } from 'react';
import {
  Box,
  Typography,
  Button,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  TextField,
  MenuItem,
  IconButton,
} from '@mui/material';
import { CheckCircleOutlined } from '@mui/icons-material';
import HighlightOffIcon from '@mui/icons-material/HighlightOff';
import HistoryIcon from '@mui/icons-material/History';
// import SearchIcon from '@mui/icons-material/Search';

const ValidacaoPage: any = () => {
  // Master Data de Registros
  const [registros, setRegistros] = useState<any>([
    { id: 1, corrida: 'Corrida 3', num: '12', equipe: 'Turbo Rolimã', tempo: '00:42.310', juiz: 'árbitro01', status: 'valido' },
    { id: 2, corrida: 'Corrida 3', num: '23', equipe: 'Gravity Force', tempo: '00:46.120', juiz: 'árbitro01', status: 'valido' },
    { id: 3, corrida: 'Corrida 4', num: '7', equipe: 'Speed Fatec', tempo: '00:44.890', juiz: 'árbitro02', status: 'pendente' },
    { id: 4, corrida: 'Corrida 4', num: '3', equipe: 'AeroFatec', tempo: '00:47.450', juiz: 'árbitro02', status: 'pendente' },
    { id: 5, corrida: 'Corrida 3', num: '18', equipe: 'Mogi Racing', tempo: '00:38.100', juiz: 'árbitro01', status: 'invalido' },
  ]);

  const [filtroStatus, setFiltroStatus] = useState<any>('todos');

  const alterarStatus: any = (id: any, novoStatus: any) => {
    setRegistros(registros.map((reg: any) => 
      reg.id === id ? { ...reg, status: novoStatus } : reg
    ));
  };

  const stats: any = {
    total: registros.length,
    validados: registros.filter((r: any) => r.status === 'valido').length,
    pendentes: registros.filter((r: any) => r.status === 'pendente').length,
    invalidos: registros.filter((r: any) => r.status === 'invalido').length,
  };

  const filteredData = filtroStatus === 'todos' 
    ? registros 
    : registros.filter((r: any) => r.status === filtroStatus);

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#F4F4F6', p: { xs: 2, md: 4 } }}>
      
      {/* HEADER */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4, flexWrap: 'wrap', gap: 2 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 900, color: '#1A1A2E' }}>Validação de Tempos</Typography>
          <Typography variant="body2" sx={{ color: '#9A9AAF' }}>Revise os registros de telemetria antes do ranking final</Typography>
        </Box>
        <Button variant="contained" sx={{ bgcolor: '#22B573', fontWeight: 800 }}>
          Validar Todos Pendentes
        </Button>
      </Box>

      {/* CARDS DE ESTATÍSTICAS */}
      <Box sx={{ display: 'flex', gap: 2, mb: 4, flexWrap: 'wrap' }}>
        <StatCard label="Validados" value={stats.validados} color="#22B573" />
        <StatCard label="Pendentes" value={stats.pendentes} color="#F5A623" />
        <StatCard label="Inválidos" value={stats.invalidos} color="#C8102E" />
        <StatCard label="Total" value={stats.total} color="#1A1A2E" />
      </Box>

      {/* BARRA DE FILTROS */}
      <Paper elevation={0} sx={{ p: 2, mb: 2, borderRadius: 2, display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
        <TextField
          select
          size="small"
          label="Filtrar por Status"
          value={filtroStatus}
          onChange={(e: any) => setFiltroStatus(e.target.value)}
          sx={{ minWidth: 200 }}
        >
          <MenuItem value="todos">Todos os Registros</MenuItem>
          <MenuItem value="pendente">Pendentes</MenuItem>
          <MenuItem value="valido">Validados</MenuItem>
          <MenuItem value="invalido">Invalidados</MenuItem>
        </TextField>
        <TextField 
          size="small" 
          placeholder="Buscar equipe..." 
          sx={{ flexGrow: 1 }}
        //   slotProps={{ startAdornment: <SearchIcon sx={{ color: 'gray', mr: 1 }} /> }}
        />
      </Paper>

      {/* TABELA DE AUDITORIA */}
      <TableContainer component={Paper} elevation={0} sx={{ borderRadius: 2, border: '1px solid #E0E0E6' }}>
        <Table size="small">
          <TableHead sx={{ bgcolor: '#1A1A2E' }}>
            <TableRow>
              <TableCell sx={{ color: 'white', fontWeight: 700 }}>Corrida</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 700 }}>Equipe</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 700 }}>Tempo</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 700 }}>Juiz/Origem</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 700 }}>Status</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 700, textAlign: 'right' }}>Ações</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredData.map((reg: any) => (
              <TableRow key={reg.id} sx={{ '&:hover': { bgcolor: '#F9F9FB' } }}>
                <TableCell sx={{ fontWeight: 600 }}>{reg.corrida}</TableCell>
                <TableCell>
                  <Typography sx={{ fontWeight: 800, fontSize: '0.9rem' }}>#{reg.num} {reg.equipe}</Typography>
                </TableCell>
                <TableCell sx={{ fontFamily: 'monospace', fontWeight: 900, fontSize: '1rem' }}>{reg.tempo}</TableCell>
                <TableCell sx={{ color: '#9A9AAF', fontSize: '0.8rem' }}>{reg.juiz}</TableCell>
                <TableCell>
                  <StatusChip status={reg.status} />
                </TableCell>
                <TableCell sx={{ textAlign: 'right' }}>
                  <Stack direction="row" spacing={1} sx={{justifyContent:"flex-end"}}>
                    {reg.status !== 'valido' && (
                      <IconButton color="success" size="small" onClick={() => alterarStatus(reg.id, 'valido')}>
                        <CheckCircleOutlined />
                      </IconButton>
                    )}
                    {reg.status !== 'invalido' && (
                      <IconButton color="error" size="small" onClick={() => alterarStatus(reg.id, 'invalido')}>
                        <HighlightOffIcon />
                      </IconButton>
                    )}
                    <IconButton size="small">
                      <HistoryIcon />
                    </IconButton>
                  </Stack>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};

// Subcomponentes auxiliares com tipagem any
const StatCard: any = ({ label, value, color }: any) => (
  <Paper elevation={0} sx={{ p: 2, flex: 1, minWidth: 120, textAlign: 'center', borderRadius: 3, border: '1px solid #E0E0E6' }}>
    <Typography sx={{ fontSize: '1.8rem', fontWeight: 900, color: color }}>{value}</Typography>
    <Typography variant="caption" sx={{ color: '#9A9AAF', fontWeight: 700, textTransform: 'uppercase' }}>{label}</Typography>
  </Paper>
);

const StatusChip: any = ({ status }: any) => {
  const configs: any = {
    valido: { label: 'Válido', color: 'success' },
    invalido: { label: 'Inválido', color: 'error' },
    pendente: { label: 'Pendente', color: 'warning' },
  };
  const config = configs[status];
  return <Chip label={config.label} color={config.color} size="small" sx={{ fontWeight: 800, fontSize: '0.7rem' }} />;
};

export {ValidacaoPage};