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
import { useParams } from 'react-router-dom';

// Simulação de Banco de Dados - Master Data
const BATERIAS_DATA: any = {
  "1": { 
    nome: "Eliminatória A", 
    equipes: [
      { id: 1, num: '10', name: 'Alpha', curso: 'ADS' },
      { id: 2, num: '05', name: 'Beta', curso: 'Mecânica' }
    ] 
  },
  "2": { 
    nome: "Eliminatória B", 
    equipes: [
      { id: 3, num: '07', name: 'Gamma', curso: 'Logística' },
      { id: 4, num: '02', name: 'Delta', curso: 'Gestão' }
    ] 
  },
  "3": { 
    nome: "Grande Final", 
    equipes: [
      { id: 5, num: '12', name: 'Turbo', curso: 'ADS' },
      { id: 6, num: '99', name: 'Relâmpago', curso: 'TI' }
    ] 
  }
};

const CronometragemPage: React.FC<any> = () => {
  const { id }: any = useParams();
  
  // Estados da Corrida
  const [running, setRunning] = useState<any>(false);
  const [time, setTime] = useState<any>(0);
  const [status, setStatus] = useState<any>('aguardando');
  const [bateria, setBateria] = useState<any>(null);
  const [teams, setTeams] = useState<any>([]);

  // Refs para controle preciso do Timer
  const timerRef: any = useRef(null);
  const startTimeRef: any = useRef(0);

  // Efeito para carregar dados quando o ID da URL mudar
  useEffect(() => {
    const dadosRecuperados = BATERIAS_DATA[id];
    if (dadosRecuperados) {
      setBateria(dadosRecuperados);
      // Prepara as equipes adicionando o campo 'time' para o cronômetro
      setTeams(dadosRecuperados.equipes.map((t: any) => ({ ...t, time: null })));
      // Reseta o cronômetro ao trocar de bateria
      setTime(0);
      setStatus('aguardando');
      setRunning(false);
      if (timerRef.current) clearInterval(timerRef.current);
    }
  }, [id]);

  // Cleanup ao sair da página
  useEffect(() => {
    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, []);

  // Formatação de tempo: 00:00.00
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
    }, 40);
  };

  const registerTime: any = (teamId: any) => {
    const currentTime = formatTime(time);
    setTeams((prev: any) =>
      prev.map((t: any) => (t.id === teamId ? { ...t, time: currentTime } : t))
    );
  };

  const finishRace: any = () => {
    clearInterval(timerRef.current);
    setRunning(false);
    setStatus('finalizada');
  };

  if (!bateria) {
    return (
      <Box sx={{ p: 4, textAlign: 'center' }}>
        <Typography color="error">Bateria {id} não encontrada no sistema.</Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', flexDirection: { xs: 'column', md: 'row' }, bgcolor: '#F4F4F6' }}>
      
      
      <Box sx={{ 
        width: { xs: '100%', md: '380px' }, 
        bgcolor: '#1A1A2E', 
        color: 'white', 
        p: 4, 
        display: 'flex', 
        flexDirection: 'column', 
        justifyContent: 'space-between',
        zIndex: 10
      }}>
        <Box>
          <Typography variant="overline" sx={{ opacity: 0.6, letterSpacing: 2 }}>MODO OPERAÇÃO</Typography>
          <Typography variant="h5" sx={{ mb: 3, fontWeight: 800 }}>{bateria.nome}</Typography>
          
          <Divider sx={{ bgcolor: 'rgba(255,255,255,0.1)', mb: 4 }} />

          <Box sx={{ textAlign: 'center', mb: 4 }}>
            <Typography variant="h2" sx={{ 
              fontFamily: 'monospace', 
              fontWeight: 900, 
              color: running ? '#4ade80' : 'white',
              fontSize: { xs: '3.5rem', md: '4rem' }
            }}>
              {formatTime(time)}
            </Typography>
            <Typography variant="caption" sx={{ opacity: 0.5 }}>TEMPO ATUAL DA BATERIA</Typography>
          </Box>

          <Stack spacing={2}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
              <Typography variant="body2" sx={{ opacity: 0.6 }}>Status do Evento</Typography>
              <Typography variant="body2" sx={{ sx: { fontWeight: 700 }, color: running ? '#4ade80' : '#C8102E' }}>
                {status.toUpperCase()}
              </Typography>
            </Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
              <Typography variant="body2" sx={{ opacity: 0.6 }}>Check-ins Realizados</Typography>
              <Typography variant="body2" sx={{ sx: { fontWeight: 700 } }}>
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
              sx={{ bgcolor: '#C8102E', py: 2, fontWeight: 800, '&:hover': { bgcolor: '#9B0D23' } }}
            >
              INICIAR CRONÔMETRO
            </Button>
          ) : (
            <Button 
              variant="contained" 
              fullWidth 
              size="large"
              startIcon={<SportsScoreOutlinedIcon />}
              onClick={finishRace}
              sx={{ bgcolor: '#22B573', py: 2, fontWeight: 800, '&:hover': { bgcolor: '#1a8a56' } }}
            >
              ENCERRAR BATERIA
            </Button>
          )}
          <Button 
            variant="outlined" 
            fullWidth 
            onClick={() => setTime(0)}
            disabled={running}
            sx={{ color: 'rgba(255,255,255,0.5)', borderColor: 'rgba(255,255,255,0.2)', textTransform: 'none' }}
          >
            Resetar Tempo
          </Button>
        </Stack>
      </Box>

      {/* PAINEL DIREITO: LISTA DINÂMICA DE EQUIPES */}
      <Box sx={{ flex: 1, p: { xs: 2, md: 4 }, overflowY: 'auto' }}>
        <Typography variant="h6" sx={{ mb: 3, sx: { fontWeight: 800 }, color: '#1A1A2E' }}>
          Equipes Alocadas para esta Descida
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
                transition: 'all 0.3s ease'
              }}
            >
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 3 }}>
                <Typography variant="h4" sx={{ sx: { fontWeight: 900 }, color: '#C8102E', minWidth: 60 }}>
                  #{team.num}
                </Typography>
                <Box>
                  <Typography variant="subtitle1" sx={{ sx: { fontWeight: 800 } }}>{team.name}</Typography>
                  <Typography variant="caption" color="text.secondary">{team.curso} — FATEC Mogi Mirim</Typography>
                </Box>
              </Box>

              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                {team.time && (
                  <Typography variant="h6" sx={{ sx: { fontWeight: 900 }, color: '#22B573', fontFamily: 'monospace' }}>
                    {team.time}
                  </Typography>
                )}
                <Button 
                  variant={team.time ? "outlined" : "contained"}
                  color={team.time ? "success" : "primary"}
                  disabled={!running || !!team.time}
                  onClick={() => registerTime(team.id)}
                  sx={{ borderRadius: 2, px: 3, sx: { fontWeight: 700 }, textTransform: 'none' }}
                >
                  {team.time ? 'Registrado' : '🏁 Chegou!'}
                </Button>
              </Box>
            </Paper>
          ))}
        </Stack>

        {teams.length === 0 && (
          <Typography sx={{ mt: 4, textAlign: 'center', color: '#9A9AAF' }}>
            Nenhuma equipe vinculada a esta bateria.
          </Typography>
        )}
      </Box>

    </Box>
  );
};

export { CronometragemPage };