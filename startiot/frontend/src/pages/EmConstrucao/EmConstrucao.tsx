import React from 'react';
import { Box, Typography, Paper } from '@mui/material';
import ConstructionOutlinedIcon from '@mui/icons-material/ConstructionOutlined';

interface EmConstrucaoProps {
  titulo: string;
  descricao?: string;
}

const EmConstrucaoPage: React.FC<EmConstrucaoProps> = ({ titulo, descricao }) => (
  <Box sx={{ minHeight: '100vh', bgcolor: '#F4F4F6', display: 'flex', alignItems: 'center', justifyContent: 'center', p: 4 }}>
    <Paper elevation={0} sx={{ p: 6, borderRadius: 3, border: '1px solid #E0E0E6', textAlign: 'center', maxWidth: 440 }}>
      <ConstructionOutlinedIcon sx={{ fontSize: 56, color: '#E0E0E6', mb: 2 }} />
      <Typography variant="h6" sx={{ fontWeight: 900, color: '#1A1A2E', mb: 1 }}>
        {titulo}
      </Typography>
      <Typography variant="body2" color="text.secondary">
        {descricao ?? 'Este módulo está em construção e estará disponível em breve.'}
      </Typography>
    </Paper>
  </Box>
);

export { EmConstrucaoPage };
