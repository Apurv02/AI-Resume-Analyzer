package com.apurv.resumeanalyzer.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class PdfTextService {
    public String extractText(MultipartFile file) throws IOException {
        try (var document = Loader.loadPDF(file.getBytes())) {
            String text = new PDFTextStripper().getText(document);
            if (text == null || text.trim().length() < 40) {
                throw new IllegalArgumentException("This PDF contains too little selectable text. Please upload a text-based PDF; scanned image PDFs are not supported.");
            }
            return text.length() > 18000 ? text.substring(0, 18000) : text;
        }
    }
}
