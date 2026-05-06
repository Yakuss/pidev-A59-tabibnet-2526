// ═══════════════════════════════════════════════════════════════════════════
// 📋 كود جاهز للنسخ واللصق - ميزات الذكاء الاصطناعي في المنتدى
// ═══════════════════════════════════════════════════════════════════════════

// ═══════════════════════════════════════════════════════════════════════════
// 1️⃣ إضافة زر الترجمة في بطاقة السؤال
// ═══════════════════════════════════════════════════════════════════════════
// في ForumController.java - داخل createQuestionCard() - بعد actionButtons

// Import needed
import com.pidev.services.GeminiAIService;
import javafx.application.Platform;

// Add translate button
Button translateBtn = new Button("🌐 ترجمة");
translateBtn.setStyle(
    "-fx-background-color: #3b82f6;" +
    "-fx-text-fill: white;" +
    "-fx-font-size: 12px;" +
    "-fx-font-weight: 600;" +
    "-fx-padding: 6 12;" +
    "-fx-background-radius: 6;" +
    "-fx-cursor: hand;"
);

translateBtn.setOnAction(e -> {
    translateBtn.setDisable(true);
    translateBtn.setText("⏳ جاري الترجمة...");
    
    new Thread(() -> {
        try {
            GeminiAIService aiService = new GeminiAIService();
            
            // Translate title
            String translatedTitle = aiService.autoTranslate(q.getTitre());
            
            // Translate description
            String translatedDesc = aiService.autoTranslate(q.getDescription());
            
            Platform.runLater(() -> {
                titleLabel.setText(translatedTitle);
                descLabel.setText(translatedDesc);
                translateBtn.setText("✓ مترجم");
                showStatus("✓ تمت الترجمة بنجاح", "#22c55e");
            });
            
        } catch (Exception ex) {
            Platform.runLater(() -> {
                translateBtn.setDisable(false);
                translateBtn.setText("🌐 ترجمة");
                showAlert(Alert.AlertType.ERROR, "خطأ", 
                    "فشلت الترجمة: " + ex.getMessage());
            });
        }
    }).start();
});

actionButtons.getChildren().add(translateBtn);


// ═══════════════════════════════════════════════════════════════════════════
// 2️⃣ إضافة رفع الصور في نموذج إضافة سؤال
// ═══════════════════════════════════════════════════════════════════════════
// في ForumController.java - داخل openAddDialog() - بعد descArea

// Import needed
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.json.JSONObject;

// Image upload section
Label imageLabel = createFormLabel("📸 صورة طبية (اختياري)");

HBox imageBox = new HBox(10);
imageBox.setAlignment(Pos.CENTER_LEFT);

Label selectedImageLabel = new Label("لم يتم اختيار صورة");
selectedImageLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

Button chooseImageBtn = new Button("اختر صورة");
chooseImageBtn.setStyle(
    "-fx-background-color: #8b5cf6;" +
    "-fx-text-fill: white;" +
    "-fx-padding: 8 16;" +
    "-fx-background-radius: 6;" +
    "-fx-cursor: hand;"
);

File[] selectedImage = new File[1]; // Array to hold selected file

chooseImageBtn.setOnAction(e -> {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("اختر صورة طبية");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("صور", "*.png", "*.jpg", "*.jpeg", "*.gif"),
        new FileChooser.ExtensionFilter("جميع الملفات", "*.*")
    );
    
    File file = fileChooser.showOpenDialog(dialog.getOwner());
    if (file != null) {
        // Check file size (max 5MB)
        long fileSizeInMB = file.length() / (1024 * 1024);
        if (fileSizeInMB > 5) {
            showAlert(Alert.AlertType.WARNING, "تنبيه", 
                "حجم الصورة كبير جداً! الحد الأقصى 5 ميجابايت.");
            return;
        }
        
        selectedImage[0] = file;
        selectedImageLabel.setText("✓ " + file.getName());
        selectedImageLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 12px; -fx-font-weight: 600;");
    }
});

imageBox.getChildren().addAll(chooseImageBtn, selectedImageLabel);

// AI Analysis button
Button analyzeImageBtn = new Button("✨ توليد تلقائي من الصورة");
analyzeImageBtn.setStyle(
    "-fx-background-color: #f59e0b;" +
    "-fx-text-fill: white;" +
    "-fx-padding: 8 16;" +
    "-fx-background-radius: 6;" +
    "-fx-cursor: hand;" +
    "-fx-font-weight: 600;"
);

