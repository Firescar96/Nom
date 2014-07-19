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
  
  rests = new Meteor.Collection("RESTS")

	Meteor.subscribe(function() {
 		rests.find().observe({
   		added: function(item){
				window.location = "com.firescar96.nom.appUser";
    		}
 	 	});
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

      if (this.request.method == 'GET') {
      	/*Collection("REST")

			Meteor.publish("RESTS", function(){
			 rests.find();
			});
			
			rests.remove({}); // remove all
			rests.insert({message: "Some message to show on every client."});
      		console.log("gotten");*/
      	if(this.request.query.checkName != null)
      	{
      		console.log(this.request.query.checkName);
      		var resUsr = Users.findOne({name:this.request.query.checkName});
      		var exists = resUsr != undefined;
      		console.log(exists);
      		this.response.writeHead(200, {'Content-Type': 'text/plain'});
      		if(exists)
      		{
      			if(resUsr.regId == this.request.query.regId)
      				this.response.end("true");
      			else
      				this.response.end("false");
      		}
      		else
      			this.response.end("true");
			}      
      }
    }
  });
});

var HandleData = function(query)
{
	if(query.host != undefined && query.regId != undefined && Users.findOne({name:query.host}) == undefined)
	{
	var editUsr = Users.findOne({regId:query.regId});
		if(editUsr == undefined)
		{
			Users.insert({name: query.host, regId: query.regId});	
			console.log("New user: " + query.host);	
		}else 
		{
			Users.update({regId: query.regId}, {$set: {name: query.host}});	
			console.log("Updated user: " + query.host + " found using regId");	
		}
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
	if (query.event.privacy == "open") 
	{
		var toUsr = Users.find({}).fetch();
	} else	
	{
		var nxtUsr = query.to.split(',');
		console.log(nxtUsr);
		console.log(nxtUsr[i]);
		var toUsr = [];
		for(var i in nxtUsr)
		{
			toUsr.push(Users.findOne({name:nxtUsr[i]}));
		}
	}
	
	for(var i in toUsr)
	{
		console.log(toUsr[i]);
		if (toUsr[i] == null) 
			continue;
		else
			console.log("here");
			
		if(toUsr[i].regId && query.event)
		{
			var GCM = Npm.require('gcm').GCM;
		
			var apiKey = 'AIzaSyAk_PxK_3WfDeFQOL0fDpPpqaA5scekrEk';
			var gcm = new GCM(apiKey);
		
			var mess = {
		   	registration_id: toUsr[i].regId , // required
		    	"data.event": "{\
		    		privacy:\""+query.event.privacy+"\",\
		    		hour:\""+query.event.hour+"\",\
		    		minute:\""+query.event.minute+"\",\
		    		location:\""+query.event.location+"\",\
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
}
