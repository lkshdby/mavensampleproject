define([
	"dojo/_base/declare", 
	"dijit/_WidgetBase", 
	"dijit/_TemplatedMixin", 
	"dojo/text!./templates/capacity.html"
], function(declare, _WidgetBase, _TemplatedMixin, template) {

	return declare([ _WidgetBase, _TemplatedMixin ], {
		templateString : template,
		buildRendering : function() {
			this.inherited(arguments);
		},
		postCreate: function() {
			if (this.availableNodes > 0) {
				this.reorderOption.style.display = "block";
			} else {
				this.pleaseCallOption.style.display = "block";
			}
			console.log("capacity.js postCreate called.");
		}
	});
});
