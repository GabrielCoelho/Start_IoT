import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  Typography,
  Button,
  Paper,
  Stack,

  Divider,
} from '@mui/material';
import SportsScoreOutlinedIcon from '@mui/icons-material/SportsScoreOutlined';
import PlayArrowOutlinedIcon from '@mui/icons-material/PlayArrowOutlined';

const CronometragemPage: React.FC<any> = () => {
  const [running, setRunning] = useState<any>(false);
  const [time, setTime] = useState<any>(0);
  const [status, setStatus] = useState<any>('aguardando'); 
  
  
  const timerRef: any = useRef(null);
  const startTimeRef: any = useRef(0);

  const [teams, setTeams] = useState<any>([
    { id: 1, num: '12', name: 'Turbo Rolimã', curso: 'ADS', time: null },
    { id: 2, num: '7', name: 'Speed Fatec', curso: 'Gestão', time: null },
    { id: 3, num: '23', name: 'Gravity Force', curso: 'Logística', time: null },
  ]);

  // Função para formatar milissegundos
  const formatTime: any = (ms: any) => {
    const minutes = Math.floor(ms / 60000);
    const seconds = Math.floor((ms % 60000) / 1000);
    const centiseconds = Math.floor((ms % 1000) / 10);
    return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}.${String(centiseconds).padStart(2, '0')}`;
  };

  const startRace: any = () => {
    setRunning(true);
    setStatus('andamento');
    startTimeRef.current = Date.now() - time;
    timerRef.current = setInterval(() => {
      setTime(Date.now() - startTimeRef.current);
    }, 40); // 40ms para uma atualização fluida de 25fps
  };

  const registerTime: any = (id: any) => {
    const currentTime = formatTime(time);
    setTeams((prev: any) =>
      prev.map((t: any) => (t.id === id ? { ...t, time: currentTime } : t))
    );
  };

  const finishRace: any = () => {
    clearInterval(timerRef.current);
    setRunning(false);
    setStatus('finalizada');
  };

  useEffect(() => {
    return () => clearInterval(timerRef.current);
  }, []);

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', flexDirection: { xs: 'column', md: 'row' }, bgcolor: '#F4F4F6' }}>
      
      {/* PAINEL ESQUERDO: CRONÔMETRO */}
      <Box sx={{ 
        width: { xs: '100%', md: '380px' }, 
        bgcolor: '#1A1A2E', 
        color: 'white', 
        p: 4, 
        display: 'flex', 
        flexDirection: 'column', 
        justifyContent: 'space-between' 
      }}>
        <Box>
          <Typography variant="overline" sx={{ opacity: 0.6, letterSpacing: 2 }}>Corrida #7</Typography>
          <Typography variant="h5" sx={{ mb: 3, fontWeight:800 }}>Bateria Eliminatória 3</Typography>
          
          <Divider sx={{ bgcolor: 'rgba(255,255,255,0.1)', mb: 4 }} />

          <Box sx={{ textAlign: 'center', mb: 4 }}>
            <Typography variant="h2" sx={{ 
              fontFamily: 'monospace', 
              fontWeight: 900, 
              color: running ? '#4ade80' : 'white',
              fontSize: { xs: '4rem', md: '4.5rem' }
            }}>
              {formatTime(time)}
            </Typography>
            <Typography variant="caption" sx={{ opacity: 0.5 }}>TEMPO DE PROVA</Typography>
          </Box>

          <Stack spacing={2}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
              <Typography variant="body2" sx={{ opacity: 0.6 }}>Status</Typography>
              <Typography variant="body2" sx={{ fontWeight: 700, color: running ? '#4ade80' : '#C8102E' }}>
                {status.toUpperCase()}
              </Typography>
            </Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
              <Typography variant="body2" sx={{ opacity: 0.6 }}>Registros</Typography>
              <Typography variant="body2" sx={{ fontWeight: 700 }}>
                {teams.filter((t: any) => t.time).length} / {teams.length}
              </Typography>
            </Box>
          </Stack>
        </Box>

        <Stack spacing={2} sx={{ mt: 4 }}>
          {status !== 'andamento' ? (
            <Button 
              variant="contained" 
              fullWidth 
              size="large"
              startIcon={<PlayArrowOutlinedIcon />}
              onClick={startRace}
              disabled={status === 'finalizada'}
              sx={{ bgcolor: '#C8102E', py: 2, fontWeight: 800 }}
            >
              INICIAR CORRIDA
            </Button>
          ) : (
            <Button 
              variant="contained" 
              fullWidth 
              size="large"
              startIcon={<SportsScoreOutlinedIcon />}
              onClick={finishRace}
              sx={{ bgcolor: '#22B573', py: 2, fontWeight: 800 }}
            >
              FINALIZAR BATERIA
            </Button>
          )}
          <Button variant="outlined" fullWidth sx={{ color: 'rgba(255,255,255,0.5)', borderColor: 'rgba(255,255,255,0.2)' }}>
            Cancelar
          </Button>
        </Stack>
      </Box>

      <Box sx={{ flex: 1, p: { xs: 2, md: 4 }, overflowY: 'auto' }}>
        <Typography variant="h6" sx={{ mb: 3, fontWeight: 800, color: '#1A1A2E' }}>
          Equipes na Pista
        </Typography>

        <Stack spacing={2}>
          {teams.map((team: any) => (
            <Paper 
              key={team.id} 
              elevation={0} 
              sx={{ 
                p: 3, 
                borderRadius: 3, 
                display: 'flex', 
                alignItems: 'center', 
                justifyContent: 'space-between',
                borderLeft: '6px solid',
                borderColor: team.time ? '#22B573' : '#C8102E',
                opacity: team.time ? 0.7 : 1,
                transition: '0.3s'
              }}
            >
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 3 }}>
                <Typography variant="h4" sx={{ fontWeight: 900, color: '#C8102E', minWidth: 60 }}>
                  #{team.num}
                </Typography>
                <Box>
                  <Typography variant="subtitle1" sx={{fontWeight:800}}>{team.name}</Typography>
                  <Typography variant="caption" color="text.secondary">{team.curso} — Fatec Mogi Mirim</Typography>
                </Box>
              </Box>

              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                {team.time && (
                  <Typography variant="h6" sx={{ fontWeight: 900, color: '#22B573', fontFamily: 'monospace' }}>
                    {team.time}
                  </Typography>
                )}
                <Button 
                  variant={team.time ? "outlined" : "contained"}
                  color={team.time ? "success" : "primary"}
                  disabled={!running || !!team.time}
                  onClick={() => registerTime(team.id)}
                  sx={{ borderRadius: 2, px: 3, fontWeight: 700 }}
                >
                  {team.time ? 'OK' : 'CHEGOU!'}
                </Button>
              </Box>
            </Paper>
          ))}
        </Stack>
      </Box>

    </Box>
  );
};

export {CronometragemPage};