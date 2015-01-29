package nondistributed;

import java.util.concurrent.Future;

import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;

@Stateless
@Local(NotificationHandler.class)
@LocalBean
public class NotificationHandlerImpl implements NotificationHandler{

	@Override
	public Future<Notification> checkForUpdates(String userId) {

		return null;
	}

	@Override
	public void update(String userId, String toUserId, Notification notification) {

	}

}
