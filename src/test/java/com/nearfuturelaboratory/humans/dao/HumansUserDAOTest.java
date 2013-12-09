package com.nearfuturelaboratory.humans.dao;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.nearfuturelaboratory.humans.entities.HumansUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.*;

import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.fail;

public class HumansUserDAOTest {

	static HumansUserDAO dao;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		dao = new HumansUserDAO("test-humans-user");
	}

	@Test
	public void testFindByUsername() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetAllHumansUsers() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testFindByHumanID() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testFindOneByUsername() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testFindOneByAccessToken() {
		HumansUser tmp = new HumansUser();
		tmp.setUsername("test");
		tmp.setPassword("pwd");
		tmp.setAccessToken("0123456789");
		dao.save(tmp);
		
		HumansUser user = dao.findOneByAccessToken("0123456789");
		
		assertThat(user, notNullValue());
		assertThat(user.getUsername(), equalTo(tmp.getUsername()));
		//assertThat(user.getPassword(), equalTo(tmp.getPassword()));
		assertThat( user.verifyPassword("pwd"), is(true) );
		dao.delete(user);

	}
	
	@Test
	public void testFindOneByUsernameAndAccessToken() {
		HumansUser tmp = new HumansUser();
		tmp.setUsername("test");
		tmp.setPassword("pwd");
		tmp.setAccessToken("0123456789");
		dao.save(tmp);
		
		HumansUser user = dao.findOneByUsernameAndAccessToken("test", "0123456789");
		
		assertThat(user, notNullValue());
		assertThat(user.getUsername(), equalTo(tmp.getUsername()));
		//assertThat(user.getPassword(), equalTo(tmp.getPassword()));
		assertThat( user.verifyPassword("pwd"), is(true) );
		dao.delete(user);
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetHumansUser() {
		fail("Not yet implemented"); // TODO
	}

}
