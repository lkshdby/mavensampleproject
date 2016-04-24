define([
	"dojo/_base/declare", 
	"dojo/dom",
	"dijit/_WidgetBase", 
	"dijit/_TemplatedMixin", 
	"dojo/text!./templates/submit_wizard.html"
], function(declare, dom, _WidgetBase, _TemplatedMixin, template) {

	return declare([ _WidgetBase, _TemplatedMixin ], {
		templateString : template,
		tempContext: {},
		show: function(context) {
			
		}
	});
});
