//================================================================= 
//                                                                  
// Licensed Materials - Property of IBM
//
// (C) COPYRIGHT International Business Machines Corp. 2013
// All Rights Reserved
//
// US Government Users Restricted Rights - Use, duplication or
// disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
//
//================================================================ 

/*jshint laxcomma:true */
/*jshint eqeqeq:true */
/*jshint curly:true */
/*jshint undef:true */
/*jshint unused:true */
/*jshint node: true */
/*jshint bitwise: true */
/*jshint forin: true */
/*jshint strict: true */
"use strict";

// install node.js  :  yum install node
// install optimist : npm install optimist 
// install async    : npm install async 
// to run  : node cluster.js --help

// cpe function wrapper avoids poluting the global namespace.
// IIFE pattern - Immediately-invoked Function Expression  
(function cpe () {
var https = require("https") 
, config = require('./cpeconfig.json')
, async = require("async")
, argv = require("optimist").argv
, fs = require('fs')
, httpRequest
, assert = require('assert')
, loop = {
	count : 1, 
	forever : false, 
	state : "CREATE",  // "WAIT", "DELETE"
	statusCode : 200,  // http status 
	response : "data", 
	currentStep : "NONE",
	id : 0
}
,  // Information to use when building a GetClusterDetails http request.
   getClusterDetailsOptions = {
      host : config.cpe.host,
      port : config.cpe.port,
      path : config.cpe.path + '/clusters/',
      method : 'GET',
      headers : {
    	  'subscriber-id' : config.cpe.subscriberid,
    	  'api-key' : config.cpe.apikey        	 
      }
   }
,  // The details returned from the GetClusterDetails command.
   getClusterDetailsData = {
      id : "",
      name : "",
      description : "",
      creator : "",
      version : "",
      ownerAccount : {
         id : "",
         name : "",
         description : ""
      },
      clusterDefinition : {
         id : "",
         name : "",
         description : ""
      },
      dataTable : [],
      startDate : "2000-06-28T15:30:00EDT",
      endDate :   "2000-06-28T15:31:00EDT",
      requestDate :   "2000-06-28T15:30:00EDT",
      deploymentVariables : [],
      tiers : [],
      state : "",
      applicationAction : ""
   }
,  // Information to use when building a List Clusters http request.
   listClustersOptions = {
      host : config.cpe.host,
      port : config.cpe.port,
      path : config.cpe.path + '/clusters',
      method : 'GET',
      headers : {
         'subscriber-id' : config.cpe.subscriberid,
         'api-key' : config.cpe.apikey        	 
      }
   }
,
// Information to use when building a CreateCluster http request.
   createClusterOptions = {
      host : config.cpe.host,
      port : config.cpe.port,
      path : config.cpe.path + '/clusters',
      method : 'POST',
      headers : {
          'subscriber-id' : config.cpe.subscriberid,
          'api-key' : config.cpe.apikey,
          'Content-Type' : 'application/json'
       }
   }
,  // The details used as input to the CreateCluster command.
   createClusterInputData = {
      'name'         : 'clustername',
      'description'  : 'clusterdescription',
      'size' : 2,
      'parameters' : {
    	  'cpeLocationName' : 'wdc01'
      }
   }
,  // Information to use when building a DeleteCluster http request.
   deleteClusterOptions = {
      host : config.cpe.host,
      port : config.cpe.port,
      path : config.cpe.path + '/clusters/',
      method : 'DELETE',
      headers : {
    	  'subscriber-id' : config.cpe.subscriberid,
    	  'api-key' : config.cpe.apikey        	 
      }
   }
,  // Information to use when building an UpdateCluster http request.
   updateClusterOptions = {
      host : config.cpe.host,
      port : config.cpe.port,
      path : config.cpe.path + '/clusters/',
      method : 'PUT',
      headers : {
         Authorization : 'Basic ' + new Buffer(config.cpe.user + ':' + config.cpe.password).toString('base64'),
         user : config.cpe.user,
         password : config.cpe.password,
         'content-type' : 'application/json'
      }
   }
, // Cluster Methods
  cluster = { 
	list: function() {
		console.log("List Clusters");
		console.log("host: "   + listClustersOptions.host);
		console.log("port: "   + listClustersOptions.port);
		console.log("path: "   + listClustersOptions.path);
		console.log("method: " + listClustersOptions.method);
		
		httpRequest = https.request(listClustersOptions, function(res){
			var httpResponseDataAsStr = '';
			res.on('data', function (chunk) {
				httpResponseDataAsStr += chunk;
	        });	
			res.on('end', function () {
				assert.notStrictEqual(res, null);
	            assert.notStrictEqual(res, undefined);
	            //if (res.statusCode !== 200) {
	            //   console.log("\nListClusters response body = " + httpResponseDataAsStr);
	            //   assert.equal(res.statusCode, 200);
	            //}
	            // Put the response's data into the object.
	            // console.log('http status code: ' + res.statusCode);
	            console.log(httpResponseDataAsStr);
	            // console.log("currentStep:" + httpResponseDataAsStr[0]["currentStep"])
	        });
		});
		httpRequest.end();
		httpRequest.on('error', function(error) { assert.equal(1, 2, error); });
	}, 
	details: function(id, waitcb) {
		if (waitcb === null) console.log("Get Cluster Details");
		getClusterDetailsOptions.path = config.cpe.path + '/clusters/' + id;
	      
	    // console.log("host: "   + getClusterDetailsOptions.host);
	    // console.log("port: "   + getClusterDetailsOptions.port);
	    // console.log("path: "   + getClusterDetailsOptions.path);
	    // console.log("method: " + getClusterDetailsOptions.method);

	    httpRequest = https.request(getClusterDetailsOptions, function(res) {
	    	var httpResponseDataAsStr = '';
	    	res.on('data', function (chunk) {
	            httpResponseDataAsStr += chunk;
	    	});
	    	res.on('end', function () {
	    		assert.notStrictEqual(res, null);
	            assert.notStrictEqual(res, undefined);
	            //if (res.statusCode !== 200) {
	            //   console.log("\nGetClusterDetails response body = " + httpResponseDataAsStr);
	            //   assert.equal(res.statusCode, 200);
	            //}
	            // Put the response's data into the object.,
	            // getClusterDetailsData = JSON.parse(httpResponseDataAsStr);
	            if (waitcb !== null) { 
	            	loop.statusCode = res.statusCode;
	            	// verify the status
				   if (loop.state === "WAIT" && loop.statusCode === 200) {
					   loop.response = JSON.parse(httpResponseDataAsStr);
					   loop.currentStep = loop.response["currentStep"];
					   if (loop.currentStep) {
						   console.log("loop.currentStep: " + loop.currentStep);
					   } else {
						   console.log(httpResponseDataAsStr);
					   }
					   if (loop.currentStep && loop.currentStep === "NONE") {
						   loop.state = "DELETE";
					   } 
				   }
				   // wait 10 seconds before sending the results  
				   setTimeout(function() {
					   waitcb(null, loop.statusCode);
				   }, 10000);
				   return;
	            } else {
	               console.log(httpResponseDataAsStr);
	            }
	    	});
	    });
	    httpRequest.end();
	    httpRequest.on('error', function(error) { assert.equal(1, 2, error); });
	},
	deletecluster: function(id, callback) {
		// console.log("Delete Cluster");
		deleteClusterOptions.path = config.cpe.path + '/clusters/' + id;
		
		// console.log("host: "   + deleteClusterOptions.host);
		// console.log("port: "   + deleteClusterOptions.port);
		// console.log("path: "   + deleteClusterOptions.path);
		// onsole.log("method: " + deleteClusterOptions.method);
		
		httpRequest = https.request(deleteClusterOptions, function(res) {
			var httpResponseDataAsStr = '';
			res.on('data', function (chunk) {
				httpResponseDataAsStr += chunk;
			});
			res.on('end', function () {
	            assert.notStrictEqual(res, null);
	            assert.notStrictEqual(res, undefined);
	            //if (res.statusCode !== 202) {
	            //   console.log("\DeleteCluster response body = " + httpResponseDataAsStr);
	            //   assert.equal(res.statusCode, 202);  // return code 202 means that the request has been accepted for processing, but the processing has not been completed.
	            //}
	            // Put the response's data into the object.,
	            // console.log('http status code: ' + res.statusCode);
	            loop.statusCode = res.statusCode;
	            loop.response = JSON.parse(httpResponseDataAsStr);
	            console.log(loop.response);
	    		if (callback != null) callback(null, loop.statusCode);
			});
		});
		httpRequest.end();
		httpRequest.on('error', function(error) { assert.equal(1, 2, error); });
	}, 
	create: function(callback) {	      
	      console.log("Create Cluster");
	      
	      // console.log("host: "   + createClusterOptions.host);
	      // console.log("port: "   + createClusterOptions.port);
	      // console.log("path: "   + createClusterOptions.path);
	      // console.log("method: " + createClusterOptions.method);
	      
	      //----------------------------------------------------------------------
	      // Send the request.
	      //----------------------------------------------------------------------
	      httpRequest = https.request(createClusterOptions, function(res) {
	         var httpResponseDataAsStr = '';
	         res.on('data', function (chunk) {
	            httpResponseDataAsStr += chunk;
	         });
	         res.on('end', function () {
	            assert.notStrictEqual(res, null);
	            assert.notStrictEqual(res, undefined);
	            //if (res.statusCode !== 201) {
	            //   console.log("\nCreateCluster response body = " + httpResponseDataAsStr);
	            //   assert.equal(res.statusCode, 201);
	            //}
	            // Put the response's data into the response data object.
	            loop.statusCode = res.statusCode;
	            console.log('http status code: ' + loop.statusCode);
	            if (loop.statusCode === 200) {
		            loop.response = JSON.parse(httpResponseDataAsStr);
	            	loop.id = loop.response["detail"]; 
	            	console.log("cluster id: " + loop.id);
	            }
	            else {
	            	console.log(httpResponseDataAsStr);
	            }
	            // Ensure the data is as expected.
	  	      	if (callback != null) callback(null, loop.statusCode);
	            //assert.equal(createClusterResponseObject.uri, "https://" + createClusterOptions.host + ":" + createClusterOptions.port + createClusterOptions.path + '/' + createClusterResponseObject.id);
	         });
	      });
	      // Actually write the "input" data for this CreateCluster in to the http request!
	      // if (argv.file) {
	      //     httpRequest.write(createClusterStringFromFile);
	      // }
	      // else {
	      httpRequest.write(JSON.stringify(createClusterInputData));
	      // }
	      httpRequest.end();
	      httpRequest.on('error', function(error) { assert.equal(1, 2, error); });
	}
  }
;

    
   // Avoid DEPTH_ZERO_SELF_SIGNED_CERT error for self-signed certs
   process.env.NODE_TLS_REJECT_UNAUTHORIZED = "0";

   //--------------------------------------------------------------------------
   // ListClusters (get a list of existing clusters in the system).
   //--------------------------------------------------------------------------
   if (argv.list) {
	   cluster.list();
   }
   //--------------------------------------------------------------------------
   // GetClusterDetails (get the details on the specified cluster).
   //--------------------------------------------------------------------------
   else if (argv.get) {
	   cluster.details(argv.get, null);
   }
   //--------------------------------------------------------------------------
   // DeleteCluster (deletes the specified cluster id).
   //--------------------------------------------------------------------------
   else if (argv.deletecluster) {
	   cluster.deletecluster(argv.deletecluster, null);
   }
   //--------------------------------------------------------------------------
   // CreateCluster (creates a new cluster).
   //--------------------------------------------------------------------------
   else if (argv.create) {
      // Parse input parms.
      // var createClusterStringFromFile;
      // if (argv.file)
      // {
      //    createClusterStringFromFile = fs.readFileSync(argv.file, 'utf8');
      // }

      if (argv.name) {
    	  // Put out a message indicating what value the user specified for this field.
    	  console.log("new cluster's name: " + argv.name);
    	  // Update the field in the input data structure.
    	  createClusterInputData.name = argv.name;
      }
      if (argv.descr) {
    	  // Put out a message indicating what value the user specified for this field.
    	  console.log("new cluster's description: " + argv.descr);
    	  // Update the field in the input data structure.
    	  createClusterInputData.description = argv.descr;
      }
      if (argv.size) {
    	  // Put out a message indicating what value the user specified for this field.
    	  console.log("new cluster's size: " + argv.size);
    	  // Update the field in the input data structure.
    	  createClusterInputData.size = argv.size;
      }
      if (argv.location) {
    	  // Put out a message indicating what value the user specified for this field.
    	  console.log("new cluster's location: " + argv.location);
          // Update the field in the input data structure.
    	  createClusterInputData.parameters.cpeLocationName = argv.location;
      }
      cluster.create(null);
   }
   //--------------------------------------------------------------------------
   // loop: create cluster, wait to ready, delete cluster
   //--------------------------------------------------------------------------
   else if (argv.loop) {
	   loop.count = argv.loop;
	   loop.forever = false; 
	   if (loop.count === 0) {
		   loop.forever = true;
	   }

	   // process the create cluster parameters
	   if (argv.name) {
		   // Put out a message indicating what value the user specified for this field.
		   console.log("new cluster's name: " + argv.name);
		   // Update the field in the input data structure.
		   createClusterInputData.name = argv.name;
	   }	
	   if (argv.descr) {
		   // Put out a message indicating what value the user specified for this field.
		   console.log("new cluster's description: " + argv.descr);
		   // Update the field in the input data structure.
		   createClusterInputData.description = argv.descr;
	   }
	   if (argv.size) {
		   // Put out a message indicating what value the user specified for this field.
		   console.log("new cluster's size: " + argv.size);
		   // Update the field in the input data structure.
		   createClusterInputData.size = argv.size;
	   }
	   if (argv.location) {
		   // Put out a message indicating what value the user specified for this field.
		   console.log("new cluster's location: " + argv.location);
		   // Update the field in the input data structure.
		   createClusterInputData.parameters.cpeLocationName = argv.location;
	   }
	   
	   
	   var name = createClusterInputData.name;
	   var num = 0;
	   while (loop.count > 0 || loop.forever) {
		   loop.count -= 1;
		   num += 1; 
		   createClusterInputData.name = name + num; 
		   
		   // series of calls 
		   async.series({
			   createloop: function(createcb) {
				   // create the cluster 
				   loop.state = "CREATE"; 
				   loop.response = null;
				   loop.statusCode = 0;
				   loop.id = 0;
				   cluster.create(createcb);	
			   },
			   waitloop: function(callback) {
				   loop.state = "WAIT";
			       console.log("entering step check ...")
				   async.whilst(
						   function () { return (loop.state === "WAIT" && 
								   loop.statusCode === 200 && 
								   loop.id !== 0); },
						   function (waitcb) {
								   cluster.details(loop.id, waitcb);
						   },
						   function (err) {
							   console.log("the wait is over, loop.state: " + loop.state)
							   callback(null, loop.statusCode);
						   }
				   );   
		    	}, 
		    	deleteloop: function(deletecb) {
		    		// delete the cluster 
		    		cluster.deletecluster(loop.id, deletecb);
		    	}
		   });
		   console.log("loop " + num + " complete");
	   }	   
   }
   //--------------------------------------------------------------------------
   // loop: wait to ready
   //--------------------------------------------------------------------------
   else if (argv.wait) {
	   loop.id = argv.wait;
	   
	   loop.state = "WAIT";
	   loop.statusCode = 200;
       console.log("entering step check ...")
	   async.whilst(
			   function () { return (loop.state === "WAIT" && 
					   loop.statusCode === 200 && 
					   loop.id !== 0); },
			   function (waitcb) {
					   cluster.details(loop.id, waitcb);
			   },
			   function (err) {
				   console.log("the wait is over")
			   }
	   );   
   }
   //--------------------------------------------------------------------------
   // UpdateCluster (updates information for the specified cluster).
   //--------------------------------------------------------------------------
   else if (argv.put) {
      console.log("UpdateCluster");
      updateClusterOptions.path = config.cpe.path + '/clusters/' + argv.put;

      console.log("host: "   + updateClusterOptions.host);
      console.log("port: "   + updateClusterOptions.port);
      console.log("path: "   + updateClusterOptions.path);
      console.log("method: " + updateClusterOptions.method);

      // Parse input parms.
      var updateClusterTierStringFromFile;
      if (argv.file)
      {
         updateClusterTierStringFromFile = fs.readFileSync(argv.file, 'utf8');
      }
      else
      {
         if (argv.state) {
            // Put out a message indicating what value the user specified for this field.
            console.log("new cluster's state: " + argv.state);
            // Update the field in the input data structure.
            updateClusterState.id    = argv.put;
            updateClusterState.state = argv.state;
         }
      }

      httpRequest = https.request(updateClusterOptions, function(res) {
         var httpResponseDataAsStr = '';
         res.on('data', function (chunk) {
            httpResponseDataAsStr += chunk;
         });
         res.on('end', function () {
            assert.notStrictEqual(res, null);
            assert.notStrictEqual(res, undefined);
            //if (res.statusCode !== 202) {
            //   console.log("\nUpdateCluster response body = " + httpResponseDataAsStr);
            //   assert.equal(res.statusCode, 202);
            //}
            // Ensure the data is as expected.
            console.log('http status code: ' + res.statusCode);
            console.log(httpResponseDataAsStr);
         });
      });
      // Actually write the "input" data for this UpdateCluster in to the http request!
      if (argv.file) {
         httpRequest.write('{ "id" : "2c9e8381-3de00624-013d-f481592d-05a6", ' + updateClusterTierStringFromFile + ' }');
         //console.log('\n{ "id" : "2c9e8381-3de00624-013d-f481592d-05a6", ' + updateClusterTierStringFromFile + ' }');
      }
      else {
         httpRequest.write(JSON.stringify(updateClusterState));
      }
      httpRequest.end();
      httpRequest.on('error', function(error) { assert.equal(1, 2, error); });
   }
   //--------------------------------------------------------------------------
   // Invalid command was specified, show the help text.
   //--------------------------------------------------------------------------
   else {
      console.log("Usage: node cluster.js [--list]");
      console.log("                     | [--get clusterId]");
      console.log("                     | [--create --name name --descr description --size size --location location]");
      console.log("                     | [--post --file inputFilePathForCreateCluster]");
      console.log("                     | [--put clusterIdToUpdate --file inputFilePathForUpdateClusterTIERS]");
      console.log("                     | [--deletecluster clusterIdToDelete]");
      console.log("                     | [--loop count --name name --descr description --size size --location location ]"); // count = 0 loop forever
   }

}());
