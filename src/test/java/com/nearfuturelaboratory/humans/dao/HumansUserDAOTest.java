package com.nearfuturelaboratory.humans.dao;

import static org.junit.Assert.*;

import com.nearfuturelaboratory.humans.entities.ServiceUser;
import com.nearfuturelaboratory.util.Constants;
import org.bson.types.ObjectId;
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
import com.nearfuturelaboratory.humans.entities.Human;


public class HumansUserDAOTest {

	static HumansUserDAO dao;
    static HumansUserDAO dao_dev;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
        Constants.load("/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/dev.app.properties");

        dao = new HumansUserDAO("test-humans-user");
        dao_dev = new HumansUserDAO("humans");

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
        HumansUser user = dao_dev.findOneByUsername("darthjulian");



		//fail("Not yet implemented"); // TODO
	}

    @Test
    public void findOneByID() {
        HumansUser tmp = new HumansUser();
        tmp.setUsername("darthjulian");
        tmp.setPassword("pwd");
        tmp.setAccessToken("0123456789");

        Human human = new Human();
        human.setName("foo");

        ServiceUser service_user = new ServiceUser();
        service_user.setServiceName("twitter");
        service_user.setUsername("shempy");
        service_user.setServiceUserID("101010101010");
        service_user.setOnBehalfOf("20202020", "seawolf", "twitter");

        human.addServiceUser(service_user);

        tmp.addHuman(human);
        //dao.delete(tmp);
        dao.save(tmp);


        HumansUser user = dao.findOneByUsername("darthjulian");
        String id = user.getId().toString();
        HumansUser test = dao.findOneByID(user.getId().toString());
        assertThat(user, equalTo(test));

        dao.delete(tmp);
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
    public void testFindMeByUsernameAndFixPassword() {
        HumansUserDAO _dao = new HumansUserDAO("humans");

        HumansUser user = _dao.findOneByUsername("darthjulian");

        assertThat(user, notNullValue());

        user.setPassword("darthjulian");
        _dao.save(user);

        user = null;

        //user = new HumansUser();
       user = _dao.findOneByUsername("darthjulian");

        assertThat(user, notNullValue() );
        assertThat(user.verifyPassword("darthjulian"), is(true));
        System.out.println(user);
    }


	@Test
	public void testGetHumansUser() {
		fail("Not yet implemented"); // TODO
	}

}
