import { SvgIcon } from '@mui/material';

const RaceFlagIcon: any = (props: any) => (
  <SvgIcon {...props} viewBox="0 0 24 24" sx={{ fontSize: '4rem', ...props.sx }}>
    <path d="M2 2h20v14H2z" fill="#ccc" />
    <path d="M2 2h5v5H2zm5 5h5v5H7zm5-5h5v5h-5zm5 5h5v5h-5zM2 12h5v5H2zm10 0h5v5h-5z" fill="#000" />
    <path d="M2 2v18" stroke="#000" strokeWidth="2" />
  </SvgIcon>
);

export  {RaceFlagIcon};