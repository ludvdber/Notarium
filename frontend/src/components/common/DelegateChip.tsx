import { Chip, Avatar } from '@mui/material';
import type { DelegateMember } from '@/types';

interface Props {
  member: DelegateMember;
}

export default function DelegateChip({ member }: Props) {
  return (
    <Chip
      avatar={<Avatar sx={{ width: 24, height: 24 }}>{member.username.charAt(0).toUpperCase()}</Avatar>}
      label={member.username}
      variant="outlined"
      size="small"
    />
  );
}
