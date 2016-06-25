package com.ddhigh.mybatis.window;

import com.ddhigh.mybatis.entity.TableEntity;
import com.ddhigh.mybatis.util.GUIUtil;
import com.ddhigh.mybatis.worker.MemoryMonitorWorker;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.util.List;

public class Dashboard {
    private static Logger logger = Logger.getLogger(Dashboard.class);
    private JPanel container;
    private JLabel labelStatus;
    private JLabel labelMemory;
    private List<TableEntity> tableEntities;


    public Dashboard(List<TableEntity> tableEntities) {
        this.tableEntities = tableEntities;
        JFrame frame = new JFrame("控制台");
        frame.setContentPane(container);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        GUIUtil.setCenter(frame);
        frame.setVisible(true);
        //启动监控线程
        launchMonitor();
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
        logger.info(" 监控线程启动");
    }
}
