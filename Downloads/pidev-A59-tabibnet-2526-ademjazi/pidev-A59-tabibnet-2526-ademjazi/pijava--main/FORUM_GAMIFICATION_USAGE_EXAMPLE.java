// ═══════════════════════════════════════════════════════════════════════════
// 📋 أمثلة على استخدام نظام التحفيز في ForumController
// ═══════════════════════════════════════════════════════════════════════════

// ═══════════════════════════════════════════════════════════════════════════
// 1️⃣ إضافة الشارات في بطاقة السؤال
// ═══════════════════════════════════════════════════════════════════════════

// في ForumController - داخل createQuestionCard() - بعد authorLabel

ForumGamificationService gamification = new ForumGamificationService();
List<String> badges = gamification.getUserBadges(q.getPatientId(), false);

// عرض أول شارة إذا وجدت
if (!badges.isEmpty()) {
    Label badgeLabel = new Label(badges.get(0));
    badgeLabel.setStyle(
        "-fx-text-fill: #f59e0b;" +
        "-fx-font-size: 10px;" +
        "-fx-font-weight: 600;" +
        "-fx-padding: 2 6;" +
        "-fx-background-color: rgba(245,158,11,0.1);" +
        "-fx-background-radius: 10;"
    );
    footer.getChildren().add(2, badgeLabel); // أضف بعد الاسم
}

// ═══════════════════════════════════════════════════════════════════════════
// 2️⃣ عرض نقاط السمعة والمستوى في صفحة التفاصيل
// ═══════════════════════════════════════════════════════════════════════════

// في ForumController - داخل openDetailDialog() - بعد authorLabel

ForumGamificationService gamification = new ForumGamificationService();
int reputation = gamification.calculateReputation(q.getPatientId(), false);
int level = gamification.getLevel(reputation);
String rank = gamification.getRankTitle(reputation);

Label reputationLabel = new Label(
    String.format("⭐ Niveau %d - %s (%d points)", level, rank, reputation)
);
reputationLabel.setStyle(
    "-fx-text-fill: #818cf8;" +
    "-fx-font-size: 11px;" +
    "-fx-font-weight: 600;"
);
metaBox.getChildren().add(reputationLabel);

// ═══════════════════════════════════════════════════════════════════════════
// 3️⃣ إضافة زر لوحة المتصدرين في القائمة الرئيسية
// ═══════════════════════════════════════════════════════════════════════════

// في ForumController - في initialize() أو في القائمة العلوية

@FXML
public void openLeaderboard() {
    Dialog<Void> dialog = new Dialog<>();
    dialog.setTitle("🏆 Leaderboard");
    dialog.setHeaderText("Top Contributors");
    
    DialogPane pane = dialog.getDialogPane();
    pane.setStyle("-fx-background-color: #0e1220;");
    pane.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
    pane.getButtonTypes().add(ButtonType.CLOSE);
    
    VBox content = new VBox(20);
    content.setPadding(new Insets(20));
    content.setPrefWidth(600);
    
    ForumGamificationService gamification = new ForumGamificationService();
    
    // Top Doctors Section
    Label doctorsTitle = new Label("🏆 Top Doctors");
    doctorsTitle.setStyle("-fx-text-fill: #f1f5f9; -fx-font-size: 18px; -fx-font-weight: 700;");
    
    VBox doctorsList = new VBox(10);
    List<ForumGamificationService.UserStats> topDoctors = gamification.getTopDoctors(5);
    
    for (int i = 0; i < topDoctors.size(); i++) {
        ForumGamificationService.UserStats stats = topDoctors.get(i);
        HBox userCard = createLeaderboardCard(i + 1, stats);
        doctorsList.getChildren().add(userCard);
    }
    
    // Top Patients Section
    Label patientsTitle = new Label("🌟 Top Patients");
    patientsTitle.setStyle("-fx-text-fill: #f1f5f9; -fx-font-size: 18px; -fx-font-weight: 700;");
    
    VBox patientsList = new VBox(10);
    List<ForumGamificationService.UserStats> topPatients = gamification.getTopPatients(5);
    
    for (int i = 0; i < topPatients.size(); i++) {
        ForumGamificationService.UserStats stats = topPatients.get(i);
        HBox userCard = createLeaderboardCard(i + 1, stats);
        patientsList.getChildren().add(userCard);
    }
    
    content.getChildren().addAll(doctorsTitle, doctorsList, patientsTitle, patientsList);
    pane.setContent(content);
    dialog.showAndWait();
}

