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

Router.map(function () {
  this.route("/", {
    where: "server",
    action: function(){
      console.log('################################################');
      console.log(this.request.method);
      console.log(this.request.headers);
      console.log('this.params.id: ' + this.params.id);

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
	
	var gcm = Npm.require('node-gcm');

	// create a message with default values
	var message = new gcm.Message();
	
	// or with object values
	var message = new gcm.Message({
	    collapseKey: 'demo',
	    delayWhileIdle: true,
	    timeToLive: 3,
	    data: {
	        key1: 'message1',
	        key2: 'message2'
	    }
	});
	
	var sender = new gcm.Sender('AIzaSyAk_PxK_3WfDeFQOL0fDpPpqaA5scekrEk');
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
	/**
	 * Params: message-literal, registrationIds-array, No. of retries, callback-function
	 **/
	sender.send(message, registrationIds, 4, function (err, result) {
	    console.log(result);
	});
}

if (Meteor.isServer) {
  Meteor.startup(function () {
  });
}
