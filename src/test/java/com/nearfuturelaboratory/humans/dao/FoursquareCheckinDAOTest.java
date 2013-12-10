package com.nearfuturelaboratory.humans.dao;

import static org.junit.Assert.*;
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
import java.util.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.nearfuturelaboratory.humans.foursquare.entities.FoursquareCheckin;

public class FoursquareCheckinDAOTest {

	static FoursquareCheckinDAO dao;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		dao = new FoursquareCheckinDAO();
	}

	@Test
	public void testFindByExactUserID() {
		fail("Not yet implemented"); // TODO
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testFindLatestCheckin() {
		// get a userid
		FoursquareCheckin lastLocalCheckin = dao.findLatestCheckin("41");
		List<FoursquareCheckin> checkins = dao.findByExactUserID("41");
		
		Collections.sort(checkins, new Comparator() {
			
		public int compare(Object o1, Object o2) {
			if(o1 instanceof FoursquareCheckin && o2 instanceof FoursquareCheckin) {
				FoursquareCheckin f1 = (FoursquareCheckin)o1;
				FoursquareCheckin f2 = (FoursquareCheckin)o2;
				int v = f1.compareTo(f2);
				//int v = f1.getCreatedDate().compareTo(f2.getCreatedDate());
					
				return v;
				}
			return 0;
			}
		});
		
		assertThat(lastLocalCheckin, equalTo(checkins.get(0)));
		
		//fail("Not yet implemented"); // TODO
	}

}
