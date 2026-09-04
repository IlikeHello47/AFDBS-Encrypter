import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HexFormat;

public final class AFDBSEncrypterUI extends JFrame {
    private JTextField txtInputFile;
    private JTextField txtOutputFile;
    private JTextField txtKey;
    private JComboBox<String> cbAction;
    private JComboBox<String> cbPreset;
    private JTextArea txtLog;

    private static final Color NEON_CYAN = new Color(0, 243, 255);
    private static final Color DARK_BG = new Color(15, 15, 22);
    private static final Color COMP_BG = new Color(28, 28, 38);

    public AFDBSEncrypterUI() {
        setTitle("AFDBS Advanced File Decryption Bypass Service Encryption Engine - v1.2 - IlikeHello47");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 600);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(DARK_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // --- ZEILE 1: Input File ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        JLabel lblInput = new JLabel("Input file:");
        lblInput.setForeground(NEON_CYAN);
        mainPanel.add(lblInput, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtInputFile = new JTextField();
        txtInputFile.setBackground(COMP_BG);
        txtInputFile.setForeground(Color.WHITE);
        txtInputFile.setCaretColor(NEON_CYAN);
        txtInputFile.setBorder(BorderFactory.createLineBorder(NEON_CYAN, 1));
        mainPanel.add(txtInputFile, gbc);

        gbc.gridx = 2; gbc.weightx = 0.0;
        JButton btnBrowseInput = new JButton("Browse...");
        btnBrowseInput.setBackground(COMP_BG);
        btnBrowseInput.setForeground(NEON_CYAN);
        btnBrowseInput.addActionListener(e -> chooseFile(txtInputFile, false));
        mainPanel.add(btnBrowseInput, gbc);

        // --- ZEILE 2: Action ---
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        JLabel lblAction = new JLabel("Action:");
        lblAction.setForeground(NEON_CYAN);
        mainPanel.add(lblAction, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        cbAction = new JComboBox<>(new String[]{"Encrypt", "Decrypt"});
        cbAction.setBackground(COMP_BG);
        cbAction.setForeground(Color.WHITE);
        cbAction.addActionListener(e -> updateFieldsBasedOnAction());
        mainPanel.add(cbAction, gbc);

        // --- ZEILE 3: Encryption/Decryption Key ---
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        JLabel lblKey = new JLabel("Key / Key File:");
        lblKey.setForeground(NEON_CYAN);
        mainPanel.add(lblKey, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtKey = new JTextField();
        txtKey.setBackground(COMP_BG);
        txtKey.setForeground(Color.WHITE);
        txtKey.setCaretColor(NEON_CYAN);
        txtKey.setBorder(BorderFactory.createLineBorder(NEON_CYAN, 1));
        txtKey.setToolTipText("Enter the key manually or select the anonymized .key file");
        mainPanel.add(txtKey, gbc);

        gbc.gridx = 2; gbc.weightx = 0.0;
        JButton btnBrowseKey = new JButton("Browse...");
        btnBrowseKey.setBackground(COMP_BG);
        btnBrowseKey.setForeground(NEON_CYAN);
        btnBrowseKey.addActionListener(e -> chooseFile(txtKey, false));
        mainPanel.add(btnBrowseKey, gbc);

        // --- ZEILE 4: Output Preset ---
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        JLabel lblPreset = new JLabel("Output preset:");
        lblPreset.setForeground(NEON_CYAN);
        mainPanel.add(lblPreset, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        cbPreset = new JComboBox<>(new String[]{"Same directory as input (default)"});
        cbPreset.setBackground(COMP_BG);
        cbPreset.setForeground(Color.WHITE);
        mainPanel.add(cbPreset, gbc);

        // --- ZEILE 5: Output File ---
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        JLabel lblOutput = new JLabel("Output (optional):");
        lblOutput.setForeground(NEON_CYAN);
        mainPanel.add(lblOutput, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtOutputFile = new JTextField();
        txtOutputFile.setBackground(COMP_BG);
        txtOutputFile.setForeground(Color.WHITE);
        txtOutputFile.setCaretColor(NEON_CYAN);
        txtOutputFile.setBorder(BorderFactory.createLineBorder(NEON_CYAN, 1));
        mainPanel.add(txtOutputFile, gbc);

        gbc.gridx = 2; gbc.weightx = 0.0;
        JButton btnBrowseOutput = new JButton("Browse...");
        btnBrowseOutput.setBackground(COMP_BG);
        btnBrowseOutput.setForeground(NEON_CYAN);
        btnBrowseOutput.addActionListener(e -> chooseFile(txtOutputFile, true));
        mainPanel.add(btnBrowseOutput, gbc);

        // --- ZEILE 6: Run Button ---
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 3; gbc.weightx = 1.0;
        JButton btnRun = new JButton("Run Process");
        btnRun.setBackground(new Color(10, 40, 50));
        btnRun.setForeground(NEON_CYAN);
        btnRun.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnRun.setBorder(BorderFactory.createLineBorder(NEON_CYAN, 2));
        btnRun.addActionListener(e -> executeCryptoAction());
        mainPanel.add(btnRun, gbc);

        // --- ZEILE 7: Log Area ---
        gbc.gridy = 6; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setBackground(new Color(20, 20, 25));
        txtLog.setForeground(Color.LIGHT_GRAY);
        txtLog.setCaretColor(NEON_CYAN);
        JScrollPane scrollPane = new JScrollPane(txtLog);

        TitledBorder logBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(NEON_CYAN, 1), "Log Output");
        logBorder.setTitleColor(NEON_CYAN);
        scrollPane.setBorder(logBorder);
        mainPanel.add(scrollPane, gbc);

        add(mainPanel);
        log("Anonymized ID system loaded. Key files will now receive random UUID names.");
    }
    private void chooseFile(JTextField targetField, boolean saveMode) {
        JFileChooser chooser = new JFileChooser();
        int ret = saveMode ? chooser.showSaveDialog(this) : chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            String selectedPath = chooser.getSelectedFile().getAbsolutePath();
            targetField.setText(selectedPath);

            // Wenn eine anonyme .key Datei ausgewählt wurde, extrahieren wir direkt den Inhalt
            if (targetField == txtKey && selectedPath.endsWith(".key")) {
                try {
                    String content = Files.readString(Path.of(selectedPath));
                    String extractedKey = AFDBSCore.parseValue(content, "key");
                    if (extractedKey != null) {
                        txtKey.setText(extractedKey);
                        log("Key successfully extracted from anonymized .key file.");
                    }
                } catch (Exception ex) {
                    log("Note: Could not automatically read key from file.");
                }
            }

            updateFieldsBasedOnAction();
        }
    }

    private void updateFieldsBasedOnAction() {
        String inStr = txtInputFile.getText().trim();
        if (inStr.isEmpty()) return;

        if ("Encrypt".equals(cbAction.getSelectedItem())) {
            txtOutputFile.setText(inStr + ".afdbslocked");
        } else {
            if (inStr.endsWith(".afdbslocked")) {
                txtOutputFile.setText(inStr.substring(0, inStr.length() - 12) + ".dec");
            } else {
                txtOutputFile.setText(inStr + ".dec");
            }
        }
    }

    private void log(String msg) {
        txtLog.append("[" + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + msg + "\n");
    }

    private void executeCryptoAction() {
        String inputStr = txtInputFile.getText().trim();
        String outputStr = txtOutputFile.getText().trim();
        String keyStr = txtKey.getText().trim();
        String action = (String) cbAction.getSelectedItem();

        if (inputStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an input file!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Path inPath = Path.of(inputStr);
        Path outPath = Path.of(outputStr.isEmpty() ? inputStr + ("Encrypt".equals(action) ? ".afdbslocked" : ".dec") : outputStr);

        new Thread(() -> {
            try {
                if ("Encrypt".equals(action)) {
                    log("Starting anonymous encryption for: " + inPath.getFileName());
                    // Der Core gibt nun den zufällig generierten Key-Pfad zurück
                    Path generatedKeyPath = AFDBSCore.encrypt(inPath, outPath);
                    log(" Encryption successful -> " + outPath);
                    log(" 🔑 Anonymized key file created: " + generatedKeyPath.getFileName());
                    log(" Send this .key file to your buddy!");
                } else {
                    log("Starting decryption for: " + inPath.getFileName());

                    Path keyPath;
                    // Überprüfung, ob eine physische .key-Datei geladen wurde
                    if (keyStr.endsWith(".key") && Files.exists(Path.of(keyStr))) {
                        keyPath = Path.of(keyStr);
                    } else {
                        // Wenn der User nur den Klartext-Key reinkopiert hat, erstellen wir eine temporäre Datei
                        keyPath = Path.of(inPath.toString() + ".tmpkey");
                        String fakeId = "00000000000000000000000000000000";

                        try (InputStream is = Files.newInputStream(inPath)) {
                            is.skip(AFDBSCore.MAGIC.length + 1);
                            byte[] fid = is.readNBytes(AFDBSCore.FILE_ID_SIZE);
                            fakeId = HexFormat.of().formatHex(fid);
                        } catch (Exception ignored) {}

                        String sb = "fileId=" + fakeId + '\n' +
                                "key=" + keyStr + '\n' +
                                "size=" + Files.size(inPath) + '\n' +
                                "ext=.dec\n";
                        Files.writeString(keyPath, sb);
                    }

                    Path finalOutPath = outPath;
                    if (Files.exists(keyPath)) {
                        String txt = Files.readString(keyPath);
                        String ext = AFDBSCore.parseValue(txt, "ext");
                        if (ext != null && !ext.isEmpty() && finalOutPath.toString().endsWith(".dec")) {
                            String base = finalOutPath.toString();
                            finalOutPath = Path.of(base.substring(0, base.length() - 4) + ext);
                        }
                    }

                    AFDBSCore.decrypt(inPath, keyPath, finalOutPath);
                    log(" Decryption successful -> " + finalOutPath);

                    if (keyPath.toString().endsWith(".tmpkey")) {
                        Files.deleteIfExists(keyPath);
                    }
                }
            } catch (Exception ex) {
                log("❌ ERROR: " + ex.getMessage());
                ex.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Standard Look & Feel could not be set.");
        }

        SwingUtilities.invokeLater(() -> new AFDBSEncrypterUI().setVisible(true));
    }
}
