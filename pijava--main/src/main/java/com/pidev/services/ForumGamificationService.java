package com.pidev.services;

import com.pidev.models.Question;
import com.pidev.models.Reponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Forum Gamification System
 * Features: Badges, Reputation, Leaderboard, Achievements
 */
public class ForumGamificationService {
    
    private final QuestionService questionService = new QuestionService();
    private final ReponseService reponseService = new ReponseService();
    
    // ═══════════════════════════════════════════════
    //  REPUTATION CALCULATION
    // ═══════════════════════════════════════════════
    
    /**
     * Calculate user reputation score
     * @param userId User ID
     * @param isDoctor true if doctor, false if patient
     * @return Reputation score
     */
    public int calculateReputation(int userId, boolean isDoctor) {
        int reputation = 0;
        
        try {
            if (isDoctor) {
                // Doctor reputation based on responses
                List<Reponse> responses = reponseService.getByMedecin(userId);
                
                for (Reponse r : responses) {
                    reputation += 10; // Base points per response
                    reputation += r.getLikes() * 5; // Bonus for likes
                }
                
                // Bonus for being active
                if (responses.size() > 50) reputation += 100;
                else if (responses.size() > 20) reputation += 50;
                else if (responses.size() > 10) reputation += 25;
                
            } else {
                // Patient reputation based on questions
                List<Question> questions = questionService.getByPatient(userId);
                
                for (Question q : questions) {
                    reputation += 5; // Base points per question
                    reputation += q.getLikes() * 3; // Bonus for likes
                    reputation += q.getAnswerCount() * 2; // Bonus for getting answers
                }
                
                // Bonus for being active
                if (questions.size() > 30) reputation += 75;
                else if (questions.size() > 15) reputation += 40;
                else if (questions.size() > 5) reputation += 20;
            }
            
        } catch (Exception e) {
            System.err.println("Error calculating reputation: " + e.getMessage());
        }
        
        return reputation;
    }
    
    // ═══════════════════════════════════════════════
    //  BADGES SYSTEM
    // ═══════════════════════════════════════════════
    
    /**
     * Get user badges
     * @param userId User ID
     * @param isDoctor true if doctor, false if patient
     * @return List of badge names
     */
    public List<String> getUserBadges(int userId, boolean isDoctor) {
        List<String> badges = new ArrayList<>();
        int reputation = calculateReputation(userId, isDoctor);
        
        try {
            if (isDoctor) {
                List<Reponse> responses = reponseService.getByMedecin(userId);
                int totalLikes = responses.stream().mapToInt(Reponse::getLikes).sum();
                
                // Response count badges
                if (responses.size() >= 100) badges.add("🏆 Expert Légendaire");
                else if (responses.size() >= 50) badges.add("⭐ Super Expert");
                else if (responses.size() >= 20) badges.add("✨ Expert");
                else if (responses.size() >= 10) badges.add("📚 Contributeur");
                
                // Likes badges
                if (totalLikes >= 200) badges.add("💎 Très Apprécié");
                else if (totalLikes >= 100) badges.add("❤️ Apprécié");
                else if (totalLikes >= 50) badges.add("👍 Populaire");
                
                // Quality badges
                double avgLikes = responses.isEmpty() ? 0 : (double) totalLikes / responses.size();
                if (avgLikes >= 5) badges.add("🌟 Réponses de Qualité");
                
                // Activity badges
                if (responses.size() >= 5) badges.add("🔥 Actif");
                
            } else {
                List<Question> questions = questionService.getByPatient(userId);
                int totalLikes = questions.stream().mapToInt(Question::getLikes).sum();
                
                // Question count badges
                if (questions.size() >= 50) badges.add("🏆 Curieux Légendaire");
                else if (questions.size() >= 30) badges.add("⭐ Super Curieux");
                else if (questions.size() >= 15) badges.add("✨ Curieux");
                else if (questions.size() >= 5) badges.add("📝 Questionneur");
                
                // Likes badges
                if (totalLikes >= 100) badges.add("💎 Questions Appréciées");
                else if (totalLikes >= 50) badges.add("❤️ Bonnes Questions");
                else if (totalLikes >= 20) badges.add("👍 Questions Utiles");
                
                // Engagement badges
                int totalAnswers = questions.stream().mapToInt(Question::getAnswerCount).sum();
                if (totalAnswers >= 50) badges.add("💬 Engagé");
                
                // Activity badges
                if (questions.size() >= 3) badges.add("🔥 Actif");
            }
            
            // Reputation badges
            if (reputation >= 1000) badges.add("👑 VIP");
            else if (reputation >= 500) badges.add("💫 Membre d'Or");
            else if (reputation >= 200) badges.add("🥈 Membre d'Argent");
            else if (reputation >= 100) badges.add("🥉 Membre de Bronze");
            
        } catch (Exception e) {
            System.err.println("Error getting badges: " + e.getMessage());
        }
        
        return badges;
    }
    
