import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Paper,
  Stack,
  Chip,
  Avatar,
  IconButton,
  Divider,
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import TrendingDownIcon from '@mui/icons-material/TrendingDown';
import RemoveIcon from '@mui/icons-material/Remove';
import EmojiEventsIcon from '@mui/icons-material/EmojiEvents';

const RankingPage: any = () => {
  const [lastUpdate, setLastUpdate] = useState<any>(new Date().toLocaleTimeString());

  // Dados Mockados (Master Data)
  const [ranking]: any = useState([
    { pos: 1, num: '12', name: 'Turbo Rolimã', curso: 'ADS', descidas: 3, melhor: '00:42.310', var: '+2' },
    { pos: 2, num: '7', name: 'Speed Fatec', curso: 'Gestão', descidas: 3, melhor: '00:44.890', var: '-1' },
    { pos: 3, num: '23', name: 'Gravity Force', curso: 'Logística', descidas: 3, melhor: '00:46.120', var: '+1' },
    { pos: 4, num: '3', name: 'AeroFatec', curso: 'Mecatrônica', descidas: 3, melhor: '00:47.450', var: '-2' },
    { pos: 5, num: '18', name: 'Mogi Racing', curso: 'Automação', descidas: 2, melhor: '00:48.700', var: '0' },
    { pos: 6, num: '31', name: 'Downhill Tech', curso: 'TI', descidas: 2, melhor: '00:49.200', var: '+1' },
  ]);

  const getPodiumColor: any = (pos: any) => {
    if (pos === 1) return '#F5A623'; // Gold
    if (pos === 2) return '#A8A9AD'; // Silver
    if (pos === 3) return '#CD7F32'; // Bronze
    return '#1A1A2E';
  };

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#F4F4F6', p: { xs: 2, md: 4 } }}>
      
      {/* HEADER DA PÁGINA */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 900, color: '#1A1A2E' }}>
            Ranking Geral — Edição 2026
          </Typography>
          <Stack direction="row" spacing={1}  sx={{ mt: 0.5, alignItems:"center" }}>
            <Chip 
              label="AO VIVO" 
              size="small" 
              sx={{ bgcolor: '#22B573', color: 'white', fontWeight: 800, height: 20, fontSize: '0.6rem' }} 
            />
            <Typography variant="caption" sx={{ color: '#9A9AAF' }}>
              Última atualização: {lastUpdate}
            </Typography>
          </Stack>
        </Box>
        <IconButton onClick={() => setLastUpdate(new Date().toLocaleTimeString())} sx={{ bgcolor: 'white', boxShadow: 1 }}>
          <RefreshIcon />
        </IconButton>
      </Box>

      {/* ÁREA DO PÓDIO (TOP 3) */}
      <Box sx={{ 
        display: 'flex', 
        flexDirection: { xs: 'column', md: 'row' }, 
        gap: 3, 
        mb: 5, 
        alignItems: 'flex-end',
        justifyContent: 'center'
      }}>
        {/* Segundo Lugar */}
        <PodiumCard team={ranking[1]} color={getPodiumColor(2)} height={200} />
        
        {/* Primeiro Lugar */}
        <PodiumCard team={ranking[0]} color={getPodiumColor(1)} height={240} isWinner />
        
        {/* Terceiro Lugar */}
        <PodiumCard team={ranking[2]} color={getPodiumColor(3)} height={180} />
      </Box>

      <Divider sx={{ mb: 4 }}><Chip label="DEMAIS POSIÇÕES" variant="outlined" /></Divider>

      {/* LISTA RESTANTE (CARDS COMPACTOS) */}
      <Stack spacing={2} sx={{ maxWidth: 800, mx: 'auto' }}>
        {ranking.slice(3).map((item: any) => (
          <Paper 
            key={item.id} 
            elevation={0} 
            sx={{ 
              p: 2, 
              borderRadius: 3, 
              display: 'flex', 
              alignItems: 'center',
              border: '1px solid #E0E0E6',
              transition: '0.2s',
              '&:hover': { transform: 'scale(1.01)', boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }
            }}
          >
            <Typography sx={{ width: 40, fontWeight: 900, color: '#9A9AAF', textAlign: 'center' }}>
              {item.pos}º
            </Typography>
            <Box sx={{ flexGrow: 1, ml: 2 }}>
              <Typography sx={{ fontWeight: 800, color: '#1A1A2E' }}>#{item.num} {item.name}</Typography>
              <Typography variant="caption" color="text.secondary">{item.curso} · {item.descidas} descidas</Typography>
            </Box>
            <Box sx={{ textAlign: 'right', mr: 3 }}>
              <Typography sx={{ fontWeight: 900, fontFamily: 'monospace', color: '#1A1A2E' }}>{item.melhor}</Typography>
              <Typography variant="caption" sx={{ display: 'block', opacity: 0.5 }}>MELHOR TEMPO</Typography>
            </Box>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, minWidth: 40 }}>
              {item.var.includes('+') ? <TrendingUpIcon sx={{ color: '#22B573', fontSize: 16 }} /> : 
               item.var.includes('-') ? <TrendingDownIcon sx={{ color: '#C8102E', fontSize: 16 }} /> : 
               <RemoveIcon sx={{ color: '#9A9AAF', fontSize: 16 }} />}
              <Typography sx={{ fontSize: '0.75rem', fontWeight: 700 }}>{item.var}</Typography>
            </Box>
          </Paper>
        ))}
      </Stack>
    </Box>
  );
};

// Componente Interno para Cards do Pódio
const PodiumCard: any = ({ team, color, height, isWinner }: any) => (
  <Paper 
    elevation={4}
    sx={{ 
      width: { xs: '100%', md: 240 }, 
      height: height, 
      bgcolor: color, 
      color: 'white', 
      borderRadius: 4,
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      position: 'relative',
      overflow: 'hidden',
      p: 2,
      boxShadow: isWinner ? `0 0 20px ${color}80` : 1
    }}
  >
    <Typography sx={{ 
      position: 'absolute', 
      top: -10, 
      left: 10, 
      fontSize: '5rem', 
      fontWeight: 900, 
      opacity: 0.2 
    }}>
      {team.pos}
    </Typography>
    
    {isWinner && <EmojiEventsIcon sx={{ fontSize: 40, mb: 1 }} />}
    
    <Typography sx={{ fontWeight: 900, fontSize: '1.2rem' }}>#{team.num}</Typography>
    <Typography sx={{ fontWeight: 800, textAlign: 'center' }}>{team.name}</Typography>
    <Typography variant="caption" sx={{ opacity: 0.8, mb: 2 }}>{team.curso}</Typography>
    
    <Box sx={{ bgcolor: 'rgba(255,255,255,0.2)', px: 2, py: 0.5, borderRadius: 2 }}>
      <Typography sx={{ fontWeight: 900, fontFamily: 'monospace', fontSize: '1.3rem' }}>
        {team.melhor}
      </Typography>
    </Box>
    
    {isWinner && (
      <Typography variant="caption" sx={{ mt: 1, fontWeight: 800, letterSpacing: 1 }}>
        🏆 ATUAL LÍDER
      </Typography>
    )}
  </Paper>
);

export {RankingPage};