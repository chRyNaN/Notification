package nondistributed;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.json.JSONException;
import org.json.JSONObject;

@WebServlet(urlPatterns = {"/channel"}, asyncSupported = true)
public class ChannelServlet extends HttpServlet{
	private static final long serialVersionUID = -7987778642195559852L;
	private ConcurrentMap<String, AsyncContext> contexts = new ConcurrentHashMap<>();
	@Inject
	NotificationHandler handler;
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/plain");
				
		//obtain userId from the request
		final String userId = request.getParameter("userId");
		if (userId == null){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		if (contexts == null || contexts.isEmpty() || !contexts.containsKey(userId)){
			addContext(userId, request, response);
		}else{
			removeContext(userId);
			addContext(userId, request, response);
		}
		
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userId = null;
		String toUserId = null;
		Notification n = null;
		try {
			userId = request.getParameter("userId");
			JSONObject obj = new JSONObject(request.getParameter("notification"));
			n = new Notification();
			
			if(obj.has("sendTo")){
				toUserId = obj.getString("sendTo");	
				if(obj.has("type")){
					n.setType(obj.getString("type"));
				}
				if(obj.has("details")){
					n.setDetails(obj.getString("details"));
				}
			}else{
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			
			//add notification to the database
			handler.update(userId, toUserId, n);
			//if user is online, send notification to them 
			this.sendNotification(toUserId, n);
		} catch (Exception e) {
			e.printStackTrace();
			//For whatever reason we couldn't send the notification
			response.sendError(HttpServletResponse.SC_CONFLICT);
			return;
		}
		
	}
	
	protected void sendNotification(String userId, Notification notification){
		System.out.println("sendNotification method");
		try{
			if(contexts.containsKey(userId)){
				AsyncContext asyncContext = contexts.get(userId);
				PrintWriter writer = asyncContext.getResponse().getWriter();
				writer.write(notification.toJSONString());
				writer.flush();
				asyncContext.complete();
				removeContext(userId);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private void addContext(String userId, HttpServletRequest request, HttpServletResponse response){
		if (contexts == null){
			contexts = new ConcurrentHashMap<String, AsyncContext>();
		}
		AsyncContext context = request.startAsync(request, response);
		context.setTimeout(10 * 60 * 1000);//timeout ten minutes
		contexts.put(userId, context);
		//check for updates from the database just in case an event has occurred while the user wasn't connected
		try{
			Future<Notification> futureNotification = handler.checkForUpdates(userId);
			Notification n;
			if(futureNotification != null) {
				n = futureNotification.get();
				if(n != null){
					sendNotification(userId, n);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void removeContext(String userId){
		if(contexts.containsKey(userId)){
			contexts.remove(userId);
		}
	}
	
}
