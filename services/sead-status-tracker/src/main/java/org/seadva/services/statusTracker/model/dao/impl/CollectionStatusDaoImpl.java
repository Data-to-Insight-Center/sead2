package org.seadva.services.statusTracker.model.dao.impl;

import org.apache.log4j.Logger;
import org.seadva.services.statusTracker.common.DBConnectionPool;
import org.seadva.services.statusTracker.common.ObjectPool;
import org.seadva.services.statusTracker.model.dao.CollectionStatusDao;
import org.seadva.services.statusTracker.model.obj.impl.CollectionStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by charmadu on 7/22/15.
 */
public class CollectionStatusDaoImpl implements CollectionStatusDao{

    private static Logger log = Logger.getLogger(StatusDaoImpl.class);
    protected ObjectPool<Connection> connectionPool = null;

    public CollectionStatusDaoImpl() {
        connectionPool = DBConnectionPool.getInstance();
    }

    protected Connection getConnection() throws SQLException {
        return connectionPool.getEntry();
    }

    @Override
    public void putCollectionStatus(CollectionStatus collectionStatus) {

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement("INSERT INTO collection_status values(?,?,?) " +
                    "ON DUPLICATE KEY UPDATE status_updated_time=?");
            statement.setString(1, collectionStatus.getCollectionId());
            statement.setString(2, collectionStatus.getCurrentStatus());
            statement.setLong(3, collectionStatus.getUpdatedTime());
            statement.setLong(4, collectionStatus.getUpdatedTime());
            statement.executeUpdate();

            statement.close();
            log.debug("Done inserting collection_status");
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
    public List<CollectionStatus> getCollectionStatusById(String collectionId) {
        List<CollectionStatus> collectionStatusList = new ArrayList<CollectionStatus>();
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = getConnection();

            statement = connection.prepareStatement("Select * from collection_status where collection_id=?");
            statement.setString(1, collectionId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                CollectionStatus collectionStatus = new CollectionStatus();
                collectionStatus.setCollectionId(resultSet.getString("collection_id"));
                collectionStatus.setCurrentStatus((resultSet.getString("current_status")));
                collectionStatus.setUpdatedTime(resultSet.getLong("status_updated_time"));
                collectionStatusList.add(collectionStatus);
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
        return collectionStatusList;
    }
}
