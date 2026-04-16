import LegalPage from './legal/LegalPage';
import { getTerms } from './legal/legalContent';

export default function Terms() {
  return <LegalPage getContent={getTerms} />;
}
