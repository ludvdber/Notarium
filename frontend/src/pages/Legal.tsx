import LegalPage from './legal/LegalPage';
import { getLegal } from './legal/legalContent';

export default function Legal() {
  return <LegalPage getContent={getLegal} />;
}
