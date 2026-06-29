-- Remove research wizard subsystem (OnboardingQuestion / OnboardingOption / UserResearchAnswer).
-- General Information tables (general_info_*) and their seed data are unaffected.
-- Seed rows inserted by V6 are removed implicitly by the DROP.
DROP TABLE IF EXISTS user_research_answer;
DROP TABLE IF EXISTS onboarding_option;
DROP TABLE IF EXISTS onboarding_question;
