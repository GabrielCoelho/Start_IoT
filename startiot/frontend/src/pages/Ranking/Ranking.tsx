import React, { useState } from 'react';
import {
  Box,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  IconButton,
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import FullscreenIcon from '@mui/icons-material/Fullscreen';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import TrendingDownIcon from '@mui/icons-material/TrendingDown';
import RemoveIcon from '@mui/icons-material/Remove';

const RankingPage: React.FC<any> = () => {
  const [lastUpdate, setLastUpdate] = useState<any>(new Date().toLocaleTimeString());
  
  // Mock de dados no estilo "Master Data"
  const [rankingData] = useState<any>([
    { pos: 1, num: '12', name: 'Turbo Rolimã', curso: 'ADS', descidas: 3, melhor: '00:42.310', media: '00:43.500', var: '+2' },
    { pos: 2, num: '7', name: 'Speed Fatec', curso: 'Gestão', descidas: 3, melhor: '00:44.890', media: '00:46.200', var: '-1' },
    { pos: 3, num: '23', name: 'Gravity Force', curso: 'Logística', descidas: 3, melhor: '00:46.120', media: '00:47.800', var: '+1' },
    { pos: 4, num: '3', name: 'AeroFatec', curso: 'Mecatrônica', descidas: 3, melhor: '00:47.450', media: '00:49.100', var: '-2' },
    { pos: 5, num: '18', name: 'Mogi Racing', curso: 'Automação', descidas: 2, melhor: '00:48.700', media: '00:50.300', var: '0' },
    { pos: 6, num: '31', name: 'Downhill Tech', curso: 'TI', descidas: 2, melhor: '00:49.200', media: '00:51.000', var: '+1' },
    { pos: 7, num: '5', name: 'Rolimã Loco', curso: 'Administração', descidas: 1, melhor: '00:51.800', media: '00:51.800', var: '0' },
  ]);

  const getVarIcon: any = (val: any) => {
    if (val.includes('+')) return <TrendingUpIcon sx={{ color: '#22B573', fontSize: 16 }} />;
    if (val.includes('-')) return <TrendingDownIcon sx={{ color: '#C8102E', fontSize: 16 }} />;
    return <RemoveIcon sx={{ color: '#9A9AAF', fontSize: 16 }} />;
  };

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#F4F4F6', p: { xs: 1, md: 3 } }}>
      
      {/* HEADER OPERACIONAL */}
      <Paper elevation={0} sx={{ 
        p: 3, 
        mb: 2, 
        bgcolor: '#1A1A2E', 
        color: 'white', 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        borderRadius: 2
      }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 900 }}>Ranking Geral</Typography>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5 }}>
            <Chip label="EM ANDAMENTO" size="small" sx={{ bgcolor: '#22B573', color: 'white', fontWeight: 800, fontSize: '0.6rem' }} />
            <Typography variant="caption" sx={{ opacity: 0.6 }}>11ª Descida da Ladeira · FATEC Mogi Mirim</Typography>
          </Box>
        </Box>
        
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Typography variant="caption" sx={{ opacity: 0.5 }}>Atualizado às: {lastUpdate}</Typography>
          <IconButton sx={{ color: 'white' }} onClick={() => setLastUpdate(new Date().toLocaleTimeString())}>
            <RefreshIcon />
          </IconButton>
          <IconButton sx={{ color: 'white' }}>
            <FullscreenIcon />
          </IconButton>
        </Box>
      </Paper>

      {/* TABELA ESTILO EXCEL */}
      <TableContainer component={Paper} elevation={0} sx={{ borderRadius: 2, border: '1px solid #E0E0E6' }}>
        <Table sx={{ minWidth: 700 }} size="small">
          <TableHead>
            <TableRow sx={{ bgcolor: '#16213E' }}>
              <TableCell sx={{ color: 'white', fontWeight: 800, width: 60, textAlign: 'center' }}>POS</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 800 }}>EQUIPE</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 800 }}>CURSO</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 800, textAlign: 'center' }}>DESCIDAS</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 800 }}>MELHOR TEMPO</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 800 }}>MÉDIA</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 800, textAlign: 'center' }}>VAR.</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rankingData.map((row: any) => (
              <TableRow 
                key={row.num} 
                sx={{ 
                  '&:nth-of-type(odd)': { bgcolor: '#FFFFFF' },
                  '&:nth-of-type(even)': { bgcolor: '#F9F9FB' },
                  '&:hover': { bgcolor: '#FEE8EB' }, // Feedback visual no hover
                  transition: '0.2s'
                }}
              >
                <TableCell sx={{ textAlign: 'center' }}>
                  <Typography sx={{ 
                    fontWeight: 900, 
                    fontSize: '1.1rem',
                    color: row.pos <= 3 ? (row.pos === 1 ? '#F5A623' : row.pos === 2 ? '#A8A9AD' : '#CD7F32') : '#9A9AAF'
                  }}>
                    {row.pos}º
                  </Typography>
                </TableCell>
                <TableCell>
                  <Typography sx={{ fontWeight: 800, color: '#1A1A2E' }}>#{row.num} {row.name}</Typography>
                </TableCell>
                <TableCell>
                  <Typography variant="body2" sx={{ color: '#9A9AAF', fontSize: '0.75rem' }}>{row.curso}</Typography>
                </TableCell>
                <TableCell sx={{ textAlign: 'center' }}>
                  <Chip label={row.descidas} size="small" variant="outlined" sx={{ fontWeight: 700 }} />
                </TableCell>
                <TableCell>
                  <Typography sx={{ fontWeight: 900, fontFamily: 'monospace', color: '#1A1A2E' }}>{row.melhor}</Typography>
                </TableCell>
                <TableCell>
                  <Typography sx={{ fontSize: '0.8rem', color: '#9A9AAF', fontFamily: 'monospace' }}>{row.media}</Typography>
                </TableCell>
                <TableCell>
                  <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 0.5 }}>
                    {getVarIcon(row.var)}
                    <Typography sx={{ fontSize: '0.7rem', fontWeight: 800, color: row.var.includes('+') ? '#22B573' : row.var.includes('-') ? '#C8102E' : '#9A9AAF' }}>
                      {row.var !== '0' ? row.var : '—'}
                    </Typography>
                  </Box>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* FOOTER DE ANÁLISE */}
      <Box sx={{ mt: 2, display: 'flex', justifyContent: 'space-between', px: 1 }}>
        <Typography variant="caption" sx={{ color: '#9A9AAF' }}>
          START IoT · Processamento em tempo real via MQTT/Node-RED
        </Typography>
        <Typography variant="caption" sx={{ fontWeight: 700, color: '#1A1A2E' }}>
          FATEC MOGI MIRIM - ARTHUR DE AZEVEDO
        </Typography>
      </Box>

    </Box>
  );
};

export {RankingPage};