    // ═══════════════════════════════════════════════
    //  LEADERBOARD
    // ═══════════════════════════════════════════════
    
    /**
     * User stats for leaderboard
     */
    public static class UserStats {
        public int userId;
        public String userName;
        public int reputation;
        public int contributions;
        public int likes;
        public List<String> badges;
        public String userType; // "doctor" or "patient"
        
        public UserStats(int userId, String userName, int reputation, int contributions, 
                        int likes, List<String> badges, String userType) {
            this.userId = userId;
            this.userName = userName;
            this.reputation = reputation;
            this.contributions = contributions;
            this.likes = likes;
            this.badges = badges;
            this.userType = userType;
        }
    }
    
    /**
     * Get top doctors leaderboard
     * @param limit Number of top doctors to return
     * @return List of top doctors with stats
     */
    public List<UserStats> getTopDoctors(int limit) {
        List<UserStats> leaderboard = new ArrayList<>();
        
        try {
            // Get all responses grouped by doctor
            List<Reponse> allResponses = reponseService.getAll();
            Map<Integer, List<Reponse>> responsesByDoctor = allResponses.stream()
                .collect(Collectors.groupingBy(Reponse::getMedecinId));
            
            // Calculate stats for each doctor
            for (Map.Entry<Integer, List<Reponse>> entry : responsesByDoctor.entrySet()) {
                int doctorId = entry.getKey();
                List<Reponse> responses = entry.getValue();
                
                if (!responses.isEmpty()) {
                    String doctorName = responses.get(0).getMedecinName();
                    int reputation = calculateReputation(doctorId, true);
                    int contributions = responses.size();
                    int likes = responses.stream().mapToInt(Reponse::getLikes).sum();
                    List<String> badges = getUserBadges(doctorId, true);
                    
                    leaderboard.add(new UserStats(doctorId, doctorName, reputation, 
                                                 contributions, likes, badges, "doctor"));
                }
            }
            
            // Sort by reputation (descending)
            leaderboard.sort((a, b) -> Integer.compare(b.reputation, a.reputation));
            
            // Return top N
            return leaderboard.stream().limit(limit).collect(Collectors.toList());
            
        } catch (Exception e) {
            System.err.println("Error getting top doctors: " + e.getMessage());
        }
        
        return leaderboard;
    }
    
    /**
     * Get top patients leaderboard
     * @param limit Number of top patients to return
     * @return List of top patients with stats
     */
    public List<UserStats> getTopPatients(int limit) {
        List<UserStats> leaderboard = new ArrayList<>();
        
        try {
            // Get all questions grouped by patient
            List<Question> allQuestions = questionService.getAll();
            Map<Integer, List<Question>> questionsByPatient = allQuestions.stream()
                .collect(Collectors.groupingBy(Question::getPatientId));
            
            // Calculate stats for each patient
            for (Map.Entry<Integer, List<Question>> entry : questionsByPatient.entrySet()) {
                int patientId = entry.getKey();
                List<Question> questions = entry.getValue();
                
                if (!questions.isEmpty()) {
                    String patientName = questions.get(0).getPatientName();
                    int reputation = calculateReputation(patientId, false);
                    int contributions = questions.size();
                    int likes = questions.stream().mapToInt(Question::getLikes).sum();
                    List<String> badges = getUserBadges(patientId, false);
                    
                    leaderboard.add(new UserStats(patientId, patientName, reputation, 
                                                 contributions, likes, badges, "patient"));
                }
            }
            
            // Sort by reputation (descending)
            leaderboard.sort((a, b) -> Integer.compare(b.reputation, a.reputation));
            
            // Return top N
            return leaderboard.stream().limit(limit).collect(Collectors.toList());
            
        } catch (Exception e) {
            System.err.println("Error getting top patients: " + e.getMessage());
        }
        
        return leaderboard;
    }
    
    // ═══════════════════════════════════════════════
    //  ACHIEVEMENTS
    // ═══════════════════════════════════════════════
    
    /**
     * Achievement definition
     */
    public static class Achievement {
        public String name;
        public String description;
        public String icon;
        public boolean unlocked;
        public int progress;
        public int target;
        
        public Achievement(String name, String description, String icon, 
                          boolean unlocked, int progress, int target) {
            this.name = name;
            this.description = description;
            this.icon = icon;
            this.unlocked = unlocked;
            this.progress = progress;
            this.target = target;
        }
    }
    
