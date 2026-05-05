import  { useState } from 'react';
import {
  Box,
  Typography,
  Button,

  Stack,
  Chip,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Divider,
} from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import TimerIcon from '@mui/icons-material/Timer';
import AssessmentOutlinedIcon from '@mui/icons-material/AssessmentOutlined';
import { useNavigate } from 'react-router-dom';

const BateriasPage: any = () => {
  const navigate: any = useNavigate();

  const [baterias] = useState([
    {
      id: '1',
      nome: 'Bateria 1 — Eliminatória',
      status: 'Finalizada',
      horario: '08:30',
      corridas: [
        { id: 1, num: 1, status: 'Finalizada', equipes: ['#12 Turbo Rolimã', '#7 Speed Fatec', '#3 AeroFatec'] },
        { id: 2, num: 2, status: 'Finalizada', equipes: ['#23 Gravity Force', '#18 Mogi Racing', '#31 Downhill Tech'] },
      ],
    },
    {
      id: '2',
      nome: 'Bateria 2 — Semifinal',
      status: 'Andamento',
      horario: '10:00',
      corridas: [
        { id: 3, num: 3, status: 'Finalizada', equipes: ['#12 Turbo Rolimã', '#23 Gravity Force'] },
        { id: 4, num: 4, status: 'Andamento', equipes: ['#7 Speed Fatec', '#3 AeroFatec'], atual: true },
        { id: 5, num: 5, status: 'Aguardando', equipes: [], alert: 'Sem equipes alocadas' },
      ],
    },
    {
      id: '3',
      nome: 'Bateria 3 — Final',
      status: 'Aguardando',
      horario: '14:00',
      corridas: [
        { id: 6, num: 6, status: 'Aguardando', equipes: [], alert: 'Aguardando classificação' },
      ],
    },
  ]);

  const getStatusColor: any = (status: any) => {
    switch (status) {
      case 'Finalizada': return { bg: '#E8F0FE', text: '#1a56db' };
      case 'Andamento': return { bg: '#E6F9F0', text: '#1a7a4a' };
      default: return { bg: '#FFF3E0', text: '#b05e00' };
    }
  };

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#F4F4F6', p: { xs: 2, md: 4 } }}>
      
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4, flexWrap: 'wrap', gap: 2 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 900, color: '#1A1A2E' }}>Baterias e Corridas</Typography>
          <Typography variant="body2" sx={{ color: '#9A9AAF' }}>11ª Descida da Ladeira · Edição 2026</Typography>
        </Box>
        <Stack direction="row" spacing={2}>
          <Button variant="outlined" sx={{ borderColor: '#E0E0E6', color: '#1A1A2E', fontWeight: 700 }}>
            + Nova Bateria
          </Button>
          <Button 
            variant="contained" 
            sx={{ bgcolor: '#C8102E', fontWeight: 800 }}
            onClick={() => navigate('/cronometragem')}
          >
            Executar Evento
          </Button>
        </Stack>
      </Box>

      <Stack spacing={2}>
        {baterias.map((bateria: any) => {
          const colors = getStatusColor(bateria.status);
          return (
            <Accordion 
              key={bateria.id} 
              elevation={0} 
              defaultExpanded={bateria.status === 'Andamento'}
              sx={{ 
                borderRadius: '12px !important', 
                overflow: 'hidden',
                border: '1px solid #E0E0E6',
                '&:before': { display: 'none' } 
              }}
            >
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', width: '100%', pr: 2 }}>
                  <Box>
                    <Typography sx={{ fontWeight: 800, color: '#1A1A2E' }}>{bateria.nome}</Typography>
                    <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
                      <Typography sx={{ fontSize: '0.75rem', color: '#9A9AAF' }}>
                        {bateria.corridas.length} corridas • Previsto: {bateria.horario}h
                      </Typography>
                      <Chip 
                        label={bateria.status} 
                        size="small" 
                        sx={{ bgcolor: colors.bg, color: colors.text, fontWeight: 800, fontSize: '0.65rem', height: 20 }} 
                      />
                    </Stack>
                  </Box>
                  {bateria.status === 'Aguardando' && (
                    <Button size="small" variant="contained" startIcon={<PlayArrowIcon />} sx={{ bgcolor: '#C8102E', fontSize: '0.7rem' }}>
                      Iniciar
                    </Button>
                  )}
                </Box>
              </AccordionSummary>

              <AccordionDetails sx={{ p: 0, bgcolor: '#FAFAFB' }}>
                <Divider />
                {bateria.corridas.map((corrida: any) => (
                  <Box 
                    key={corrida.id} 
                    sx={{ 
                      display: 'flex', 
                      alignItems: 'center', 
                      p: 2, 
                      pl: 5, 
                      borderBottom: '1px solid #E0E0E6',
                      bgcolor: corrida.atual ? '#FFFBEA' : 'transparent',
                      '&:last-child': { borderBottom: 'none' }
                    }}
                  >
                    <Typography sx={{ fontWeight: 800, width: 100, fontSize: '0.9rem' }}>
                      Corrida {corrida.num} {corrida.atual && '●'}
                    </Typography>

                    <Box sx={{ flexGrow: 1, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                      {corrida.equipes.length > 0 ? (
                        corrida.equipes.map((equipe: any) => (
                          <Chip key={equipe} label={equipe} size="small" sx={{ fontSize: '0.7rem', fontWeight: 600, borderRadius: '4px' }} />
                        ))
                      ) : (
                        <Typography sx={{ fontSize: '0.75rem', color: '#C8102E', fontWeight: 700, bgcolor: '#FDEAEA', px: 1, borderRadius: 1 }}>
                          ⚠ {corrida.alert}
                        </Typography>
                      )}
                    </Box>

                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                      <Chip 
                        label={corrida.status} 
                        variant="outlined"
                        size="small" 
                        sx={{ 
                          fontSize: '0.65rem', 
                          fontWeight: 800, 
                          color: getStatusColor(corrida.status).text,
                          borderColor: getStatusColor(corrida.status).text
                        }} 
                      />
                      {corrida.status === 'Andamento' ? (
                        <Button 
                          variant="contained" 
                          size="small" 
                          startIcon={<TimerIcon />}
                          onClick={() => navigate(`/cronometragem/${bateria.id}`)}
                          sx={{ bgcolor: '#C8102E', textTransform: 'none', fontSize: '0.75rem' }}
                        >
                          Cronometrar
                        </Button>
                      ) : (
                        <Button 
                          variant="outlined" 
                          size="small" 
                          startIcon={<AssessmentOutlinedIcon />}
                          sx={{ textTransform: 'none', fontSize: '0.75rem', color: '#1A1A2E', borderColor: '#E0E0E6' }}
                        >
                          Resultados
                        </Button>
                      )}
                    </Box>
                  </Box>
                ))}
              </AccordionDetails>
            </Accordion>
          );
        })}
      </Stack>

    </Box>
  );
};

export default BateriasPage;