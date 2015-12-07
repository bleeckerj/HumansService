package com.nearfuturelaboratory.humans.entities;

import com.nearfuturelaboratory.humans.flickr.entities.FlickrUser;
import com.nearfuturelaboratory.humans.foursquare.entities.FoursquareUser;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUser;
import com.nearfuturelaboratory.humans.service.FlickrService;
import com.nearfuturelaboratory.humans.service.FoursquareService;
import com.nearfuturelaboratory.humans.service.InstagramService;
import com.nearfuturelaboratory.humans.service.TwitterService;
import com.nearfuturelaboratory.humans.twitter.entities.TwitterUser;
import org.bson.types.ObjectId;
//import org.jvnet.hk2.annotations.Optional;
import org.mongodb.morphia.annotations.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.CoreMatchers.equalTo;


@Entity(value="humans",noClassnameStored = false)
// compound index
//@Indexes(@Index(name = "nameAndID", value = "name, id", unique = true))
public class Human  /*extends BaseEntity*/ {

	// uniqueness applies across the collection, even in an embedded document
	// would need a compound index otherwise, cf http://joegornick.com/2012/10/25/mongodb-unique-indexes-on-single-embedded-documents/
	//@Indexed(value = IndexDirection.ASC, name="name"/*, unique = true*/)
	protected String name;

	//@Indexed(value = IndexDirection.ASC, name = "humanid", unique = true/*, sparse = true, dropDups = true*/)
    protected Boolean isYouMan = Boolean.FALSE;

	@Id 
	@Property("humanid")
	protected ObjectId humanid;

	@Embedded("serviceUsers")
	protected List<ServiceUser> serviceUsers = new ArrayList<ServiceUser>();

	@PrePersist void prePersist() {
		if(humanid == null) {
			humanid = new ObjectId();
		}
	}


	public String getName() {
		return name;
	}
	public void setName(String aName) {
		name = aName;
	}
	public List<ServiceUser> getServiceUsers() {
		return serviceUsers;
	}

	@Deprecated
	public void setServiceUsers(List<ServiceUser> aServiceUsers) {
		serviceUsers = aServiceUsers;
	}

	public boolean addServiceUser(ServiceUser aServiceUser) {
		boolean result;
		if(serviceUsers.contains(aServiceUser)) {
			result = false;
		} else {
			result = serviceUsers.add(aServiceUser);
		}
		return result;
	}

	public boolean removeServiceUser(ServiceUser aServiceUser) {
		boolean result;
		result = serviceUsers.remove(aServiceUser);
		return result;
	}

    public void removeAllServiceUsers() {
        Iterator<ServiceUser> iter = serviceUsers.iterator();
        while(iter.hasNext()) {
            iter.next();
            iter.remove();
        }
    }

	public List<ServiceEntry> getServicesThisHumanReliesUpon()
	{
		List<ServiceEntry> results = new ArrayList<ServiceEntry>();
		
		//List<ServiceUser> service_users = this.getServiceUsers();
		// lambdaj syntax..whatever..
		results.add(forEach(getServiceUsers()).getOnBehalfOf());
		
		return results;
	}
	
	public List<ServiceUser> getServiceUsersRelyingOn(ServiceEntry onBehalfOf)
	{
		List<ServiceUser> service_users = 
				select(this.getServiceUsers(),
						having(on(ServiceUser.class).getOnBehalfOf(), equalTo(onBehalfOf)));
				
		return service_users;
	}
	
	public boolean removeServiceUsersByServiceEntry(ServiceEntry onBehalfOf)
	{
		boolean result = false;
		List<ServiceUser> service_users = 
				select(serviceUsers,
						having(on(ServiceUser.class).getOnBehalfOf(), equalTo(onBehalfOf)));
				
				
		for(ServiceUser service_user : service_users) {
			boolean b = this.removeServiceUser(service_user);
			if(b == true) {
				result = true;
			} else {
				result = false;
				break;
			}
		}
		return result;
	}
    /**
     * Remove a service user by its Mongo DB ID
     * @param aId a valid Mongo DB ID
     * @return
     */
	public boolean removeServiceUserById(String aId) {
		boolean result;
		ServiceUser serviceUser = selectUnique(serviceUsers, having(on(ServiceUser.class).getId(), equalTo(new ObjectId(aId))));
		result = serviceUsers.remove(serviceUser);
		return result;
	}