analyzeImageBtn.setOnAction(e -> {
    if (selectedImage[0] == null) {
        showAlert(Alert.AlertType.WARNING, "تنبيه", 
            "يرجى اختيار صورة أولاً!");
        return;
    }
    
    analyzeImageBtn.setDisable(true);
    analyzeImageBtn.setText("⏳ جاري التحليل...");
    
    new Thread(() -> {
        try {
            GeminiAIService aiService = new GeminiAIService();
            
            if (!aiService.isConfigured()) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "خطأ", 
                        "مفتاح API غير مُعد! يرجى إضافة مفتاح Gemini API في GeminiAIService.java");
                    analyzeImageBtn.setDisable(false);
                    analyzeImageBtn.setText("✨ توليد تلقائي من الصورة");
                });
                return;
            }
            
            JSONObject result = aiService.analyzeImage(selectedImage[0]);
            
            Platform.runLater(() -> {
                titreField.setText(result.getString("title"));
                descArea.setText(result.getString("description"));
                
                analyzeImageBtn.setDisable(false);
                analyzeImageBtn.setText("✓ تم التحليل");
                analyzeImageBtn.setStyle(
                    "-fx-background-color: #22c55e;" +
                    "-fx-text-fill: white;" +
                    "-fx-padding: 8 16;" +
                    "-fx-background-radius: 6;" +
                    "-fx-font-weight: 600;"
                );
                
                showStatus("✓ تم توليد العنوان والوصف تلقائياً", "#22c55e");
            });
            
        } catch (Exception ex) {
            Platform.runLater(() -> {
                analyzeImageBtn.setDisable(false);
                analyzeImageBtn.setText("✨ توليد تلقائي من الصورة");
                showAlert(Alert.AlertType.ERROR, "خطأ", 
                    "فشل تحليل الصورة: " + ex.getMessage());
                ex.printStackTrace();
            });
        }
    }).start();
});

// Add to form
form.getChildren().addAll(imageLabel, imageBox, analyzeImageBtn);


// ═══════════════════════════════════════════════════════════════════════════
// 3️⃣ حفظ الصورة عند إضافة السؤال
// ═══════════════════════════════════════════════════════════════════════════
// في openAddDialog() - داخل زر "إضافة" - بعد التحقق من الحقول

String imageName = null;

// Save image if selected
if (selectedImage[0] != null) {
    try {
        // Create upload directory if not exists
        File uploadDir = new File("uploads/forum_images");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
            System.out.println("✅ Created directory: " + uploadDir.getAbsolutePath());
        }
        
        // Generate unique filename
        String timestamp = String.valueOf(System.currentTimeMillis());
        String extension = selectedImage[0].getName()
            .substring(selectedImage[0].getName().lastIndexOf("."));
        imageName = "question_" + timestamp + extension;
        
        // Copy image to upload directory
        File destFile = new File(uploadDir, imageName);
        Files.copy(selectedImage[0].toPath(), destFile.toPath(), 
                   StandardCopyOption.REPLACE_EXISTING);
        
        System.out.println("✅ Image saved: " + imageName);
        
    } catch (Exception ex) {
        showAlert(Alert.AlertType.ERROR, "خطأ", 
            "فشل حفظ الصورة: " + ex.getMessage());
        ex.printStackTrace();
        return;
    }
}

// Create question with image
Question newQ = new Question();
newQ.setTitre(titre);
newQ.setDescription(desc);
newQ.setSpecialiteId(selectedSpecId);
newQ.setPatientId(UserSession.getInstance().getUser().getId());
newQ.setImageName(imageName); // Save image name

questionService.ajouter(newQ);


// ═══════════════════════════════════════════════════════════════════════════
// 4️⃣ عرض الصورة في بطاقة السؤال
// ═══════════════════════════════════════════════════════════════════════════
// في createQuestionCard() - بعد descLabel

// Import needed
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// Display image if exists
if (q.getImageName() != null && !q.getImageName().isEmpty()) {
    try {
        File imageFile = new File("uploads/forum_images/" + q.getImageName());
        if (imageFile.exists()) {
            Image image = new Image(imageFile.toURI().toString());
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(500);
            imageView.setPreserveRatio(true);
            imageView.setStyle(
                "-fx-background-radius: 8;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);"
            );
            
            // Add spacing
            VBox.setMargin(imageView, new Insets(10, 0, 0, 0));
            
            // Add image to card
            card.getChildren().add(card.getChildren().size() - 1, imageView);
            
            // Make image clickable for full view
            imageView.setOnMouseClicked(e -> {
                // Open image in full size dialog
                Stage imageStage = new Stage();
                imageStage.setTitle("عرض الصورة");
                
                ImageView fullImageView = new ImageView(image);
                fullImageView.setPreserveRatio(true);
                fullImageView.setFitWidth(800);
                
                ScrollPane scrollPane = new ScrollPane(fullImageView);
                scrollPane.setStyle("-fx-background: #0e1220;");
                
                Scene scene = new Scene(scrollPane, 850, 600);
                imageStage.setScene(scene);
                imageStage.show();
            });
            
            imageView.setStyle(imageView.getStyle() + "-fx-cursor: hand;");
        }
    } catch (Exception e) {
        System.err.println("Failed to load image: " + e.getMessage());
    }
}


// ═══════════════════════════════════════════════════════════════════════════
// 5️⃣ عرض الصورة في صفحة تفاصيل السؤال
// ═══════════════════════════════════════════════════════════════════════════
// في openDetailDialog() - بعد descriptionLabel

