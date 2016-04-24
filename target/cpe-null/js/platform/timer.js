define([	
	"dojo/_base/declare", 
	"dojo/_base/lang", 
	"dojo/Evented"
], function(
	declare,
	lang,
	Evented
){
	return declare([ Evented ], {
		timeout : 15000,
		constructor: function(args) {
			lang.mixin(this, args);
		},
		start : function() {
			this.stop();
			this.emit("start", {});
			var self = this;
			this._handle = setInterval(function() {
				self.emit("tick", {});
			}, this.timeout);
		},
		stop : function() {
			if (this._handle) {
				clearInterval(this._handle);
				delete this._handle;
				this.emit("stop", {});
			}
		}
	});
});
