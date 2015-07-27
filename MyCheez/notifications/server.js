
var Firebase = require('firebase');
// var myRootRef = new Firebase('https://torrid-inferno-8611.firebaseio.com/mycheez/users');
// myRootRef.set("hello world!");

// Get a reference to our posts
var ref = new Firebase("https://torrid-inferno-8611.firebaseio.com/mycheez/users");

// Get the data on a post that has changed
ref.on("child_changed", function(snapshot) {
  var changedPost = snapshot.val();
  console.log("The updated post title is " + changedPost.name);
});



// var gcm = require('node-gcm');

// var message = new gcm.Message();

// message.addData('key1', 'msg1');



// var msg = {
// 	"data":{
// 		"message":"Hello world!"
// 	}
// };


// var sender = new gcm.Sender('AIzaSyD4QRxJV4ZIvrgq4IFe23wFLrkZYR9rnho');
 
// // set api key 
// //sender.setAPIKey('AIzaSyD4QRxJV4ZIvrgq4IFe23wFLrkZYR9rnho');

// console.log("About to send the message");

// var ids = ["cu5aTFqiwV4:APA91bHeX2_eiwr8gruW5ak17oCWiTMFLOKnGsCgM1G1fki8yt0xW65pZUyUeGb7pP6OWlJEkCfpWHYS-LuskC2d1pfvj3oEezOWvsgTxcMViQ3tT4LpDfutuLu1fL0zrrxi7QFTJlMZ"];
// console.log("Message look like : " + JSON.stringify(message));

// sender.send(message, ids, 10, function(err, data) {
//     if (!err) {
// 	       console.log("SUCESS " + data); 
//     } else {
//         console.log("ERROR : " + err); 
//     }
// });

