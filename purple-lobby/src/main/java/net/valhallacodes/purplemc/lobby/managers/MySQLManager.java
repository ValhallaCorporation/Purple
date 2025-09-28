/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.managers;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MySQLManager {

    private final PurpleLobby plugin;
    private final ExecutorService executor;
    private Connection connection;
    private String host, database, username, password;
    private int port;
    private boolean ssl;
    private boolean tablesCreated = false;

    public MySQLManager(PurpleLobby plugin) {
        this.plugin = plugin;
        this.executor = Executors.newFixedThreadPool(10);
        loadConfig();
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        this.host = config.getString("database.host", "localhost");
        this.port = config.getInt("database.port", 3306);
        this.database = config.getString("database.database", "purplemc");
        this.username = config.getString("database.username", "root");
        this.password = config.getString("database.password", "");
        this.ssl = config.getBoolean("database.ssl", false);
    }

    public boolean connectSync() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return true;
        }
        
        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=%s&autoReconnect=true&useUnicode=true&characterEncoding=UTF-8",
                host, port, database, ssl);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, username, password);
            
            if (!tablesCreated) {
                createTables();
                tablesCreated = true;
            }
            
            return true;
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL não encontrado: " + e.getMessage());
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
                    luna_plus_color VARCHAR(10) DEFAULT '§5',
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
            
            // Adicionar coluna luna_plus_color se não existir
            try {
                String addLunaPlusColorColumn = "ALTER TABLE players ADD COLUMN luna_plus_color VARCHAR(10) DEFAULT '§5'";
                PreparedStatement alterStmt = connection.prepareStatement(addLunaPlusColorColumn);
                alterStmt.executeUpdate();
                alterStmt.close();
                plugin.getLogger().info("Coluna luna_plus_color adicionada com sucesso!");
            } catch (SQLException e) {
                // Coluna já existe, ignorar erro
                plugin.getLogger().fine("Coluna luna_plus_color já existe ou erro ao adicionar: " + e.getMessage());
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao criar tabela players: " + e.getMessage());
        }
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("Erro ao fechar conexão MySQL: " + e.getMessage());
            }
        }
        executor.shutdown();
    }
    
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connectSync();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao verificar conexão MySQL: " + e.getMessage());
        }
        return connection;
    }
}