    /**
     * Get user achievements
     * @param userId User ID
     * @param isDoctor true if doctor, false if patient
     * @return List of achievements with progress
     */
    public List<Achievement> getUserAchievements(int userId, boolean isDoctor) {
        List<Achievement> achievements = new ArrayList<>();
        
        try {
            if (isDoctor) {
                List<Reponse> responses = reponseService.getByMedecin(userId);
                int totalLikes = responses.stream().mapToInt(Reponse::getLikes).sum();
                
                // Response milestones
                achievements.add(new Achievement(
                    "Première Réponse", "Répondre à votre première question",
                    "🎯", responses.size() >= 1, responses.size(), 1
                ));
                achievements.add(new Achievement(
                    "10 Réponses", "Répondre à 10 questions",
                    "📚", responses.size() >= 10, responses.size(), 10
                ));
                achievements.add(new Achievement(
                    "50 Réponses", "Répondre à 50 questions",
                    "⭐", responses.size() >= 50, responses.size(), 50
                ));
                achievements.add(new Achievement(
                    "100 Réponses", "Répondre à 100 questions",
                    "🏆", responses.size() >= 100, responses.size(), 100
                ));
                
                // Likes milestones
                achievements.add(new Achievement(
                    "Premier J'aime", "Recevoir votre premier j'aime",
                    "❤️", totalLikes >= 1, totalLikes, 1
                ));
                achievements.add(new Achievement(
                    "50 J'aimes", "Recevoir 50 j'aimes",
                    "💕", totalLikes >= 50, totalLikes, 50
                ));
                achievements.add(new Achievement(
                    "100 J'aimes", "Recevoir 100 j'aimes",
                    "💖", totalLikes >= 100, totalLikes, 100
                ));
                
            } else {
                List<Question> questions = questionService.getByPatient(userId);
                int totalLikes = questions.stream().mapToInt(Question::getLikes).sum();
                int totalAnswers = questions.stream().mapToInt(Question::getAnswerCount).sum();
                
                // Question milestones
                achievements.add(new Achievement(
                    "Première Question", "Poser votre première question",
                    "🎯", questions.size() >= 1, questions.size(), 1
                ));
                achievements.add(new Achievement(
                    "5 Questions", "Poser 5 questions",
                    "📝", questions.size() >= 5, questions.size(), 5
                ));
                achievements.add(new Achievement(
                    "15 Questions", "Poser 15 questions",
                    "✨", questions.size() >= 15, questions.size(), 15
                ));
                achievements.add(new Achievement(
                    "30 Questions", "Poser 30 questions",
                    "⭐", questions.size() >= 30, questions.size(), 30
                ));
                
                // Engagement milestones
                achievements.add(new Achievement(
                    "Première Réponse Reçue", "Recevoir une réponse à votre question",
                    "💬", totalAnswers >= 1, totalAnswers, 1
                ));
                achievements.add(new Achievement(
                    "10 Réponses Reçues", "Recevoir 10 réponses",
                    "💭", totalAnswers >= 10, totalAnswers, 10
                ));
                
                // Likes milestones
                achievements.add(new Achievement(
                    "Question Appréciée", "Recevoir un j'aime sur une question",
                    "👍", totalLikes >= 1, totalLikes, 1
                ));
                achievements.add(new Achievement(
                    "Questions Populaires", "Recevoir 20 j'aimes",
                    "🌟", totalLikes >= 20, totalLikes, 20
                ));
            }
            
        } catch (Exception e) {
            System.err.println("Error getting achievements: " + e.getMessage());
        }
        
        return achievements;
    }
    
    // ═══════════════════════════════════════════════
    //  UTILITY METHODS
    // ═══════════════════════════════════════════════
    
    /**
     * Get user level based on reputation
     * @param reputation Reputation score
     * @return Level number
     */
    public int getLevel(int reputation) {
        if (reputation >= 1000) return 10;
        if (reputation >= 800) return 9;
        if (reputation >= 600) return 8;
        if (reputation >= 450) return 7;
        if (reputation >= 350) return 6;
        if (reputation >= 250) return 5;
        if (reputation >= 150) return 4;
        if (reputation >= 80) return 3;
        if (reputation >= 30) return 2;
        return 1;
    }
    
    /**
     * Get reputation needed for next level
     * @param currentReputation Current reputation
     * @return Reputation needed for next level
     */
    public int getReputationForNextLevel(int currentReputation) {
        int[] levels = {0, 30, 80, 150, 250, 350, 450, 600, 800, 1000};
        for (int levelRep : levels) {
            if (currentReputation < levelRep) {
                return levelRep - currentReputation;
            }
        }
        return 0; // Max level reached
    }
    
    /**
     * Get rank title based on reputation
     * @param reputation Reputation score
     * @return Rank title
     */
    public String getRankTitle(int reputation) {
        if (reputation >= 1000) return "Légende";
        if (reputation >= 800) return "Maître";
        if (reputation >= 600) return "Expert";
        if (reputation >= 450) return "Avancé";
        if (reputation >= 350) return "Intermédiaire";
        if (reputation >= 250) return "Confirmé";
        if (reputation >= 150) return "Apprenti";
        if (reputation >= 80) return "Débutant";
        if (reputation >= 30) return "Novice";
        return "Nouveau";
    }
}
