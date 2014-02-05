package com.nearfuturelaboratory.humans.rest;

import javax.servlet.ServletContext;

import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.humans.entities.HumansUser;
import com.nearfuturelaboratory.humans.entities.InvalidUserException;

public class RestCommon {

	protected HumansUser getUserForAccessToken(/*ServletContext context, */String access_token) throws InvalidAccessTokenException
	{

		if(access_token == null) {
			throw new InvalidAccessTokenException("invalid access token");
		}
		HumansUser user;
		HumansUserDAO dao = new HumansUserDAO();//(HumansUserDAO)context.getAttribute("dao");
//		if(dao == null) {
//			dao = new HumansUserDAO();
//			context.setAttribute("dao", dao);
//		}

		user = dao.findOneByAccessToken(access_token);//(HumansUser)context.getAttribute(access_token+"_user");


		//		HttpSession session = request.getSession();
		//		logger.debug(session.getId());
		//		HumansUser user = (HumansUser)session.getAttribute(access_token);
		if(user == null) {
			user = dao.findOneByAccessToken(access_token);
			if(user == null) {
				throw new InvalidAccessTokenException("invalid access token");
			}
		}
		//MongoUtil.getMongo().getConnector().close();
		//logger.debug("dao = "+dao);

		return user;
	}

}
