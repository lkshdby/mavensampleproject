define([
	"dojo/_base/declare", 
	"dijit/_WidgetBase", 
	"dijit/_TemplatedMixin", 
	"dojo/text!./templates/accesscontrol.html"
], function(declare, _WidgetBase, _TemplatedMixin, template) {

	return declare([ _WidgetBase, _TemplatedMixin ], {
		templateString : template,
		buildRendering : function() {
			this.inherited(arguments);
		},
		postCreate: function() {
			if (this.retry) {
				this.invalidAccessCode.style.display = "block";
			}
//			console.log("accesscontrol.js postCreate called.");
		}
	});
});
