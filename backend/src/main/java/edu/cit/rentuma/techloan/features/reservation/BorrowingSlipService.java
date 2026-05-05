package edu.cit.rentuma.techloan.features.reservation;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import edu.cit.rentuma.techloan.features.auth.repository.UserRepository;
import edu.cit.rentuma.techloan.features.reservation.model.BorrowRequest;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class BorrowingSlipService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private final QrCodeService qrCodeService;
    private final UserRepository userRepository;

    public BorrowingSlipService(QrCodeService qrCodeService, UserRepository userRepository) {
        this.qrCodeService  = qrCodeService;
        this.userRepository = userRepository;
    }

    public byte[] generateSlip(BorrowRequest request) {
        String borrowerName = userRepository.findById(request.getUserId())
                .map(u -> u.getFullName())
                .orElse(request.getUserEmail());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A5);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont  = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(139, 0, 0));
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.DARK_GRAY);
            Font labelFont  = new Font(Font.HELVETICA, 9,  Font.BOLD, Color.GRAY);
            Font valueFont  = new Font(Font.HELVETICA, 9,  Font.NORMAL, Color.BLACK);

            Paragraph title = new Paragraph("TechLoan — Borrowing Slip", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            Paragraph sub = new Paragraph("CIT-U Lab Equipment Borrowing System", new Font(Font.HELVETICA, 8, Font.NORMAL, Color.GRAY));
            sub.setAlignment(Element.ALIGN_CENTER);
            doc.add(sub);

            doc.add(new Paragraph(" "));
            addDivider(doc);
            doc.add(new Paragraph(" "));

            addRow(doc, "Reservation ID", "#" + request.getId(), labelFont, valueFont);
            addRow(doc, "Status", request.getStatus().name(), labelFont, valueFont);

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Borrower Information", headerFont));
            doc.add(new Paragraph(" "));

            addRow(doc, "Name",  borrowerName,         labelFont, valueFont);
            addRow(doc, "Email", request.getUserEmail(), labelFont, valueFont);

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Item Details", headerFont));
            doc.add(new Paragraph(" "));

            addRow(doc, "Item",        request.getItemName(),                     labelFont, valueFont);
            addRow(doc, "Quantity",    String.valueOf(request.getQuantity()),      labelFont, valueFont);
            addRow(doc, "Purpose",     request.getPurpose() != null ? request.getPurpose() : "—", labelFont, valueFont);
            addRow(doc, "Borrow Date", request.getBorrowDate() != null
                    ? request.getBorrowDate().format(DATE_FMT) : "—",             labelFont, valueFont);
            addRow(doc, "Return Date", request.getReturnDate() != null
                    ? request.getReturnDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "—",
                    labelFont, valueFont);

            doc.add(new Paragraph(" "));
            addDivider(doc);
            doc.add(new Paragraph(" "));

            byte[] qrBytes = qrCodeService.generateQrPng("TECHLOAN-RESERVATION-" + request.getId(), 150);
            Image qr = Image.getInstance(qrBytes);
            qr.setAlignment(Element.ALIGN_CENTER);
            qr.scaleAbsolute(120, 120);
            doc.add(qr);

            Paragraph qrNote = new Paragraph("Show this QR code to the custodian", new Font(Font.HELVETICA, 7, Font.ITALIC, Color.GRAY));
            qrNote.setAlignment(Element.ALIGN_CENTER);
            doc.add(qrNote);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate borrowing slip: " + e.getMessage(), e);
        }
    }

    private void addRow(Document doc, String label, String value, Font labelFont, Font valueFont) throws DocumentException {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + ": ", labelFont));
        p.add(new Chunk(value, valueFont));
        doc.add(p);
    }

    private void addDivider(Document doc) throws DocumentException {
        Paragraph divider = new Paragraph("────────────────────────────────────────", new Font(Font.HELVETICA, 8, Font.NORMAL, Color.LIGHT_GRAY));
        divider.setAlignment(Element.ALIGN_CENTER);
        doc.add(divider);
    }
}
