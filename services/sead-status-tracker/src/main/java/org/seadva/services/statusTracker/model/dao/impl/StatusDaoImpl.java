package org.seadva.services.statusTracker.model.dao.impl;

import org.apache.log4j.Logger;
import org.seadva.services.statusTracker.common.DBConnectionPool;
import org.seadva.services.statusTracker.common.ObjectPool;
import org.seadva.services.statusTracker.model.dao.StatusDao;
import org.seadva.services.statusTracker.model.obj.impl.Status;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StatusDaoImpl implements StatusDao {

    private static Logger log = Logger.getLogger(StatusDaoImpl.class);
    protected ObjectPool<Connection> connectionPool = null;

    public StatusDaoImpl() {
        connectionPool = DBConnectionPool.getInstance();
    }

    protected Connection getConnection() throws SQLException {
        return connectionPool.getEntry();
    }

    @Override
    public void putStatus(Status status) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement("INSERT IGNORE INTO status values(?,?,?)");
            statement.setString(1, status.getStatusId());
            statement.setString(2, status.getComponent());
            statement.setString(3, status.getDescription());
            statement.executeUpdate();

            statement.close();
            log.debug("Done inserting Status");
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    log.warn("Unable to close statement", e);
                }
                statement = null;
            }
            connectionPool.releaseEntry(connection);

        }
    }

    @Override
    public Status getStatusById(String id) {
        Status status = null;
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = getConnection();

            statement = connection.prepareStatement("Select * from status where status_id=?");
            statement.setString(1, id);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                status = new Status();
                status.setStatusId(resultSet.getString("status_id"));
                status.setComponent(resultSet.getString("component"));
                status.setDescription((resultSet.getString("description")));
                break;
            }

        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    //  log.warn("Unable to close statement", e);
                }
                statement = null;
            }
            connectionPool.releaseEntry(connection);

        }
        return status;
    }
}
