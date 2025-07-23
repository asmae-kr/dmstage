package com.demande.dmstage.services;

import com.demande.dmstage.entities.DemandeStage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public byte[] exportDemandesToExcel(List<DemandeStage> demandes) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Demandes");

            // Créer l'en-tête
            Row headerRow = sheet.createRow(0);
            String[] columns = {
                "ID", "Nom", "Prénom", "Sexe", "Email", "Téléphone", "CIN", "Adresse Domicile",
                "Type Stage", "Date Début", "Durée", "Statut", "Date Demande"
            };

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // Remplir les données
            int rowNum = 1;
            for (DemandeStage demande : demandes) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(demande.getId());
                row.createCell(1).setCellValue(demande.getNom());
                row.createCell(2).setCellValue(demande.getPrenom());
                row.createCell(3).setCellValue(demande.getSexe() != null ? demande.getSexe() : "");
                row.createCell(4).setCellValue(demande.getEmail());
                row.createCell(5).setCellValue(demande.getTelephone());
                row.createCell(6).setCellValue(demande.getCin());
                row.createCell(7).setCellValue(demande.getAdresseDomicile());

                // Convertir typeStage en String (en cas d'enum)
                String typeStageStr = demande.getTypeStage() != null ? demande.getTypeStage().toString() : "";
                row.createCell(8).setCellValue(typeStageStr);

                row.createCell(9).setCellValue(
                    demande.getDateDebut() != null ? demande.getDateDebut().format(DATE_FORMATTER) : ""
                );

                row.createCell(10).setCellValue(demande.getDuree());

                // Convertir statut en String (en cas d'enum)
                String statutStr = demande.getStatut() != null ? demande.getStatut().toString() : "";
                row.createCell(11).setCellValue(statutStr);

                row.createCell(12).setCellValue(
                    demande.getDateDemande() != null ? demande.getDateDemande().format(DATE_FORMATTER) : ""
                );
            }

            // Ajuster la largeur des colonnes
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Exporter en bytes
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
