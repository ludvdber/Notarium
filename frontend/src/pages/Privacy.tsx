import LegalPage from './legal/LegalPage';
import { getPrivacy } from './legal/legalContent';

export default function Privacy() {
  return <LegalPage getContent={getPrivacy} />;
}
