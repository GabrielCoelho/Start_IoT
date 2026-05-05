import  { useState } from 'react';
import {
  Box,
  Typography,
  Button,
  Paper,
  Stack,
  Chip,
  TextField,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Divider,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import VisibilityOutlinedIcon from '@mui/icons-material/VisibilityOutlined';

const EquipesPage: any = () => {
  const [open, setOpen] = useState<any>(false);
  const [filter, setFilter] = useState<any>('Todos');

  // Master Data de Equipes
  const [equipesData]: any = useState([
    { id: 1, num: '12', name: 'Turbo Rolimã', curso: 'ADS', membros: 4, carrinho: 'CAR-001', vistoria: true, status: 'Aprovada' },
    { id: 2, num: '7', name: 'Speed Fatec', curso: 'Gestão', membros: 3, carrinho: 'CAR-002', vistoria: true, status: 'Aprovada' },
    { id: 3, num: '23', name: 'Gravity Force', curso: 'Logística', membros: 2, carrinho: 'CAR-003', vistoria: false, status: 'Pendente' },
    { id: 4, num: '3', name: 'AeroFatec', curso: 'Mecatrônica', membros: 5, carrinho: 'CAR-004', vistoria: true, status: 'Aprovada' },
    { id: 5, num: '18', name: 'Mogi Racing', curso: 'Automação', membros: 3, carrinho: null, vistoria: false, status: 'Pendente' },
  ]);

  const filteredEquipes = filter === 'Todos' 
    ? equipesData 
    : equipesData.filter((e: any) => e.status === filter);

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#F4F4F6', p: { xs: 2, md: 4 } }}>
      
      {/* HEADER DA PÁGINA */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4, flexWrap: 'wrap', gap: 2 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 900, color: '#1A1A2E' }}>Equipes — Edição 2026</Typography>
          <Typography variant="body2" sx={{ color: '#9A9AAF' }}>11ª Descida da Ladeira · {equipesData.length} inscritas</Typography>
        </Box>
        <Button 
          variant="contained" 
          startIcon={<AddIcon />}
          onClick={() => setOpen(true)}
          sx={{ bgcolor: '#C8102E', fontWeight: 800, borderRadius: 2, px: 3 }}
        >
          Nova Equipe
        </Button>
      </Box>

      {/* FILTROS RÁPIDOS */}
      <Stack direction="row" spacing={1} sx={{ mb: 4, overflowX: 'auto', pb: 1 }}>
        {['Todos', 'Aprovada', 'Pendente', 'Reprovada'].map((status: any) => (
          <Chip 
            key={status}
            label={status}
            onClick={() => setFilter(status)}
            sx={{ 
              fontWeight: 700, 
              bgcolor: filter === status ? '#1A1A2E' : '#E0E0E6',
              color: filter === status ? '#fff' : '#1A1A2E',
              '&:hover': { bgcolor: filter === status ? '#1A1A2E' : '#D0D0D6' }
            }}
          />
        ))}
      </Stack>

      {/* GRID DE CARDS (Usando Box flex e não Grid) */}
      <Box sx={{ 
        display: 'flex', 
        flexWrap: 'wrap', 
        gap: 3, 
        justifyContent: { xs: 'center', md: 'flex-start' } 
      }}>
        {filteredEquipes.map((equipe: any) => (
          <Paper 
            key={equipe.id}
            elevation={0}
            sx={{ 
              width: { xs: '100%', sm: '280px' },
              p: 3,
              borderRadius: 3,
              borderTop: '5px solid',
              borderColor: equipe.status === 'Aprovada' ? '#22B573' : '#F5A623',
              boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
              transition: '0.2s',
              '&:hover': { transform: 'translateY(-5px)', boxShadow: '0 8px 24px rgba(0,0,0,0.1)' }
            }}
          >
            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
              <Typography sx={{ fontSize: '1.8rem', fontWeight: 900, color: '#C8102E' }}>
                #{equipe.num}
              </Typography>
              <Chip 
                label={equipe.status} 
                size="small" 
                sx={{ 
                  fontSize: '0.65rem', 
                  fontWeight: 800, 
                  bgcolor: equipe.status === 'Aprovada' ? '#E6F9F0' : '#FFF3E0',
                  color: equipe.status === 'Aprovada' ? '#1a7a4a' : '#b05e00'
                }} 
              />
            </Box>

            <Typography sx={{ fontWeight: 800, fontSize: '1.1rem', mb: 0.5 }}>{equipe.name}</Typography>
            <Typography sx={{ fontSize: '0.8rem', color: '#9A9AAF', mb: 2 }}>{equipe.curso} · {equipe.membros} membros</Typography>

            <Stack direction="row" spacing={1} sx={{ mb: 3 }}>
              <Chip 
                label={equipe.carrinho ? 'Carrinho OK' : 'S/ Carrinho'} 
                variant="outlined" 
                size="small"
                sx={{ fontSize: '0.65rem', fontWeight: 700, borderColor: equipe.carrinho ? '#22B573' : '#E0E0E6' }}
              />
              <Chip 
                label={equipe.vistoria ? 'Vistoria OK' : 'Vistoria Pend.'} 
                variant="outlined" 
                size="small"
                sx={{ fontSize: '0.65rem', fontWeight: 700, borderColor: equipe.vistoria ? '#22B573' : '#E0E0E6' }}
              />
            </Stack>

            <Divider sx={{ mb: 2 }} />

            <Stack direction="row" spacing={1}>
              <Button fullWidth size="small" variant="outlined" startIcon={<VisibilityOutlinedIcon />} sx={{ textTransform: 'none', fontSize: '0.75rem' }}>
                Ver
              </Button>
              <Button fullWidth size="small" variant="contained" color="success" sx={{ textTransform: 'none', fontSize: '0.75rem', bgcolor: '#22B573' }}>
                Aprovar
              </Button>
            </Stack>
          </Paper>
        ))}
      </Box>

      {/* MODAL CADASTRO (Simplificado) */}
      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="xs" fullWidth sx={{ '& .MuiPaper-root': { borderRadius: 3 } }}>
        <DialogTitle sx={{ fontWeight: 800 }}>➕ Cadastrar Equipe</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField fullWidth label="Nome da Equipe" variant="outlined"  />
            <Box sx={{ display: 'flex', gap: 2 }}>
              <TextField select fullWidth label="Curso" defaultValue="ADS">
                <MenuItem value="ADS">ADS</MenuItem>
                <MenuItem value="Gestão">Gestão</MenuItem>
              </TextField>
              <TextField fullWidth label="Número" type="number" />
            </Box>
            <TextField fullWidth label="Membro Líder" variant="outlined" />
          </Stack>
        </DialogContent>
        <DialogActions sx={{ p: 3 }}>
          <Button onClick={() => setOpen(false)} color="inherit">Cancelar</Button>
          <Button variant="contained" onClick={() => setOpen(false)} sx={{ bgcolor: '#C8102E' }}>Salvar Equipe</Button>
        </DialogActions>
      </Dialog>

    </Box>
  );
};

export  {EquipesPage};