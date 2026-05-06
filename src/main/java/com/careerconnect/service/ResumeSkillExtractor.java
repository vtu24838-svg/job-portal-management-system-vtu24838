package com.careerconnect.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Extracts skill keywords from uploaded PDF or DOCX resume files.
 * Uses a predefined technology/skill dictionary and phrase matching.
 */
@Slf4j
@Component
public class ResumeSkillExtractor {

    /** Master skill dictionary — sorted by length desc so longer phrases match first */
    private static final List<String> SKILL_DICTIONARY = List.of(
        // Languages
        "Java", "Python", "JavaScript", "TypeScript", "C++", "C#", "Kotlin", "Swift",
        "Go", "Ruby", "Rust", "Scala", "PHP", "R", "MATLAB",
        // Frontend
        "React", "Angular", "Vue.js", "Vue", "HTML", "CSS", "Bootstrap",
        "Tailwind CSS", "jQuery", "Next.js", "Nuxt.js",
        // Backend
        "Spring Boot", "Spring", "Node.js", "Express.js", "Django", "Flask",
        "FastAPI", "Laravel", "Ruby on Rails", "ASP.NET", ".NET",
        // Databases
        "MySQL", "PostgreSQL", "MongoDB", "Redis", "Oracle", "SQL Server",
        "SQLite", "Cassandra", "DynamoDB", "Firebase",
        // Cloud & DevOps
        "AWS", "Azure", "GCP", "Docker", "Kubernetes", "Jenkins", "CI/CD",
        "GitHub Actions", "Terraform", "Ansible", "Linux",
        // Data & AI
        "Machine Learning", "Deep Learning", "TensorFlow", "PyTorch", "Scikit-learn",
        "Pandas", "NumPy", "Data Analysis", "NLP", "Computer Vision",
        // Tools
        "Git", "GitHub", "Jira", "Maven", "Gradle", "Postman",
        "REST API", "GraphQL", "Microservices", "Agile", "Scrum",
        // Other
        "Android", "iOS", "Flutter", "React Native",
        "Hadoop", "Spark", "Kafka", "Elasticsearch"
    );

    /**
     * Extracts text from a PDF or DOCX file and returns matched skills.
     *
     * @param file uploaded resume file (PDF or DOCX)
     * @return comma-separated string of matched skills, empty if none found
     */
    public String extractSkills(MultipartFile file) {
        if (file == null || file.isEmpty()) return "";

        String fileName = file.getOriginalFilename();
        if (fileName == null) return "";

        try {
            String rawText;
            if (fileName.toLowerCase().endsWith(".pdf")) {
                rawText = extractFromPdf(file.getInputStream());
            } else if (fileName.toLowerCase().endsWith(".docx")) {
                rawText = extractFromDocx(file.getInputStream());
            } else {
                return "";
            }
            return matchSkills(rawText);
        } catch (IOException e) {
            log.warn("Could not extract skills from resume: {}", e.getMessage());
            return "";
        }
    }

    /** Extract raw text from PDF using Apache PDFBox 3.x */
    private String extractFromPdf(InputStream is) throws IOException {
        // PDFBox 3.x uses Loader.loadPDF() instead of PDDocument.load()
        try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(is.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    /** Extract raw text from DOCX using Apache POI */
    private String extractFromDocx(InputStream is) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(is)) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph p : doc.getParagraphs()) {
                sb.append(p.getText()).append(" ");
            }
            return sb.toString();
        }
    }

    /** Match dictionary skills against extracted text (case-insensitive) */
    private String matchSkills(String text) {
        if (text == null || text.isBlank()) return "";
        String lowerText = text.toLowerCase();
        Set<String> found = new LinkedHashSet<>();
        for (String skill : SKILL_DICTIONARY) {
            if (lowerText.contains(skill.toLowerCase())) {
                found.add(skill);
            }
        }
        return String.join(", ", found);
    }
}
