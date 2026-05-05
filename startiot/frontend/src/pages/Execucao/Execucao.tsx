import  { useState } from 'react';
import {
  Box,
  Typography,
  Button,
  Paper,
  Stack,
  Chip,
  LinearProgress,
  Divider,
} from '@mui/material';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import TimerIcon from '@mui/icons-material/Timer';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import { useNavigate } from 'react-router-dom';

const ExecucaoPage: any = () => {
  const navigate: any = useNavigate();

  const [corridas]: any = useState([
    { id: 3, num: 3, status: 'Finalizada', teams: '#12 Turbo Rolimã · #23 Gravity Force', result: '🏆 1º #12 (00:42.310)' },
    { id: 4, num: 4, status: 'Andamento', teams: '#7 Speed Fatec · #3 AeroFatec', ativa: true },
    { id: 5, num: 5, status: 'Aguardando', teams: '#18 Mogi Racing · #31 Downhill Tech' },
    { id: 6, num: 6, status: 'Aguardando', teams: null, alert: 'Sem equipes alocadas' },
  ]);

  const corridaAtiva = corridas.find((c: any) => c.ativa);

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#F4F4F6', p: { xs: 2, md: 4 } }}>
      
      {/* HEADER */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4, flexWrap: 'wrap', gap: 2 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 900, color: '#1A1A2E' }}>Execução do Evento</Typography>
          <Typography variant="body2" sx={{ color: '#9A9AAF' }}>Controle em tempo real das baterias</Typography>
        </Box>
        <Stack direction="row" spacing={2}>
          <Button 
            variant="outlined" 
            startIcon={<ArrowBackIcon />} 
            onClick={() => navigate('/baterias')}
            sx={{ borderColor: '#E0E0E6', color: '#1A1A2E' }}
          >
            Baterias
          </Button>
          <Button variant="contained" color="success" sx={{ fontWeight: 800 }}>
            Finalizar Bateria
          </Button>
        </Stack>
      </Box>

      {/* STATUS DA BATERIA */}
      <Paper elevation={0} sx={{ p: 3, bgcolor: '#1A1A2E', color: 'white', borderRadius: 3, mb: 4 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
          <Box sx={{ flexGrow: 1 }}>
            <Typography variant="overline" sx={{ opacity: 0.6, letterSpacing: 2 }}>Bateria Atual</Typography>
            <Typography variant="h5" sx={{ fontWeight: 800 }}>🏁 Bateria 2 — Semifinal</Typography>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mt: 1 }}>
              <Typography variant="body2" sx={{ opacity: 0.7 }}>Progresso do Evento: 3 de 5 corridas</Typography>
              <Box sx={{ width: 150 }}>
                <LinearProgress variant="determinate" value={60} sx={{ height: 8, borderRadius: 5, bgcolor: 'rgba(255,255,255,0.1)', '& .MuiLinearProgress-bar': { bgcolor: '#22B573' } }} />
              </Box>
            </Box>
          </Box>
          <Box sx={{ textAlign: 'right' }}>
            <Typography variant="h3" sx={{ fontWeight: 900, lineHeight: 1 }}>3/5</Typography>
            <Typography variant="caption" sx={{ opacity: 0.5 }}>CORRIDAS CONCLUÍDAS</Typography>
          </Box>
        </Box>
      </Paper>

      {/* CONTEÚDO PRINCIPAL (BOX FLEX) */}
      <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 4 }}>
        
        {/* COLUNA: SEQUÊNCIA DE CORRIDAS */}
        <Box sx={{ flex: 1 }}>
          <Typography variant="subtitle2" sx={{ mb: 2, fontWeight: 800, color: '#9A9AAF', textTransform: 'uppercase' }}>
            Sequência de Lançamentos
          </Typography>
          <Stack spacing={2}>
            {corridas.map((c: any) => (
              <Paper 
                key={c.id} 
                elevation={0} 
                sx={{ 
                  p: 2, 
                  display: 'flex', 
                  alignItems: 'center', 
                  gap: 3, 
                  borderRadius: 3,
                  borderLeft: '5px solid',
                  borderColor: c.status === 'Finalizada' ? '#22B573' : (c.ativa ? '#C8102E' : '#E0E0E6'),
                  opacity: c.status === 'Finalizada' ? 0.7 : 1
                }}
              >
                <Typography variant="h4" sx={{ fontWeight: 900, color: c.ativa ? '#C8102E' : '#E0E0E6', minWidth: 40, textAlign: 'center' }}>
                  {c.num}
                </Typography>
                <Box sx={{ flexGrow: 1 }}>
                  <Typography sx={{ fontWeight: 800, color: '#1A1A2E' }}>
                    Corrida {c.num} {c.ativa && <Chip label="ATIVA" size="small" sx={{ height: 16, fontSize: '0.6rem', bgcolor: '#C8102E', color: 'white', fontWeight: 900, ml: 1 }} />}
                  </Typography>
                  <Typography variant="caption" sx={{ color: c.teams ? '#9A9AAF' : '#C8102E', fontWeight: c.teams ? 400 : 700 }}>
                    {c.teams || `⚠ ${c.alert}`}
                  </Typography>
                </Box>
                <Box>
                  {c.status === 'Finalizada' ? (
                    <CheckCircleIcon sx={{ color: '#22B573' }} />
                  ) : (
                    <Button 
                      variant={c.ativa ? "contained" : "outlined"} 
                      size="small"
                      disabled={!c.ativa && c.id !== 5}
                      onClick={() => navigate(`/cronometragem/${c.id}`)}
                      sx={{ bgcolor: c.ativa ? '#C8102E' : 'transparent', fontWeight: 700 }}
                    >
                      {c.ativa ? <TimerIcon fontSize="small" /> : <PlayArrowIcon fontSize="small" />}
                    </Button>
                  )}
                </Box>
              </Paper>
            ))}
          </Stack>
        </Box>

        {/* COLUNA: PAINEL DA CORRIDA ATIVA */}
        <Box sx={{ width: { xs: '100%', md: '360px' } }}>
          {corridaAtiva && (
            <Paper elevation={0} sx={{ p: 3, bgcolor: '#1A1A2E', color: 'white', borderRadius: 4, position: 'sticky', top: 80 }}>
              <Typography variant="overline" sx={{ opacity: 0.5, letterSpacing: 1 }}>Operação Atual</Typography>
              <Typography variant="h5" sx={{ fontWeight: 900, mb: 1 }}>Corrida {corridaAtiva.num}</Typography>
              <Typography variant="body2" sx={{ opacity: 0.6, mb: 3 }}>Bateria 2 — Semifinal</Typography>
              
              <Divider sx={{ bgcolor: 'rgba(255,255,255,0.1)', mb: 3 }} />

              <Typography variant="caption" sx={{ opacity: 0.5, display: 'block', mb: 2 }}>EQUIPES EM PISTA</Typography>
              <Stack spacing={1.5} sx={{ mb: 3 }}>
                {/* Aqui você filtraria as equipes reais baseadas na corrida ativa */}
                <ActiveTeamCard num="7" name="Speed Fatec" curso="Gestão" />
                <ActiveTeamCard num="3" name="AeroFatec" curso="Mecatrônica" />
              </Stack>

              <Button 
                variant="contained" 
                fullWidth 
                size="large"
                startIcon={<TimerIcon />}
                onClick={() => navigate(`/cronometragem/${corridaAtiva.id}`)}
                sx={{ bgcolor: '#C8102E', py: 1.5, fontWeight: 800, mb: 1.5 }}
              >
                Cronometrar Agora
              </Button>
              
              <Button 
                variant="contained" 
                fullWidth 
                sx={{ bgcolor: '#22B573', fontWeight: 800, '&:hover': { bgcolor: '#1a8a56' } }}
              >
                Finalizar Corrida
              </Button>
            </Paper>
          )}

          {!corridaAtiva && (
            <Paper sx={{ p: 3, textAlign: 'center', borderRadius: 3, border: '2px dashed #E0E0E6' }}>
              <WarningAmberIcon sx={{ color: '#F5A623', fontSize: 40, mb: 1 }} />
              <Typography variant="subtitle2">Nenhuma corrida ativa</Typography>
              <Typography variant="caption" color="text.secondary">Selecione uma corrida na lista para iniciar</Typography>
            </Paper>
          )}
        </Box>

      </Box>

    </Box>
  );
};

// Sub-componente para cards de equipes no painel lateral
const ActiveTeamCard: any = ({ num, name, curso }: any) => (
  <Box sx={{ bgcolor: 'rgba(255,255,255,0.07)', p: 1.5, borderRadius: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
    <Box>
      <Typography sx={{ fontSize: '0.85rem', fontWeight: 700 }}>{name}</Typography>
      <Typography sx={{ fontSize: '0.7rem', opacity: 0.5 }}>{curso}</Typography>
    </Box>
    <Typography sx={{ fontWeight: 900, color: '#C8102E', fontSize: '1.2rem' }}>#{num}</Typography>
  </Box>
);

export {ExecucaoPage};