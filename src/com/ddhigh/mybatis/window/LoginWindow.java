package com.ddhigh.mybatis.window;

import com.ddhigh.mybatis.entity.TableEntity;
import com.ddhigh.mybatis.util.DbUtil;
import com.ddhigh.mybatis.util.GUIUtil;
import com.ddhigh.mybatis.worker.GetTablesWorker;
import com.formdev.flatlaf.FlatLightLaf;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class LoginWindow {
    private JPanel container;
    private JButton btnConnect;
    private JComboBox comboBoxType;
    private JTextField txtHost;
    private JTextField txtUsername;
    private JTextField txtPassword;
    private JTextField txtPort;
    private JTextField txtDatabase;
    private static Logger logger = Logger.getLogger(LoginWindow.class);
    private static JFrame frame;
    public LoginWindow() {
        btnConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String host = txtHost.getText().trim();
                String port = txtPort.getText().trim();
                String username = txtUsername.getText().trim();
                String password = txtPassword.getText().trim();
                String type = comboBoxType.getSelectedItem().toString();
                String database = txtDatabase.getText().trim();
                if (host.isEmpty() || port.isEmpty() || username.isEmpty() || database.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "请填写必填信息");
                    return;
                }
                connect(host, port, username, password, type, database);
            }
        });
        comboBoxType.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selected = comboBoxType.getSelectedIndex();
                switch (selected) {
                    case 0:
                        txtPort.setText(String.valueOf(DbUtil.portMap.get(DbUtil.Type.MySQL)));
                        break;
                    case 1:
                        txtPort.setText(String.valueOf(DbUtil.portMap.get(DbUtil.Type.Oracle)));
                        break;
                }
            }
        });
    }

    private void connect(final String host, final String port, final String username, final String password, final String type, final String database) {
        btnConnect.setText("连接中");
        btnConnect.setEnabled(false);
        GetTablesWorker getTablesWorker = new GetTablesWorker(host, port, username, password, type, database);
        getTablesWorker.setListener(new GetTablesWorker.OnLoadedListener() {
            @Override
            public void onSuccess(List<TableEntity> list) {
                DbUtil.Type dbType;
                switch (type) {
                    case "Oracle":
                        dbType = DbUtil.Type.Oracle;
                        break;
                    default:
                        dbType = DbUtil.Type.MySQL;
                        break;
                }
                new Dashboard(new DbUtil(host, port, username, password, dbType, database));
                frame.dispose();
            }

            @Override
            public void onError(String message, Throwable ex) {
                btnConnect.setEnabled(true);
                btnConnect.setText("连接");
                JOptionPane.showMessageDialog(frame, message);
                logger.error(message, ex);
            }
        });
        getTablesWorker.execute();
    }

    public static void main(String[] args) {
        FlatLightLaf.install();
        frame = new JFrame("连接数据库");
        frame.setContentPane(new LoginWindow().container);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setResizable(false);
        GUIUtil.setCenter(frame);
        frame.setVisible(true);
    }
}
