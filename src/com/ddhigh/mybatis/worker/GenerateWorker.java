package com.ddhigh.mybatis.worker;

import com.ddhigh.mybatis.entity.TableEntity;
import com.ddhigh.mybatis.util.DbUtil;
import org.apache.log4j.Logger;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.config.*;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.internal.DefaultShellCallback;

import javax.swing.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GenerateWorker {
    private static final Logger logger = Logger.getLogger(GetTablesWorker.class);

    public interface OnGenerateCompleteListener {
        public void onSuccess(String msg);

        public void onError(String message, Throwable ex);
    }

    private String src;
    private String modelPkg;
    private String mapPkg;
    private String daoPkg;
    private List<TableEntity> tableEntities;
    private JLabel label;
    private boolean overwrite;
    private DbUtil dbUtil;
    private OnGenerateCompleteListener listener;
    private long beginAt = System.currentTimeMillis();

    public void setListener(OnGenerateCompleteListener listener) {
        this.listener = listener;
    }

    public GenerateWorker(String src, String modelPkg, String mapPkg, String daoPkg, List<TableEntity> tableEntities, JLabel label, boolean overwrite, DbUtil dbUtil) {
        this.src = src;
        this.modelPkg = modelPkg;
        this.mapPkg = mapPkg;
        this.daoPkg = daoPkg;
        this.tableEntities = tableEntities;
        this.label = label;
        this.overwrite = overwrite;
        this.dbUtil = dbUtil;
    }

    public void execute() throws InvalidConfigurationException, InterruptedException, SQLException, IOException {
        List<String> warnings = new ArrayList<>();
        Configuration configuration = new Configuration();
        //jdbc
        Context context = new Context(null);
        context.setTargetRuntime("MyBatis3");
        context.setId("table2orm");
        JDBCConnectionConfiguration jdbcConnectionConfiguration = new JDBCConnectionConfiguration();
        jdbcConnectionConfiguration.setConnectionURL(dbUtil.buildConnectionString(dbUtil));
        jdbcConnectionConfiguration.setPassword(dbUtil.getPassword());
        jdbcConnectionConfiguration.setUserId(dbUtil.getUser());
        jdbcConnectionConfiguration.setDriverClass(dbUtil.getType().equals(DbUtil.Type.MySQL) ? "com.mysql.cj.jdbc.Driver" : "oracle.jdbc.driver.OracleDriver");
        context.setJdbcConnectionConfiguration(jdbcConnectionConfiguration);
        //路径完成
        JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = new JavaModelGeneratorConfiguration();
        javaModelGeneratorConfiguration.setTargetProject(src);
        javaModelGeneratorConfiguration.setTargetPackage(modelPkg);
        context.setJavaModelGeneratorConfiguration(javaModelGeneratorConfiguration);

        SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration = new SqlMapGeneratorConfiguration();
        sqlMapGeneratorConfiguration.setTargetPackage(mapPkg);
        sqlMapGeneratorConfiguration.setTargetProject(src);
        context.setSqlMapGeneratorConfiguration(sqlMapGeneratorConfiguration);

        JavaClientGeneratorConfiguration javaClientGeneratorConfiguration = new JavaClientGeneratorConfiguration();
        javaClientGeneratorConfiguration.setTargetPackage(daoPkg);
        javaClientGeneratorConfiguration.setTargetProject(src);
        javaClientGeneratorConfiguration.setConfigurationType("XMLMAPPER");
        context.setJavaClientGeneratorConfiguration(javaClientGeneratorConfiguration);

        for (TableEntity tableEntity : tableEntities) {
            TableConfiguration tableConfiguration = new TableConfiguration(context);
            tableConfiguration.setTableName(tableEntity.getTableName());
            tableConfiguration.setDomainObjectName(tableEntity.getEntityName());
            tableConfiguration.setDeleteByExampleStatementEnabled(false);
            tableConfiguration.setUpdateByExampleStatementEnabled(false);
            tableConfiguration.setCountByExampleStatementEnabled(false);
            tableConfiguration.setSelectByExampleStatementEnabled(false);
            context.addTableConfiguration(tableConfiguration);
        }
        configuration.addContext(context);
        DefaultShellCallback defaultShellCallback = new DefaultShellCallback(overwrite);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(configuration, defaultShellCallback, warnings);
        //实体
        List<TableConfiguration> tableConfigurations = configuration.getContexts().get(0).getTableConfigurations();

        for (TableEntity tableEntity : tableEntities) {
            TableConfiguration tableConfiguration = new TableConfiguration(configuration.getContexts().get(0));
            tableConfiguration.setTableName(tableEntity.getTableName());
            tableConfiguration.setDomainObjectName(tableEntity.getEntityName());
            tableConfiguration.setCountByExampleStatementEnabled(false);
            tableConfiguration.setDeleteByExampleStatementEnabled(false);
            tableConfiguration.setSelectByExampleStatementEnabled(false);
            tableConfiguration.setUpdateByExampleStatementEnabled(false);
            tableConfigurations.add(tableConfiguration);
        }
        myBatisGenerator.generate(progressCallback);
    }

    private ProgressCallback progressCallback = new ProgressCallback() {
        @Override
        public void introspectionStarted(int i) {
            logger.debug("introspectionStarted => " + i);
        }

        @Override
        public void generationStarted(int i) {
            logger.debug("generationStarted => " + i);
        }

        @Override
        public void saveStarted(int i) {
            logger.debug("saveStarted => " + i);
        }

        @Override
        public void startTask(String s) {
            label.setText(s);
        }

        @Override
        public void done() {
            listener.onSuccess("生成完成");
        }

        @Override
        public void checkCancel() throws InterruptedException {
        }
    };
}
