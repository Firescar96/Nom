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
      //console.log(this.request.method);
      //console.log(this.request.headers);

      //console.log('------------------------------');
      console.log(this.request.body);
      //console.log('------------------------------');

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

var GCM = Npm.require('gcm').GCM;
		
var apiKey = 'AIzaSyAk_PxK_3WfDeFQOL0fDpPpqaA5scekrEk';
var gcm = new GCM(apiKey);
			
var HandleData = function(query)
{
	//Users.remove({});
	
	if(query.location != undefined)
	{
		Users.update({name: query.host}, {$set: {location: query.location}});	
		console.log("Updated user: " + query.host + " location");
		//console.log(Users.find({}).fetch());
		return;
	}	

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
		
		return;
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
	if(query.event != undefined)
	{
		if (query.event.privacy == "open") 
		{
			var toUsr = Users.find({}).fetch();
		} else if(query.chat == "true")
		{
			var toUsr = Users.find({}).fetch();
		} else	
		{
			var nxtUsr = query.to.split(',');
			console.log(nxtUsr);
			var toUsr = [];
			for(var i in nxtUsr)
			{
			console.log(nxtUsr[i]);
				toUsr.push(Users.findOne({name:nxtUsr[i]}));
			}
		}
		
		if(Events.findOne({hash:query.event.hash}) == undefined)
		{
			Events.insert({
				privacy:query.event.privacy,
	    		location:query.event.location,
	    		date:query.event.date,
	    		hash:query.event.hash,
	    		host:query.event.host
		    });
		}
	}
	
	if(query.hash != undefined)
	{
		var toUsr = [];
		toUsr.push(Users.findOne({name:query.to}));
	}	
	
	for(var i in toUsr)
	{
		console.log(toUsr[i]);
		if (toUsr[i] == null) 
			continue;
		
		if(query.event != undefined)
			var hostUsr = Users.findOne({name:query.event.host});
		else 
			var hostUsr = Users.findOne({name:query.host});
		//console.log(query.event.host);
		//console.log(toUsr[i].location);
		//console.log(Users.findOne({name:query.event.host}));
		if(toUsr[i].location.latitude != undefined)
		{
			toUsr[i].location.latitude;
			var latPow = Math.pow(parseInt(hostUsr.location.latitude)-parseInt(toUsr[i].location.latitude),2);
			var longPow = Math.pow(parseInt(hostUsr.location.longitude)-parseInt(toUsr[i].location.longitude),2);
			var dist = Math.sqrt(latPow+longPow);
			if(dist > 1)
				continue;
		}	
			
		if(query.chat == "true")
		{
			var mess = {
		   	registration_id: toUsr[i].regId , // required
		    	"data.chat": "{\
		    		message:\""+query.message+"\",\
		    		author:\""+query.author+"\",\
		    		date:\""+query.date+"\",\
		    		location:\""+query.location+"\",\
		    		host:\""+query.host+"\"\
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
	
		if(toUsr[i].regId && query.event != null)
		{
			var mess = {
		   	registration_id: toUsr[i].regId , // required
		    	"data.event": "{\
		    		privacy:\""+query.event.privacy+"\",\
		    		location:\""+query.event.location+"\",\
		    		date:\""+query.event.date+"\",\
		    		hash:\""+query.event.hash+"\",\
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
		
		if(toUsr[i].regId && query.hash!= null)
		{
			var curEve = Events.findOne({hash:query.hash});
			var mess = {
		   	registration_id: toUsr[i].regId , // required
		    	"data.event": "{\
		    		privacy:\""+curEve.privacy+"\",\
		    		location:\""+curEve.location+"\",\
		    		date:\""+curEve.date+"\",\
		    		hash:\""+curEve.hash+"\",\
		    		host:\""+curEve.host+"\"\
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