// Display image in detail view
if (q.getImageName() != null && !q.getImageName().isEmpty()) {
    try {
        File imageFile = new File("uploads/forum_images/" + q.getImageName());
        if (imageFile.exists()) {
            Label imageLabel = new Label("📸 الصورة المرفقة:");
            imageLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px; -fx-font-weight: 600;");
            
            Image image = new Image(imageFile.toURI().toString());
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(600);
            imageView.setPreserveRatio(true);
            imageView.setStyle(
                "-fx-background-radius: 12;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 12, 0, 0, 4);"
            );
            
            VBox imageContainer = new VBox(10);
            imageContainer.getChildren().addAll(imageLabel, imageView);
            VBox.setMargin(imageContainer, new Insets(15, 0, 15, 0));
            
            // Add after description
            content.getChildren().add(3, imageContainer);
        }
    } catch (Exception e) {
        System.err.println("Failed to load image in detail view: " + e.getMessage());
    }
}


// ═══════════════════════════════════════════════════════════════════════════
// 6️⃣ إضافة زر ترجمة في صفحة التفاصيل
// ═══════════════════════════════════════════════════════════════════════════
// في openDetailDialog() - في actionButtons

Button translateDetailBtn = new Button("🌐 ترجمة السؤال");
translateDetailBtn.setStyle(
    "-fx-background-color: #3b82f6;" +
    "-fx-text-fill: white;" +
    "-fx-padding: 10 20;" +
    "-fx-background-radius: 8;" +
    "-fx-cursor: hand;" +
    "-fx-font-weight: 600;"
);

translateDetailBtn.setOnAction(e -> {
    translateDetailBtn.setDisable(true);
    translateDetailBtn.setText("⏳ جاري الترجمة...");
    
    new Thread(() -> {
        try {
            GeminiAIService aiService = new GeminiAIService();
            
            String translatedTitle = aiService.autoTranslate(q.getTitre());
            String translatedDesc = aiService.autoTranslate(q.getDescription());
            
            Platform.runLater(() -> {
                titleLabel.setText(translatedTitle);
                descriptionLabel.setText(translatedDesc);
                translateDetailBtn.setText("✓ مترجم");
            });
            
        } catch (Exception ex) {
            Platform.runLater(() -> {
                translateDetailBtn.setDisable(false);
                translateDetailBtn.setText("🌐 ترجمة السؤال");
                showAlert(Alert.AlertType.ERROR, "خطأ", 
                    "فشلت الترجمة: " + ex.getMessage());
            });
        }
    }).start();
});

actionButtons.getChildren().add(translateDetailBtn);


// ═══════════════════════════════════════════════════════════════════════════
// 7️⃣ إضافة زر ترجمة للأجوبة
// ═══════════════════════════════════════════════════════════════════════════
// في createResponseBubble() - في actionButtons

Button translateResponseBtn = new Button("🌐");
translateResponseBtn.setStyle(
    "-fx-background-color: transparent;" +
    "-fx-text-fill: #3b82f6;" +
    "-fx-font-size: 14px;" +
    "-fx-cursor: hand;" +
    "-fx-padding: 4 8;"
);
translateResponseBtn.setTooltip(new Tooltip("ترجمة الإجابة"));

translateResponseBtn.setOnAction(e -> {
    translateResponseBtn.setDisable(true);
    
    new Thread(() -> {
        try {
            GeminiAIService aiService = new GeminiAIService();
            String translatedText = aiService.autoTranslate(r.getTexte());
            
            Platform.runLater(() -> {
                responseText.setText(translatedText);
                translateResponseBtn.setText("✓");
            });
            
        } catch (Exception ex) {
            Platform.runLater(() -> {
                translateResponseBtn.setDisable(false);
                showAlert(Alert.AlertType.ERROR, "خطأ", 
                    "فشلت الترجمة: " + ex.getMessage());
            });
        }
    }).start();
});

actionButtons.getChildren().add(translateResponseBtn);


// ═══════════════════════════════════════════════════════════════════════════
// 📝 ملاحظات مهمة
// ═══════════════════════════════════════════════════════════════════════════

/*
1. تأكد من إضافة مفتاح Gemini API في GeminiAIService.java:
   private static final String GEMINI_API_KEY = "YOUR_KEY_HERE";

2. أضف مكتبة JSON في pom.xml:
   <dependency>
       <groupId>org.json</groupId>
       <artifactId>json</artifactId>
       <version>20231013</version>
   </dependency>

3. أنشئ مجلد uploads/forum_images في مجلد المشروع

4. تأكد من استيراد الحزم المطلوبة:
   import com.pidev.services.GeminiAIService;
   import javafx.application.Platform;
   import javafx.stage.FileChooser;
   import javafx.scene.image.Image;
   import javafx.scene.image.ImageView;
   import org.json.JSONObject;
   import java.io.File;
   import java.nio.file.Files;
   import java.nio.file.StandardCopyOption;

5. اختبر كل ميزة على حدة قبل دمجها كلها

6. الترجمة والتحليل يستغرقان 3-10 ثواني، لذلك استخدم Thread منفصل

7. تأكد من معالجة الأخطاء بشكل صحيح

8. أضف رسائل تحميل واضحة للمستخدم
*/
