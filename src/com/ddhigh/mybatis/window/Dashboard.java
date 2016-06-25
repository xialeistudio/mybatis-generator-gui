package com.ddhigh.mybatis.window;

import com.ddhigh.mybatis.entity.TableEntity;
import com.ddhigh.mybatis.util.DbUtil;
import com.ddhigh.mybatis.util.GUIUtil;
import com.ddhigh.mybatis.worker.GetTablesWorker;
import com.ddhigh.mybatis.worker.MemoryMonitorWorker;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

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
    private DbUtil dbUtil;

    //生成参数区域
    private String src;
    private String modelPkg;
    private String mapPkg;
    private String daoPkg;

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
}
