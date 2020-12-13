package com.netcracker.odstc.logviewer.serverconnection.services;

import com.netcracker.odstc.logviewer.models.Directory;
import com.netcracker.odstc.logviewer.models.LogFile;
import com.netcracker.odstc.logviewer.models.lists.LogLevel;
import com.netcracker.odstc.logviewer.models.lists.Protocol;
import com.netcracker.odstc.logviewer.serverconnection.FTPServerConnection;
import com.netcracker.odstc.logviewer.serverconnection.SSHServerConnection;
import com.netcracker.odstc.logviewer.serverconnection.ServerConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerConnectionService {
    private static ServerConnectionService instance;
    private final Logger logger = LogManager.getLogger(ServerConnectionService.class.getName());
    protected List<Pattern> logSearchPatterns; // На случай если паттернов поиска будет несколько

    private static final String[] SEARCH_PATTERNS = new String[]{
            "(\\d+\\.\\d+\\.\\d{4}\\s\\d+:\\d+:\\d+\\.\\d+)\\s([A-Z]+)?.*$"//Добавить второй шаблон
    };//Может получатся из базы, но пока и так неплохо.

    private ServerConnectionService() {
        logSearchPatterns = new ArrayList<>();
        for (String pattern : SEARCH_PATTERNS) {
            logSearchPatterns.add(Pattern.compile(pattern));
        }
    }

    public static ServerConnectionService getInstance() {
        if (instance == null) {
            instance = new ServerConnectionService();
        }
        return instance;
    }

    public Date formatDate(String date) {
        Date logCreationDate = null;
        try {
            logCreationDate = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss.SSS").parse(date);
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        }
        return logCreationDate;
    }

    public LogLevel formatLogLevel(String level) {
        if (level == null) {
            return null;
        }
        if (LogLevel.contains(level)) {
            return LogLevel.valueOf(level);
        } else {
            return null;
        }
    }

    public List<LogFile> getFilesFromRemoteDirectory(Directory directory, String extension) {
        List<LogFile> logFiles = new ArrayList<>();
        ServerConnection serverConnection;
        if (directory.getParentServer().getProtocol().equals(Protocol.FTP)) {
            serverConnection = new FTPServerConnection(directory.getParentServer());
        } else if (directory.getParentServer().getProtocol().equals(Protocol.SSH)) {
            serverConnection = new SSHServerConnection(directory.getParentServer());
        } else {
            return logFiles;
        }
        if (serverConnection.connect() && serverConnection.isDirectoryValid(directory)) {
            logFiles.addAll(serverConnection.getLogFiles(directory, extension));
        }
        return logFiles;
    }

    public Matcher getLogMatcher(String line) {
        Matcher matcher;
        for (Pattern pattern :
                logSearchPatterns) {
            matcher = pattern.matcher(line);
            if (matcher.find()) {
                return matcher;
            }
        }
        return null;
    }
}