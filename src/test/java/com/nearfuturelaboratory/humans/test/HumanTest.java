package com.nearfuturelaboratory.humans.test;

//import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.bson.types.ObjectId;
import org.junit.BeforeClass;
import org.junit.Test;

import com.nearfuturelaboratory.humans.entities.Human;
import com.nearfuturelaboratory.humans.entities.ServiceEntry;
import com.nearfuturelaboratory.humans.entities.ServiceUser;



//import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

//import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.Matchers.*;

public class HumanTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void getServiceUserById() {
		ServiceEntry service_entry = new ServiceEntry("id_", "username_", "service_");
		ServiceUser service_user = new ServiceUser("id__", "username__", "name__", "image_url__", service_entry);
		ObjectId aId = new ObjectId(new Date(), 1);
		service_user.setId(aId);
		Human human = new Human();
		human.setName("test");
		human.addServiceUser(service_user);
		ServiceUser s = human.getServiceUserById(aId.toString());
		assertThat(s, is(equalTo(service_user)));
		aId = new ObjectId(new Date(), 2);
		s.setId(aId);
		s.setUsername("skrumpy");
		boolean result = human.addServiceUser(s);
		assertThat(result, is(false));
		assertThat(human.getServiceUsers(), hasItem(s));
		assertThat(human.getServiceUsers(), hasSize(1));
		assertThat(human.getServiceUsers().get(0), sameInstance(s));
		assertThat(s, sameInstance(service_user));
		
		result = human.removeServiceUserById(new ObjectId(new Date(), 1).toString());
		assertThat(result, is(false));
		assertThat(human.getServiceUsers(), hasSize(1));

		result = human.removeServiceUserById(aId.toString());
		assertThat(result, is(true));
		assertThat(human.getServiceUsers(), hasSize(0));

		
		
		System.out.println(human.getServiceUsers());
	}
	
	@Test
	public void addServiceUser() {
		ServiceEntry service_entry = new ServiceEntry("id_", "username_", "service_");
		ServiceUser service_user = new ServiceUser("id__", "username__", "name__", "image_url__", service_entry);
		Human human = new Human();
		human.setName("test");
		human.addServiceUser(service_user);
		assertThat(human.getServiceUsers(), hasItem(service_user));
	}
	
	@Test
	public void removeServiceUser() {
		ServiceEntry service_entry = new ServiceEntry("id_", "username_", "service_");
		ServiceUser service_user = new ServiceUser("id__", "username__", "name__", "image_url__", service_entry);
		//ServiceUser dummy = new ServiceUser("foo", "username__", "name__", "image_url__", service_entry);
		ObjectId aId = new ObjectId(new Date(), 1);
		service_user.setId(aId);
		
		Human human = new Human();
		human.setName("test");
		boolean result = human.addServiceUser(service_user);
		assertThat(human.getServiceUsers(), hasSize(1));
		assertThat(human.getServiceUsers(), hasItem(service_user));
		assertThat("addServiceUser failed", result, is(true));
		
		result = human.removeServiceUser(service_user);
		assertThat(human.getServiceUsers(), hasSize(0));
		assertThat(result, is(true));

		human.addServiceUser(service_user);
		assertThat(human.getServiceUsers(), hasSize(1));
		assertThat(human.getServiceUsers(), hasItem(service_user));
		result = human.removeServiceUserById(aId.toString());
		assertThat(human.getServiceUsers(), hasSize(0));
		assertThat(result, is(true));
//		Collection<Integer> collection = new ArrayList<Integer>();		
//		assertThat(human.getServiceUsers(), hasItem(service_user));
//		assertThat(collection, is(empty()));
//        assertThat("i like cheese", containsString("cheese"));
		
	}
}
