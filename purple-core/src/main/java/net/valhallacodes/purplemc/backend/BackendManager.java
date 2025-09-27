/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.backend;

import net.md_5.bungee.config.Configuration;
import net.valhallacodes.purplemc.PurpleCore;

import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackendManager {
    
    private final PurpleCore plugin;
    private final ExecutorService executor;
    private Connection connection;
    private String host, database, username, password;
    private int port;
    private boolean ssl;
    
    public BackendManager(PurpleCore plugin) {
        this.plugin = plugin;
        this.executor = Executors.newFixedThreadPool(10);
        loadConfig();
    }
    
    private void loadConfig() {
        Configuration config = plugin.getConfig();
        this.host = config.getString("backend.host", "localhost");
        this.port = config.getInt("backend.port", 3306);
        this.database = config.getString("backend.database", "purplemc");
        this.username = config.getString("backend.username", "root");
        this.password = config.getString("backend.password", "");
        this.ssl = config.getBoolean("backend.ssl", false);
    }
    
    public void initialize() throws SQLException {
        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=%s&autoReconnect=true&useUnicode=true&characterEncoding=UTF-8",
                host, port, database, ssl);
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, username, password);
            createTables();
            plugin.getLogger().info("Conexão com backend estabelecida com sucesso!");
        } catch (ClassNotFoundException e) {
        }
    }
    
    private void createTables() throws SQLException {
        try {
            String createPlayersTable = """
                CREATE TABLE IF NOT EXISTS players (
                    uuid VARCHAR(36) PRIMARY KEY,
                    name VARCHAR(16) NOT NULL,
                    rank VARCHAR(20) DEFAULT 'MEMBRO',
                    tag VARCHAR(20) DEFAULT 'MEMBER',
                    prefix_type VARCHAR(20) DEFAULT 'DEFAULT_GRAY',
                    first_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    INDEX idx_name (name),
                    INDEX idx_rank (rank),
                    INDEX idx_tag (tag)
                )
                """;
            
            PreparedStatement stmt = connection.prepareStatement(createPlayersTable);
            stmt.executeUpdate();
            stmt.close();
            
            plugin.getLogger().info("Tabela players criada/verificada com sucesso!");
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao criar tabela players: " + e.getMessage());
        }
    }
    
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("Erro ao fechar conexão backend: " + e.getMessage());
            }
        }
        executor.shutdown();
    }
    
    public Connection getConnection() {
        return connection;
    }
}
