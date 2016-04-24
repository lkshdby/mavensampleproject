define([], 
function(){
	var clusterStateName = {
		0: "New", 
		1: "Provisioning", 
		2: "Deploying", 
		3: "Importing data", 
		4: "Starting", 
		5: "Running", 
		6: "Stopping", 
		7: "Exporting data", 
		8: "Done", 
		9: "Deprovisioning", 
		10: "Deprovisioned", 
		11: "Deleting", 
		12: "Deleted", 
		13: "Failed" 
	};
		
	var monthName = {
			0: "Jan",
			1: "Feb",
			2: "Mar",
			3: "Apr",
			4: "May",
			5: "Jun",
			6: "Jul",
			7: "Aug",
			8: "Sep",
			9: "Oct",
			10: "Nov",
			11: "Dec"
	};
	
	var formatDateTime = function(ts) {
		var d = new Date();
		d.setTime(ts);
		
		var day = d.getDate();
		var month = monthName[d.getMonth()];
		var year = d.getFullYear();
		var hour = d.getHours();
		var minute = d.getMinutes();
		var second = d.getSeconds();
		
		var s = "" + day + month + year + " ";
		if (hour == 0) {
			s += "12";
		} else if (hour <= 12) {
			s += hour;
		} else {
			s += (hour - 12);
		}
		if (minute < 10) {
			s += ":0" + minute;
		} else {
			s += ":" + minute;
		}
//		if (second < 10) {
//			s += ":0" + second;
//		} else {
//			s += ":" + second;
//		}
		if (hour < 12) {
			s += " AM";
		} else {
			s += " PM";
		}
		return s;
	};

	var formatISODateTime = function(ts) {
		var d = new Date();
		d.setTime(ts);
		
		var day = d.getDate();
		var month = d.getMonth() + 1;
		var year = d.getFullYear();
		var hour = d.getHours();
		var minute = d.getMinutes();
		var second = d.getSeconds();
		
		var s = "" + year;
		if (month < 10) {
			s += "-0" + month;
		} else {
			s += "-" + month;
		}
		if (day < 10) { 
			s += "-0" + day;
		} else {
			s += "-" + day;
		}
		s += " ";
		if (hour == 0) {
			s += "00";
		} else if (hour < 10) {
			s += "0" + hour;
		} else {
			s += hour;
		}
		if (minute < 10) {
			s += ":0" + minute;
		} else {
			s += ":" + minute;
		}
//		if (second < 10) {
//			s += ":0" + second;
//		} else {
//			s += ":" + second;
//		}
		return s;
	};

	return {
		formatDateTime: formatDateTime,
		formatISODateTime: formatISODateTime
	};
});
