package io.github.wujun728.uidgenerator.worker.repository;

import io.github.wujun728.uidgenerator.worker.entity.WorkerNodeEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Objects;

@Repository
public class DbWorkerNodeResposity implements WorkerNodeResposity {
    private static final String GET_WORKER_NODE_BY_HOST_PORT_SQL = "SELECT ID,HOST_NAME,PORT,TYPE,LAUNCH_DATE,MODIFIED,CREATED FROM worker_node WHERE HOST_NAME = ? AND PORT = ?";
    private static final String ADD_WORKER_NODE_SQL = "INSERT INTO worker_node (HOST_NAME,PORT,TYPE,LAUNCH_DATE,MODIFIED,CREATED) VALUES (?,?,?,?,NOW(),NOW())";

    private final JdbcTemplate jdbcTemplate;

    public DbWorkerNodeResposity(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * @param host
     * @param port
     * @return
     */
    @Override
    public WorkerNodeEntity getWorkerNodeByHostPort(String host, String port) {
        return this.jdbcTemplate.queryForObject(GET_WORKER_NODE_BY_HOST_PORT_SQL, (rs, rowNum) -> {
            WorkerNodeEntity entity = new WorkerNodeEntity();
            entity.setId(rs.getLong("ID"));
            entity.setHostName(rs.getString("HOST_NAME"));
            entity.setPort(rs.getString("PORT"));
            entity.setType(rs.getInt("TYPE"));
            entity.setLaunchDateDate(rs.getDate("LAUNCH_DATE"));
            entity.setModified(rs.getTimestamp("MODIFIED"));
            entity.setCreated(rs.getTime("CREATED"));
            return entity;
        }, new String[]{host, port});
    }

    /**
     * Add {@link WorkerNodeEntity}
     *
     * @param entity
     */
    @Override
    public void addWorkerNode(WorkerNodeEntity entity) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator preparedStatementCreator = con -> {
            PreparedStatement ps = con.prepareStatement(ADD_WORKER_NODE_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, entity.getHostName());
            ps.setString(2, entity.getPort());
            ps.setInt(3, entity.getType());
            ps.setObject(4, entity.getLaunchDate());
            return ps;
        };
        this.jdbcTemplate.update(preparedStatementCreator, keyHolder);
        entity.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
    }

}