// Helper method لإنشاء بطاقة في لوحة المتصدرين
private HBox createLeaderboardCard(int rank, ForumGamificationService.UserStats stats) {
    HBox card = new HBox(15);
    card.setAlignment(Pos.CENTER_LEFT);
    card.setPadding(new Insets(12, 16, 12, 16));
    card.setStyle(
        "-fx-background-color: #1c2133;" +
        "-fx-background-radius: 8;" +
        "-fx-border-color: #252d42;" +
        "-fx-border-width: 1;" +
        "-fx-border-radius: 8;"
    );
    
    // Rank
    Label rankLabel = new Label("#" + rank);
    rankLabel.setStyle(
        "-fx-text-fill: " + (rank == 1 ? "#f59e0b" : rank == 2 ? "#94a3b8" : "#64748b") + ";" +
        "-fx-font-size: 18px;" +
        "-fx-font-weight: 700;" +
        "-fx-min-width: 40;"
    );
    
    // User info
    VBox userInfo = new VBox(4);
    Label nameLabel = new Label(stats.userName);
    nameLabel.setStyle("-fx-text-fill: #f1f5f9; -fx-font-size: 14px; -fx-font-weight: 600;");
    
    Label statsLabel = new Label(
        String.format("%d points • %d contributions • %d likes", 
                     stats.reputation, stats.contributions, stats.likes)
    );
    statsLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
    
    userInfo.getChildren().addAll(nameLabel, statsLabel);
    HBox.setHgrow(userInfo, Priority.ALWAYS);
    
    // Badges
    HBox badgesBox = new HBox(5);
    for (String badge : stats.badges.stream().limit(3).collect(Collectors.toList())) {
        Label badgeLabel = new Label(badge.split(" ")[0]); // Just the emoji
        badgeLabel.setStyle("-fx-font-size: 16px;");
        badgeLabel.setTooltip(new Tooltip(badge));
        badgesBox.getChildren().add(badgeLabel);
    }
    
    card.getChildren().addAll(rankLabel, userInfo, badgesBox);
    return card;
}

// ═══════════════════════════════════════════════════════════════════════════
// 4️⃣ إضافة نافذة الإنجازات
// ═══════════════════════════════════════════════════════════════════════════

@FXML
public void openAchievements() {
    // Get current user
    com.pidev.models.BaseUser user = com.pidev.utils.UserSession.getInstance().getUser();
    if (user == null) return;
    
    boolean isDoctor = user.getRole().equals("MEDECIN");
    
    Dialog<Void> dialog = new Dialog<>();
    dialog.setTitle("🎯 Achievements");
    dialog.setHeaderText("Your Achievements");
    
    DialogPane pane = dialog.getDialogPane();
    pane.setStyle("-fx-background-color: #0e1220;");
    pane.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
    pane.getButtonTypes().add(ButtonType.CLOSE);
    
    VBox content = new VBox(15);
    content.setPadding(new Insets(20));
    content.setPrefWidth(500);
    
    ForumGamificationService gamification = new ForumGamificationService();
    List<ForumGamificationService.Achievement> achievements = 
        gamification.getUserAchievements(user.getId(), isDoctor);
    
    for (ForumGamificationService.Achievement achievement : achievements) {
        VBox achievementCard = createAchievementCard(achievement);
        content.getChildren().add(achievementCard);
    }
    
    pane.setContent(content);
    dialog.showAndWait();
}

// Helper method لإنشاء بطاقة إنجاز
private VBox createAchievementCard(ForumGamificationService.Achievement achievement) {
    VBox card = new VBox(8);
    card.setPadding(new Insets(12, 16, 12, 16));
    card.setStyle(
        "-fx-background-color: " + (achievement.unlocked ? "#1c2133" : "#0e1220") + ";" +
        "-fx-background-radius: 8;" +
        "-fx-border-color: " + (achievement.unlocked ? "#22c55e" : "#252d42") + ";" +
        "-fx-border-width: 1;" +
        "-fx-border-radius: 8;" +
        "-fx-opacity: " + (achievement.unlocked ? "1.0" : "0.6") + ";"
    );
    
    // Header
    HBox header = new HBox(10);
    header.setAlignment(Pos.CENTER_LEFT);
    
    Label icon = new Label(achievement.icon);
    icon.setStyle("-fx-font-size: 24px;");
    
    VBox info = new VBox(2);
    Label name = new Label((achievement.unlocked ? "✅ " : "🔒 ") + achievement.name);
    name.setStyle(
        "-fx-text-fill: " + (achievement.unlocked ? "#22c55e" : "#94a3b8") + ";" +
        "-fx-font-size: 14px;" +
        "-fx-font-weight: 600;"
    );
    
    Label description = new Label(achievement.description);
    description.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
    description.setWrapText(true);
    
    info.getChildren().addAll(name, description);
    HBox.setHgrow(info, Priority.ALWAYS);
    
    header.getChildren().addAll(icon, info);
    
    // Progress bar (if not unlocked)
    if (!achievement.unlocked) {
        VBox progressBox = new VBox(4);
        
        Label progressLabel = new Label(
            String.format("Progress: %d / %d", achievement.progress, achievement.target)
        );
        progressLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px;");
        
        ProgressBar progressBar = new ProgressBar(
            (double) achievement.progress / achievement.target
        );
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setStyle(
            "-fx-accent: #5b6ef5;" +
            "-fx-control-inner-background: #252d42;"
        );
        
        progressBox.getChildren().addAll(progressLabel, progressBar);
        card.getChildren().addAll(header, progressBox);
    } else {
        card.getChildren().add(header);
    }
    
    return card;
}

