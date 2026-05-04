import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { theme } from './styles/theme';
import { AppRoutes } from './routes/Routes';

function App() {

  return (
   <ThemeProvider theme={theme}>
      <CssBaseline /> 
      <AppRoutes />
    </ThemeProvider>
  )
}

export default App