    /**
     * Get a service user by its Mongo DB ID
     * @param aId a valid Mongo DB ID
     * @return
     */
	public ServiceUser getServiceUserById(String aId) {
		ServiceUser serviceUser = selectUnique(serviceUsers, having(on(ServiceUser.class).getId(), equalTo(new ObjectId(aId))));
		return serviceUser;
	}

    /**
     * Get a service user by the ID the service has assigned it, eg an Instagram user id
     * @param aServiceUserId
     * @return
     */
    public ServiceUser getServiceUserByServiceUserId(String aServiceUserId) {
        ServiceUser serviceUser = selectUnique(serviceUsers, having(on(ServiceUser.class).getServiceUserID(), equalTo(aServiceUserId)));
        return serviceUser;
    }

	public String getId() {
        if(humanid != null) {
            return humanid.toString();
        } else {
            return null;
        }
	}

    public void setYouMan(boolean _isYouMan) {
        this.isYouMan = _isYouMan;
    }

    public boolean isYouMan() {
        return this.isYouMan;
    }
	/**
	 * Really only for testing
	 * @param aId
	 */
	public void setId(String aId) {
		humanid = new ObjectId(aId);
	}

	protected ObjectId getHumanid() {
		return humanid;
	}
	protected void setHumanid(ObjectId aHumanid) {
		humanid = aHumanid;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Human [name=" + name + ", humanid=" + humanid
				+ ", serviceUsers=" + serviceUsers + ", isYouMan="+isYouMan+"]";
	}


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Human human = (Human) o;
        if(humanid == null) return false;
        if(human.humanid == null) return false;
        if (!humanid.equals(human.humanid)) return false;
        if (!isYouMan.equals(human.isYouMan)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = isYouMan.hashCode();
        result = 31 * result + humanid.hashCode();
        return result;
    }

    public void fixImageUrls()
    {
        for(ServiceUser service_user : this.serviceUsers) {
            fixImageUrls(service_user);
        }
    }


    protected ServiceUser fixImageUrls(ServiceUser aServiceUser) {
		ServiceUser result = aServiceUser;
		ServiceEntry se = aServiceUser.getOnBehalfOf();
		if(aServiceUser.getServiceName().equalsIgnoreCase("instagram")) {
			try {
				InstagramService instagram = InstagramService.createServiceOnBehalfOfUsername(se.getServiceUsername());
				InstagramUser u = instagram.serviceRequestUserBasicForUserID(aServiceUser.getServiceUserID());
				aServiceUser.setImageURL(u.getImageURL());
				result = aServiceUser;
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		if(aServiceUser.getServiceName().equalsIgnoreCase("twitter")) {
			try {
				TwitterService service = TwitterService.createTwitterServiceOnBehalfOfUsername(se.getServiceUsername());
				TwitterUser u = service.serviceRequestUserBasicForUserID(aServiceUser.getServiceUserID());
				aServiceUser.setImageURL(u.getImageURL());
				result = aServiceUser;
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		if(aServiceUser.getServiceName().equalsIgnoreCase("foursquare")) {
			try {
				FoursquareService service = FoursquareService.createFoursquareServiceOnBehalfOfUserID(se.getServiceUserID());
				FoursquareUser u = service.serviceRequestUserBasicForUserID(aServiceUser.getServiceUserID());
				aServiceUser.setImageURL(u.getImageURL());
				result = aServiceUser;
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		if(aServiceUser.getServiceName().equalsIgnoreCase("flickr")) {
			try {
				FlickrService service = FlickrService.createFlickrServiceOnBehalfOfUserID(se.getServiceUserID());
				FlickrUser u = service.serviceRequestUserBasicForUserID(aServiceUser.getServiceUserID());
				aServiceUser.setImageURL(u.getImageURL());
				result = aServiceUser;
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		return result;
	}

}