// ═══════════════════════════════════════════════════════════════════════════
// 5️⃣ عرض معلومات المستخدم في الملف الشخصي
// ═══════════════════════════════════════════════════════════════════════════

@FXML
public void openUserProfile() {
    com.pidev.models.BaseUser user = com.pidev.utils.UserSession.getInstance().getUser();
    if (user == null) return;
    
    boolean isDoctor = user.getRole().equals("MEDECIN");
    ForumGamificationService gamification = new ForumGamificationService();
    
    // Calculate stats
    int reputation = gamification.calculateReputation(user.getId(), isDoctor);
    int level = gamification.getLevel(reputation);
    String rank = gamification.getRankTitle(reputation);
    int nextLevelRep = gamification.getReputationForNextLevel(reputation);
    List<String> badges = gamification.getUserBadges(user.getId(), isDoctor);
    
    Dialog<Void> dialog = new Dialog<>();
    dialog.setTitle("👤 Profile");
    dialog.setHeaderText(user.getNom() + " " + user.getPrenom());
    
    DialogPane pane = dialog.getDialogPane();
    pane.setStyle("-fx-background-color: #0e1220;");
    pane.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
    pane.getButtonTypes().add(ButtonType.CLOSE);
    
    VBox content = new VBox(20);
    content.setPadding(new Insets(20));
    content.setPrefWidth(400);
    
    // Level and Rank
    VBox levelBox = new VBox(8);
    Label levelLabel = new Label(String.format("⭐ Niveau %d - %s", level, rank));
    levelLabel.setStyle("-fx-text-fill: #f1f5f9; -fx-font-size: 18px; -fx-font-weight: 700;");
    
    Label repLabel = new Label(String.format("💎 %d points de réputation", reputation));
    repLabel.setStyle("-fx-text-fill: #818cf8; -fx-font-size: 14px;");
    
    levelBox.getChildren().addAll(levelLabel, repLabel);
    
    // Progress to next level
    if (nextLevelRep > 0) {
        VBox progressBox = new VBox(4);
        Label progressLabel = new Label(
            String.format("%d points pour le niveau suivant", nextLevelRep)
        );
        progressLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        
        ProgressBar progressBar = new ProgressBar(0.8); // Calculate actual progress
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: #5b6ef5;");
        
        progressBox.getChildren().addAll(progressLabel, progressBar);
        levelBox.getChildren().add(progressBox);
    }
    
    // Badges
    if (!badges.isEmpty()) {
        VBox badgesBox = new VBox(8);
        Label badgesTitle = new Label("🏅 Badges");
        badgesTitle.setStyle("-fx-text-fill: #f1f5f9; -fx-font-size: 16px; -fx-font-weight: 600;");
        
        FlowPane badgesFlow = new FlowPane(10, 10);
        for (String badge : badges) {
            Label badgeLabel = new Label(badge);
            badgeLabel.setStyle(
                "-fx-text-fill: #f59e0b;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: 600;" +
                "-fx-padding: 6 12;" +
                "-fx-background-color: rgba(245,158,11,0.1);" +
                "-fx-background-radius: 15;" +
                "-fx-border-color: rgba(245,158,11,0.3);" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 15;"
            );
            badgesFlow.getChildren().add(badgeLabel);
        }
        
        badgesBox.getChildren().addAll(badgesTitle, badgesFlow);
        content.getChildren().addAll(levelBox, badgesBox);
    } else {
        content.getChildren().add(levelBox);
    }
    
    pane.setContent(content);
    dialog.showAndWait();
}

// ═══════════════════════════════════════════════════════════════════════════
// 📝 ملاحظات الاستخدام
// ═══════════════════════════════════════════════════════════════════════════

/*
1. أضف هذه الطرق إلى ForumController.java

2. أضف أزرار في واجهة المستخدم:
   - زر "Leaderboard" في القائمة العلوية
   - زر "Achievements" في القائمة
   - زر "Profile" لعرض الملف الشخصي

3. استيراد الحزم المطلوبة:
   import com.pidev.services.ForumGamificationService;
   import java.util.stream.Collectors;
   import javafx.scene.control.ProgressBar;
   import javafx.scene.layout.FlowPane;

4. يمكنك تخصيص الألوان والأنماط حسب تصميمك

5. جميع الحسابات تلقائية - لا حاجة لتحديث يدوي
*/
