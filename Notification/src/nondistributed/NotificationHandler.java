package nondistributed;

import java.util.concurrent.Future;

import javax.ejb.Asynchronous;

public interface NotificationHandler {
	
	@Asynchronous
	public Future<Notification> checkForUpdates(String userId);
	@Asynchronous
	public void update(String userId, String toUserId, Notification notification);
}
