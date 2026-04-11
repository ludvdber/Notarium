import { Container } from '@mui/material';
import HeroSection from '@/components/home/HeroSection';
import StatsSection from '@/components/home/StatsSection';
import SectionsGrid from '@/components/home/SectionsGrid';
import AdBannerSection from '@/components/home/AdBannerSection';
import NewsAndLinks from '@/components/home/NewsAndLinks';
import PopularDocs from '@/components/home/PopularDocs';
import LeaderboardDelegates from '@/components/home/LeaderboardDelegates';
import CommunityCarousel from '@/components/home/CommunityCarousel';
import SignupBanner from '@/components/home/SignupBanner';
import Divider from '@/components/ui/Divider';

export default function Home() {
  return (
    <>
      <HeroSection />
      <Container maxWidth="lg">
        <StatsSection />
        <Divider />
        <SectionsGrid />
        <Divider />
        <AdBannerSection />
        <Divider />
        <NewsAndLinks />
        <Divider />
        <PopularDocs />
        <Divider />
        <LeaderboardDelegates />
        <Divider />
        <CommunityCarousel />
        <Divider />
        <SignupBanner />
      </Container>
    </>
  );
}
