package com.ddhigh.mybatis.window;

import com.ddhigh.mybatis.entity.TableEntity;
import com.ddhigh.mybatis.util.DbUtil;
import com.ddhigh.mybatis.util.GUIUtil;
import com.ddhigh.mybatis.worker.GenerateWorker;
import com.ddhigh.mybatis.worker.GetTablesWorker;
import com.ddhigh.mybatis.worker.MemoryMonitorWorker;
import org.apache.log4j.Logger;
import org.mybatis.generator.exception.InvalidConfigurationException;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

public class Dashboard {
    private static Logger logger = Logger.getLogger(Dashboard.class);
    private JPanel container;
    private JLabel labelStatus;
    private JLabel labelMemory;
    private JTextField txtSrc;
    private JButton btnSrc;
    private JTextField txtModelPkg;
    private JTextField txtMapPkg;
    private JTextField txtDaoPkg;
    private JButton btnRefreshTable;
    private JButton btnGenerate;
    private JTable tableTable;
    private JTextField txtEntity;
    private JCheckBox checkBoxOverwrite;
    private DbUtil dbUtil;

    //生成参数区域
    private String src;
    private String modelPkg;
    private String mapPkg;
    private String daoPkg;
    protected boolean overwrite = true;
    private String entitySuffix = "Entity";
    JFrame frame;

    public Dashboard(final DbUtil dbUtil) {
        this.dbUtil = dbUtil;
        frame = new JFrame("控制台");
        setupMenu();
        frame.setContentPane(container);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        GUIUtil.setCenter(frame);
        frame.setVisible(true);
        btnSrc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = fileChooser.showSaveDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    src = fileChooser.getSelectedFile().getAbsolutePath();
                    txtSrc.setText(src);
                }
            }
        });
        btnRefreshTable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                launchTableLoader();
            }
        });

        txtEntity.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                entitySuffix = txtEntity.getText().trim();
                logger.info("entitySuffix => " + entitySuffix);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                entitySuffix = txtEntity.getText().trim();
                logger.info("entitySuffix => " + entitySuffix);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                entitySuffix = txtEntity.getText().trim();
                logger.info("entitySuffix => " + entitySuffix);
            }
        });
        btnGenerate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generate();
            }
        });
        //启动监控线程
        launchMonitor();
        //首次加载表格
        launchTableLoader();
    }

    /**
     * 初始化菜单
     */
    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("帮助(H)");
        menu.setMnemonic('H');
        JMenuItem menuItem = new JMenuItem("关于(A)");
        menuItem.setMnemonic('A');
        menu.add(menuItem);
        menuBar.add(menu);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AboutDialog();
            }
        });
        frame.setJMenuBar(menuBar);
    }

    /**
     * 启动监控线程
     */
    private void launchMonitor() {
        MemoryMonitorWorker worker = new MemoryMonitorWorker(new MemoryMonitorWorker.OnMemoryLoadedListener() {
            @Override
            public void onLoaded(long used, long free, long total) {
                labelMemory.setText(used / 1024 / 1024 + "MB/" + total / 1024 / 1024 + "MB " + (used * 100 / total) + "%");
            }
        });
        worker.execute();
        logger.info("监控线程启动");
    }

    /**
     * 加载数据表
     */
    private void launchTableLoader() {
        btnRefreshTable.setEnabled(false);
        btnGenerate.setEnabled(false);
        labelStatus.setText("加载数据表");
        GetTablesWorker getTablesWorker = new GetTablesWorker(dbUtil);
        getTablesWorker.setListener(new GetTablesWorker.OnLoadedListener() {
            @Override
            public void onSuccess(List<TableEntity> list) {
                btnRefreshTable.setEnabled(true);
                btnGenerate.setEnabled(true);
                labelStatus.setText("成功加载【" + list.size() + "】张数据表");
                tables = list;
                displayTable();
            }

            @Override
            public void onError(String message, Throwable ex) {
                labelStatus.setText(message + " " + ex.getMessage());
                logger.error(message, ex);
                btnRefreshTable.setEnabled(true);
            }
        });
        getTablesWorker.execute();
    }

    private static Vector<String> tableColumnNames = new Vector<>();

    static {
        tableColumnNames.add("序号");
        tableColumnNames.add("表名");
        tableColumnNames.add("实体类名");
    }

    private List<TableEntity> tables;

    /**
     * 通过数据加载table
     */
    private void displayTable() {
        for (TableEntity t : tables) {
            t.setEntityName(t.getEntityName() + entitySuffix);
        }
        tableTable.setModel(new TableModel() {
            @Override
            public int getRowCount() {
                return tables.size();
            }

            @Override
            public int getColumnCount() {
                return tableColumnNames.size();
            }

            @Override
            public String getColumnName(int columnIndex) {
                return tableColumnNames.get(columnIndex);
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex == 2;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                TableEntity entity = tables.get(rowIndex);
                if (columnIndex == 0) {
                    return rowIndex + 1;
                }
                if (columnIndex == 1) {
                    return entity.getTableName();
                }
                if (columnIndex == 2) {
                    return entity.getEntityName();
                }
                return null;
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                if (columnIndex == 2) {
                    TableEntity entity = tables.get(rowIndex);
                    entity.setEntityName(aValue.toString());
                    logger.debug("[" + rowIndex + "][" + columnIndex + "] - " + aValue);
                }
            }

            @Override
            public void addTableModelListener(TableModelListener l) {

            }

            @Override
            public void removeTableModelListener(TableModelListener l) {

            }
        });
        TableColumn tableColumn = tableTable.getColumnModel().getColumn(0);
        tableColumn.setMaxWidth(48);
        tableColumn.setPreferredWidth(48);
        tableColumn.setMinWidth(48);
        tableTable.validate();
    }

    /**
     * 生成XML
     */
    private void generate() {
        src = txtSrc.getText().trim();
        modelPkg = txtModelPkg.getText().trim();
        mapPkg = txtMapPkg.getText().trim();
        daoPkg = txtDaoPkg.getText().trim();
        overwrite = checkBoxOverwrite.isSelected();
        entitySuffix = txtEntity.getText().trim();
        if (src.equals("请选择生成的src根目录") || modelPkg.isEmpty() || mapPkg.isEmpty() || daoPkg.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "请将信息填写完整");
            return;
        }
        labelStatus.setText("生成中");
        btnGenerate.setEnabled(false);
        btnRefreshTable.setEnabled(false);
        try {
            GenerateWorker worker = new GenerateWorker(src, modelPkg, mapPkg, daoPkg, tables, labelStatus, overwrite, dbUtil);
            worker.setListener(new GenerateWorker.OnGenerateCompleteListener() {
                @Override
                public void onSuccess(String msg) {
                    labelStatus.setText(msg);
                    btnGenerate.setEnabled(true);
                    btnRefreshTable.setEnabled(true);
                    JOptionPane.showMessageDialog(null, "生成成功!");
                }

                @Override
                public void onError(String message, Throwable ex) {
                    labelStatus.setText(message);
                    btnGenerate.setEnabled(true);
                    btnRefreshTable.setEnabled(true);
                    logger.error(message, ex);
                }
            });
            worker.execute();
        } catch (InterruptedException | InvalidConfigurationException | SQLException | IOException e) {
            logger.error(e);
            labelStatus.setText(e.getMessage());
            btnGenerate.setEnabled(true);
            btnRefreshTable.setEnabled(true);
        }
    }

}
