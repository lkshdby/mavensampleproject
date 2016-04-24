define([
	"dojo/_base/declare", 
	"platform/wizard",
	"platform/smart_wizard_page"
], function(declare, Wizard, SmartWizardPage) {

	return declare([ Wizard ], {
		buildRendering: function() {
			this.inherited(arguments);
			console.log("Building smart wizard");
			this.addPage(new SmartWizardPage({name: "Steps : New cluster addition"}));
		}
	});
});
