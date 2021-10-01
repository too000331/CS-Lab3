import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.json.JSONArray;
import org.json.JSONObject;


public class Main extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final String INTERNAL_POLICY_FILE = "policy.json";
    private static final String SBT_CUSTOM_ITEMS = "-={SBT_CUSTOM_ITEMS}=-";

    private final JFileChooser fc = new JFileChooser();
    private JToolBar toolBar;
    private JButton btnLoad;
    private JButton btnLoadJson;
    private JButton btnSave;
    private JButton btnSaveAs;
    private JButton btnExport;
    private JButton btnSelectAll;
    private JButton btnUnselectAll;
    private JButton btnAudit;
    private JTextField txtFilter;
    private JScrollPane scrl;
    private CustomItemModel itemsModel;
    private JTable itemsTable;
    private String template = "MSCT_Windows_10_1909_1.0.0.audit";

    private Policy currentPolicy;


    public Main() {
        this.setTitle("Auditing a workstation");
        this.setSize(1450, 815);
        this.setLayout(new BorderLayout(5, 5));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        fc.setCurrentDirectory(Paths.get("").toFile().getAbsoluteFile());

        toolBar = new JToolBar();
        this.add(toolBar, BorderLayout.NORTH);

        btnLoad = new JButton("Load audit file");
        toolBar.add(btnLoad);
        btnLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadFromFile();
            }
        });

        btnLoadJson = new JButton("Load JSON file");
        toolBar.add(btnLoadJson);
        btnLoadJson.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadFromJsonFile();
            }
        });

        toolBar.addSeparator();

        btnSave = new JButton("Save");
        toolBar.add(btnSave);
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });

        btnSaveAs = new JButton("Save As");
        toolBar.add(btnSaveAs);
        btnSaveAs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveAs();
            }
        });

        btnExport = new JButton("Export");
        toolBar.add(btnExport);
        btnExport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                export();
            }
        });

        toolBar.addSeparator();

        JLabel lbl = new JLabel("Filter:");
        toolBar.add(lbl);
        txtFilter = new JTextField();
        toolBar.add(txtFilter);
        txtFilter.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                itemsModel.filter(txtFilter.getText());
            }
        });

        btnSelectAll = new JButton("Select All");
        toolBar.add(btnSelectAll);
        btnSelectAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleSelectAll(true);
            }
        });

        btnUnselectAll = new JButton("Unselect All");
        toolBar.add(btnUnselectAll);
        btnUnselectAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleSelectAll(false);
            }
        });

        toolBar.addSeparator();
        btnAudit = new JButton("Audit");
        toolBar.add(btnAudit);
        btnAudit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                audit();
            }
        });


        itemsModel = new CustomItemModel(null);
        itemsTable = new JTable(itemsModel);

        scrl = new JScrollPane(itemsTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrl.setBounds(0, 0, 1400, 700);
        this.add(scrl, BorderLayout.CENTER);

        itemsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        itemsTable.getColumn("Select").setPreferredWidth(50);
        itemsTable.getColumn("Status").setPreferredWidth(60);
        itemsTable.getColumn("System Value").setPreferredWidth(150);
        itemsTable.getColumn("description").setPreferredWidth(500);
        itemsTable.getColumn("solution").setPreferredWidth(700);
        itemsTable.getColumn("info").setPreferredWidth(700);
        itemsTable.getColumn("see_also").setPreferredWidth(250);
        itemsTable.getColumn("type").setPreferredWidth(150);
        itemsTable.getColumn("value_type").setPreferredWidth(150);
        itemsTable.getColumn("value_data").setPreferredWidth(150);
        itemsTable.getColumn("right_type").setPreferredWidth(150);
        itemsTable.getColumn("reg_item").setPreferredWidth(150);
        itemsTable.getColumn("reg_key").setPreferredWidth(500);
        itemsTable.getColumn("reference").setPreferredWidth(500);

        if(new File(INTERNAL_POLICY_FILE).exists()) {
            readFromJsonFile(INTERNAL_POLICY_FILE);
        }
    }

    private void loadFromFile() {
        fc.setDialogTitle("Load from Security Policy file");
        fc.setFileFilter(auditFileFilter);
        if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String fName = fc.getSelectedFile().getAbsolutePath();
            try {
                JSONObject json = new AuditParser().parseFile(fName);
                currentPolicy = new Policy(json);
                itemsModel.setPolicy(currentPolicy);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private void loadFromJsonFile() {
        fc.setDialogTitle("Load from JSON file");
        fc.setFileFilter(jsonFileFilter);
        if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String fName = fc.getSelectedFile().getAbsolutePath();
            readFromJsonFile(fName);
        }
    }

    private void readFromJsonFile(String fName) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(fName));
            String content = new String(bytes);
            JSONObject  json = new JSONObject(content);
            currentPolicy = new Policy(json);
            itemsModel.setPolicy(currentPolicy);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private void saveAs() {
        fc.setDialogTitle("Asve As JSON file");
        fc.setFileFilter(jsonFileFilter);
        if(fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String fName = fc.getSelectedFile().getAbsolutePath();
            writeToJsonFile(fName);
        }
    }

    private void save() {
        writeToJsonFile(INTERNAL_POLICY_FILE);
        readFromJsonFile(INTERNAL_POLICY_FILE);
    }

    private void export() {
        File templateFile = new File("templates", template);
        if(!templateFile.exists()) {
            String errorMessage = "The template file is missing: " + templateFile.getAbsolutePath();
            JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        fc.setDialogTitle("Export As Security Policy file");
        fc.setFileFilter(auditFileFilter);
        if(fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String fName = fc.getSelectedFile().getAbsolutePath();
            writeToAuditFile(fName, templateFile);
        }
    }

    private void writeToAuditFile(String fName, File templateFile) {
        try {
            byte[] bytes = Files.readAllBytes(templateFile.toPath());
            String content = new String(bytes);
            StringBuilder sb = new StringBuilder();
            for(CustomItem item : currentPolicy.getItems()) {
                if(item.isSelected()) {
                    sb.append(item.toAuditFormat()).append("\n");
                }
            }
            content = content.replace(SBT_CUSTOM_ITEMS, sb.toString());

            Files.write(Paths.get(fName), Arrays.asList(content));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToJsonFile(String fName) {
        JSONObject json = new JSONObject(currentPolicy.getJson().toString());
        JSONArray customItemsJson = json.getJSONObject("check_type").getJSONObject("group_policy").getJSONObject("if").getJSONObject("then").getJSONArray("custom_item");

        while(!customItemsJson.isEmpty()) {
            customItemsJson.remove(0);
        }
        for(CustomItem item : currentPolicy.getItems()) {
            if(item.isSelected()) {
                customItemsJson.put(item.getJson());
            }
        }

        try {
            Files.write(Paths.get(fName), Arrays.asList(json.toString(2)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void toggleSelectAll(boolean select) {
        for(int i = 0; i < itemsModel.getRowCount(); i++) {
            itemsModel.setValueAt(select, i, CustomItemModel.COL_IDX_SELECTED);
        }
        itemsTable.repaint();
    }

    private void audit() {
        ExecutorService svc = Executors.newFixedThreadPool(1);
        for(int i = 0; i < itemsModel.getRowCount(); i++) {
            CustomItem item = itemsModel.getItem(i);
            final int idx = i;
            if(item.isSelected()) {
                svc.execute(new Runnable() {
                    @Override
                    public void run() {
                        itemsModel.setStatus(idx, "checking");
                        ExecutionResult result = Executer.check(item);
                        itemsModel.setStatus(idx, result.getStatus());
                        itemsModel.setSystemValue(idx, result.getValue());
                    }
                });
            }
        }
    }

    private FileFilter jsonFileFilter = new FileFilter() {
        @Override
        public String getDescription() {
            return "JSON file (.json)";
        }

        @Override
        public boolean accept(File f) {
            return f.getName().toLowerCase().endsWith(".json");
        }
    };

    private FileFilter auditFileFilter = new FileFilter() {
        @Override
        public String getDescription() {
            return "Security Policy file (.audit)";
        }

        @Override
        public boolean accept(File f) {
            return f.getName().toLowerCase().endsWith(".audit");
        }
    };

    public static void main(String[] args) {
        new Main().setVisible(true);
    }

}