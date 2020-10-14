package pdfreader;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.text.*;
import org.apache.pdfbox.pdmodel.*;

public class main extends javax.swing.JFrame {

    public main() {
        initComponents();
        initForm();
    }

    private void initForm() {
        setLocationRelativeTo(null);
        setResizable(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnProcesar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Convertidor de PDF a formato IRP");

        btnProcesar.setText("Procesar");
        btnProcesar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnProcesarMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(113, 113, 113)
                .addComponent(btnProcesar, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(113, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(65, 65, 65)
                .addComponent(btnProcesar)
                .addContainerGap(68, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    enum TipoFact {
        CASE_USA,
        DESCONOCIDO
    }

    private String convert2Text(File file) throws IOException {
        PDDocument pdDoc = PDDocument.load(file);
        PDFTextStripper pdfStripper = new PDFLayoutTextStripper();

        return pdfStripper.getText(pdDoc);
    }

    private String removeSpaces(String s) {
        return s.replaceAll("\\s", "");
    }

    private TipoFact detect(String s) {
        TipoFact resultado = TipoFact.DESCONOCIDO;
        String[] text = s.split("\n");

        for (int i = 0; i < 50; i++) {
            String currText = removeSpaces(text[i]);
            if (currText.equals("ORDERNUMBERCUSTOMERP.O.ORDERTYPEORDERDATECARRIERMOT")) {
                resultado = TipoFact.CASE_USA;
                break;
            }
        }

        return resultado;
    }

    private String extract(TipoFact tipo, String s) {
        String resultado = "";
        String[] text = s.split("\n");

        switch (tipo) {
            case CASE_USA:
                for (int i = 0; i < 50; i++) {
                    String currText = removeSpaces(text[i]);
                    if (currText.equals("ORDERNUMBERCUSTOMERP.O.ORDERTYPEORDERDATECARRIERMOT")) {

                        // TODO obtener pedido
                        i++;

                        // obtener repuestos
                        boolean fin = false;
                        while (!fin) {
                            i++;
                            for (int j = 0; j < 30; j++) {
                                currText = removeSpaces(text[j + i]);
                                
                                // fin de pagina ?
                                if (currText.equals("ThegoodsaresoldwithretentionoftitleofCNHIInternational"
                                        + "/itsassigneeuntilreceiptoffullpayment.Thisinvoiceistob"
                                        + "epaidatourHeadOffice.Theaboveinvoiceiscorrectandtrue")) {
                                    break;
                                }
                                
                                // no hay repuestos ?
                                if (currText.equals("Subtotals")) {
                                    fin = true;
                                    break;
                                }

                                if (!text[j + i].isBlank()) {
                                    resultado = resultado + text[j + i] + "\n";
                                }
                            }

                            // detectar sig. pagina
                            if (!fin) {
                                fin = true;
                                i++;
                                for (int j = 0; j < 25; i++) {
                                    currText = removeSpaces(text[i]);
                                    if (currText.equals("LINESHIPPINGDOCPARTNUMBERDESCRIPTIONCUSTOMSCountry"
                                            + "ECCNCASESHIPPINGQUANTITYSDCLISTPRICE%DISCOUNT/UNIT"
                                            + "PRICEPRNETAMOUNTVAT")) {
                                        i++;
                                        fin = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                break;
        }

        return resultado;
    }

    private void btnProcesarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnProcesarMouseClicked
        JFileChooser fc = new JFileChooser();

        fc.setDialogTitle("Seleccione archivo a convertir...");

        FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos Adobe PDF (*.pdf)", "pdf");
        fc.setAcceptAllFileFilterUsed(false);
        fc.addChoosableFileFilter(filter);

        int ret = fc.showOpenDialog(main.this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            try {
                String s = convert2Text(fc.getSelectedFile());
                TipoFact tipo = detect(s);

                System.out.println(tipo);

                String data = extract(tipo, s);

                try (FileWriter writer = new FileWriter(fc.getSelectedFile().toString().replace(".pdf", ".irp"))) {
                    writer.write(data);
                }

                System.out.println(data);
            } catch (IOException ex) {
                Logger.getLogger(main.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_btnProcesarMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnProcesar;
    // End of variables declaration//GEN-END:variables
}
