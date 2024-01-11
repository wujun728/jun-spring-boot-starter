package entity;

import cn.hutool.json.JSONUtil;
import com.alibaba.druid.pool.DruidDataSource;
import io.github.wujun728.jdbc.BaseDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
public class Test {

	public static void main(String[] args) {
		DruidDataSource druidDataSource = new DruidDataSource();
		druidDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
//		druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");
		druidDataSource.setUrl("jdbc:mysql://127.0.0.1/db_test666?characterEncoding=utf8");
		druidDataSource.setUsername("root");
		druidDataSource.setPassword("");
		druidDataSource.setInitialSize(3);
		druidDataSource.setMinIdle(3);
		druidDataSource.setMaxActive(20);
		druidDataSource.setMaxWait(60000);
		druidDataSource.setTimeBetweenEvictionRunsMillis(60000);
		druidDataSource.setMinEvictableIdleTimeMillis(300000);
		druidDataSource.setTestWhileIdle(true);
		druidDataSource.setTestOnBorrow(false);
		druidDataSource.setTestOnReturn(false);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(druidDataSource);
		BaseDao baseDao = new BaseDao();
		baseDao.setJdbcTemplate(jdbcTemplate);
//		baseDao.setEntityClass(UploadFile.class);
//		List<UploadFile> projectList = baseDao.select(UploadFile.class);
		Test11 test11 = new Test11();
		test11.setId(111333L);
		test11.setTitle("22222");
		test11.setCreateTime(new Date());
		test11.setContent333("666666666 555 ");
		test11.setFieldNametest("teste 666666   ");
		baseDao.insert(test11);
		List<Test11> projectList2 = baseDao.select("select * from test order by id desc");
		log.info(JSONUtil.toJsonStr(projectList2));
	}

}
