package com.ddhigh.mybatis.worker;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class MemoryMonitorWorker extends SwingWorker<Void, Long> {
    public interface OnMemoryLoadedListener {
        void onLoaded(long used, long free, long total);
    }

    private OnMemoryLoadedListener listener;

    public MemoryMonitorWorker(OnMemoryLoadedListener listener) {
        this.listener = listener;
    }

    @Override
    protected Void doInBackground() throws Exception {
        final Runtime runtime = Runtime.getRuntime();
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long total = runtime.totalMemory();
                long free = runtime.freeMemory();
                long used = total - free;
                publish(used,free,total);
            }
        });
        timer.start();
        return null;
    }

    @Override
    protected void process(List<Long> chunks) {
        listener.onLoaded(chunks.get(0), chunks.get(1), chunks.get(2));
    }
}
