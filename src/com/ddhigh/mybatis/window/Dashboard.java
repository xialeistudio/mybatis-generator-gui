package com.ddhigh.mybatis.window;

import com.ddhigh.mybatis.entity.TableEntity;
import com.ddhigh.mybatis.util.DbUtil;
import com.ddhigh.mybatis.util.GUIUtil;
import com.ddhigh.mybatis.worker.GetTablesWorker;
import com.ddhigh.mybatis.worker.MemoryMonitorWorker;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private DbUtil dbUtil;

    //生成参数区域
    private String src;
    private String modelPkg;
    private String mapPkg;
    private String daoPkg;
    private String entitySuffix = "Entity";

    public Dashboard(final DbUtil dbUtil) {
        this.dbUtil = dbUtil;
        final JFrame frame = new JFrame("控制台");
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
        //启动监控线程
        launchMonitor();
        //首次加载表格
        launchTableLoader();
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
                displayTableWithData(list);
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

    /**
     * 通过数据加载table
     *
     * @param list
     */
    private void displayTableWithData(final List<TableEntity> list) {
        for (TableEntity t : list) {
            t.setEntityName(t.getEntityName() + entitySuffix);
        }
        tableTable.setModel(new TableModel() {
            @Override
            public int getRowCount() {
                return list.size();
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
                TableEntity entity = list.get(rowIndex);
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
                    TableEntity entity = list.get(rowIndex);
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
}
