(function(){
	/** Socket object will handle the connecting, disconnecting, and message transfer to the Servlet; low-level.
	 *  Channel object will use the Socket object to send and receive the notifications from the Servlet; high-level.
	 *  Notification object will be a simple data structure holding the notification Type, Details, and sendTo attributes. 
	 *  Notification Types: Request, Response, Invite, Update, Message. 
	 *  Notification Details: meant to only be a little information about the notification; will be sent for the user to access;
	 *  can be in JSON format, though will not be parsed into an object. */
	
	//Oh no, I'm using constructors instead of Object literals to define my Objects! Damn my Java ways!
	var Socket = function(location, userId){
		if(!(this instanceof Socket)){
			sendError("Forgot new keyword when instantiating Socket object.");
		}
		var that = this; //for accessing within inner functions with a different scope.
		function connect(){
			var xhr = new XMLHttpRequest();
			xhr.open("get", location + "?userId=" + userId, true);
			xhr.setRequestHeader("content-type", "application/x-www-form-urlencoded");
			xhr.onreadystatechange = function(event){
				if(xhr.readyState == 4 && xhr.status == 200){
					if(typeof that.onmessage !== "undefined"){
						try{
							var msg = JSON.parse(xhr.responseText);
							if(typeof msg.details !== "undefined"){
								var n = new Notification(msg.type, msg.details);
							}else{
								var n = new Notification(msg.type);
							}
						}catch(error){
							var n = xhr.response;
						}
						that.onmessage(n);
						connect();
					}
				}
			};
			xhr.send();
		}
		
		function send(notification){
			var xhr = new XMLHttpRequest();
			xhr.open("post", location + "?userId=" + userId, true);
			xhr.setRequestHeader("content-type", "application/x-www-form-urlencoded");
			xhr.readystatechange = function(event){
				if(xhr.readyState == 4){
					if(xhr.source != 200){
						//error sending notification
					}
				}
			};
			xhr.send("notification=" + JSON.stringify(notification));
		}
		
		connect();
		this.send = send;
		this.onmessage;
		
		Object.seal(this);
	};
	
	var Channel = function(location, userId){
		if(!(this instanceof Channel)){
			sendError("Forgot new keyword when instantiating Channel object.");
		}
		var that = this;
		if(isUndefined(location)) sendError("'location' parameter of Channel object must be defined.");
		if(isUndefined(userId)) sendError("'userId' parameter of Channel object must be defined.");
		this.onrequest;
		this.onresponse;
		this.oninvite;
		this.onupdate;
		this.onmessage;
		
		var socket = new Socket(location, userId);
		socket.onmessage = function(notification){
			if(typeof notification === "undefined" || typeof notification.type === "undefined"){
				console.error("Notification or notification.type = undefined, in onmessage method.");
				return;
			}
			console.log(JSON.stringify(notification));
			switch(notification.type){
			case "request":
				if(typeof that.onrequest !== "undefined") that.onrequest(notification);
				break;
			case "response":
				if(typeof that.onresponse !== "undefined") that.onresponse(notification);
				break;
			case "invite":
				if(typeof that.oninvite !== "undefined") that.oninvite(notification);
				break;
			case "update":
				console.log("case update");
				if(typeof that.onupdate !== "undefined") that.onupdate(notification);
				break;
			case "message":
				if(typeof that.onmessage !== "undefined") that.onmessage(notification);
				break;
			}
		};
		this.sendNotification = socket.send;
	};
	
	var Notification = function(type, details, sendTo){
		//toUserId is an optional parameter used when sending a notification
		//catch the "forgot new keyword" error
		if(!(this instanceof Notification)){
			sendError("Forgot new keyword when instantiating Notification object.");
		}
		this.type = (typeof type === "string") ? type.toLowerCase() : type;
		this.details = details;
		if(typeof this.type !== "string"){
			sendError("'type' property of Notification object must be of type String.");
		}
		this.toString = function(){
			return "[Object Notification: { " + "Type: " + this.type + ", Details: " + this.details + "}]";
		};
		Object.seal(this);
	};
	
	function sendError(errorMessage){
		if(typeof console !== "undefined"){
			console.error(errorMessage);
		}
		throw errorMessage;
	}
	
	function sendErrorWithoutThrow(errorMessage){
		if(typeof console !== "undefined"){
			console.error(errorMessage);
		}
	}
	
	function isUndefined(obj){
		if(typeof obj === "undefined"){
			return true;
		}else{
			return false;
		}
	}
	
	window.Notification = Notification;
	window.Channel = Channel;
	
})();