if (Meteor.isClient) {
  Template.hello.greeting = function () {
    return "Welcome to Backend.";
  };

  Template.hello.events({
    'click input': function () {
      // template data, if any, is available in 'this'
      if (typeof console !== 'undefined')
        console.log("You pressed the button");
    }
  });
}

if (Meteor.isServer) {

Users = new Meteor.Collection('users');
Events = new Meteor.Collection('events');

Router.map(function () {
  this.route("/", {
    where: "server",
    action: function(){
      console.log('################################################');
      console.log(this.request.method);
      console.log(this.request.headers);

      console.log('------------------------------');
      console.log(this.request.body);
      console.log('------------------------------');

      this.response.statusCode = 200;
      this.response.setHeader("Content-Type", "application/json");
      this.response.setHeader("Access-Control-Allow-Origin", "*");
      this.response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");

      if (this.request.method == 'POST') {
				HandleData(this.request.body);
      }
    }
  });
});

var HandleData = function(query)
{
 	console.log("data received");
	console.log(query);
	
	if(query.username != undefined && query.regId != undefined && Users.findOne({name:query.to}) == undefined)
	{
		Users.insert({name: query.username, regId: query.regId});	
		console.log("New user: " + query.username);	
	}
	else if(query.username != undefined && query.regId != undefined)
	{
		Users.update({name: query.username}, {regId: query.regId});	
		console.log("Updated user: " + query.username);	
	}
	
	/*var nodegcm = Npm.require('node-gcm');

	// create a message with default values
	var message = new nodegcm.Message();
	
	// or with object values
	var message = new nodegcm.Message({
	    collapseKey: 'demo',
	    delayWhileIdle: true,
	    timeToLive: 3,
	    data: {
	        key1: 'message1',
	        key2: 'message2'
	    }
	});
	
	var sender = new nodegcm.Sender('AIzaSyAk_PxK_3WfDeFQOL0fDpPpqaA5scekrEk');
	var registrationIds = [];
	
	// OPTIONAL
	// add new key-value in data object
	message.addDataWithKeyValue('key1','message1');
	message.addDataWithKeyValue('key2','message2');
	
	// or add a data object
	message.addDataWithObject({
	    key1: 'message1',
	    key2: 'message2'
	});
	
	// or with backwards compability of previous versions
	message.addData('key1','message1');
	message.addData('key2','message2');
	
	
	message.collapseKey = 'demo';
	message.delayWhileIdle = true;
	message.timeToLive = 3;
	message.dryRun = true;
	// END OPTIONAL
	
	// At least one required
	registrationIds.push(query.regId);
*/
	/**
	 * Params: message-literal, registrationIds-array, No. of retries, callback-function
	 **/
	/*sender.send(message, registrationIds, 4, function (err, result) {
	    console.log(result);
	});*/
	var toUsr = Users.findOne({name:query.to});
	console.log(toUsr);
	console.log(toUsr.name);
	if(toUsr.regId && query.event)
	{
		var GCM = Npm.require('gcm').GCM;
	
		var apiKey = 'AIzaSyAk_PxK_3WfDeFQOL0fDpPpqaA5scekrEk';
		var gcm = new GCM(apiKey);
	
		var mess = {
	   	registration_id: toUsr.regId , // required
	    	"data.event": "{\
	    		privacy:\""+query.event.privacy+"\",\
	    		hour:\""+query.event.hour+"\",\
	    		minute:\""+query.event.minute+"\",\
	    		date:\""+query.event.date+"\",\
	    		host:\""+query.event.host+"\"\
	    	}"
		};
	
		gcm.send(mess, function(err, messageId){
	    	if (err) {
	    		console.log("Something has gone wrong!");
	    	} else {
	        	console.log("Sent with message ID: ", messageId);
	    	}
		});
	}
}

}
