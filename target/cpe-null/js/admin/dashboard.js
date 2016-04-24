define([
	"dojo/_base/declare", 
	"dojo/_base/lang", 
	"dojo/dom", 
	"dojo/on", 
	"dojo/request", 
	"dojox/math/round", 

	"dijit/_WidgetBase", 
	"dijit/_TemplatedMixin", 
	"dijit/_WidgetsInTemplateMixin",
	"dijit/Dialog", 
	"dijit/form/Button", 
	"dijit/form/TextBox", 

	"admin/urls",
	"platform/uiutils",
	
	"dojo/text!./templates/dashboard.html"
], function(
	declare, 
	lang, 
	dom, 
	on, 
	request, 
	mathRound,
	
	_WidgetBase, 
	_TemplatedMixin, 
	_WidgetsInTemplateMixin,
	Dialog, 
	Button, 
	TextBox, 
	
	urls, 
	UIUtils,
	
	template
) {

//	var apiKey = icasConfig.apiKey;
	
	var OFFERING_COL = 0;
	var NAME_COL = 1;
	var OWNER_COL = 2;
	var LAUNCH_TIME_COL = 3;
	var EXPIRATION_COL = 4;
	var ACTIONS_COL = 5;
	
	var formatDateTime = UIUtils.formatISODateTime;
	
	/* ------------------------------------------------------------------------------------ 
	 * declaring the widget
	 * ------------------------------------------------------------------------------------
	 */
	return declare([ _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin ], {
		templateString : template,
		
		buildRendering : function() {
			this.inherited(arguments);
//			console.log("build rendering called");
		},

		hideDialogCloseButton: function(dialog) {
			dialog.closeButtonNode.style.display = "none";
		},
		
		postCreate: function() {
//			console.log("postCreate called");
		
			this.hideDialogCloseButton(this.detailsDialog);
			this.hideDialogCloseButton(this.waitDialog);
//			this.hideDialogCloseButton(this.deleteDialog);
//			this.hideDialogCloseButton(this.errorDialog);
//			this.hideDialogCloseButton(this.dataTransferDialog);
			
			this.connect(this.refreshLink, "onclick", "refresh");

			this.updateClusterList();
		},
		
		showErrorMessage: function(msg, callback) {		
			this.errorDialogErrorMessage.innerHTML = msg;
			
			var okSignal = this.connect(this.errorDialogOKButton, "onClick", function(evt) {
				console.log("OK clicked");
				okSignal.remove();
				this.errorDialog.hide();
				callback();			
			});

			this.errorDialog.show();
		},

		refresh: function() {
			console.log("refresh called");
			this.updateClusterList();
		},
		
		updateInProgress: false,
		
		updateClusterList: function() {
			console.log("about to update cluster list.");
			if (this.updateInProgress) {
				console.log("Another update is in progress. Aborting.");
				return;
			} else {
				this.updateInProgress = true;
			}

			var url = urls.allcluster;
			var clusterList = this.clusterList;
			var showDetails = lang.hitch(this, "showDetails");
			var updateDone = lang.hitch(this, function() {
				this.updateInProgress = false;
			});
//			console.log("Calling GET " + url);
			request(url, { 
				handleAs: "json",
				headers: {
//					"api-key" : apiKey
				}
			}).then(function(clusters){
				console.debug(JSON.stringify(clusters, null, 2));
				
				console.debug("Repopulating cluster grid.");

				// populate the new list
				var tBody = clusterList.tBodies.item(0);
				while (tBody.rows.length > 0) {
					tBody.deleteRow(0);
				}
				clusters.forEach(function(cluster) {
					var rowId = tBody.rows.length;
					var row = tBody.insertRow(-1);
					row.setAttribute("offeringId", cluster.offeringId);
					row.setAttribute("accountId", cluster.accountId);
					row.setAttribute("owner", cluster.owner);
					row.setAttribute("id", cluster.id);
					
					var offeringCell = row.insertCell(-1);
					offeringCell.innerHTML = cluster.offeringName;

					var nameCell = row.insertCell(-1);
					var nameId = "cluster_name_" + rowId;
					nameCell.innerHTML = "<a href=\"#\" id=\"" + nameId + "\" title=\"Click for details\">" + cluster.name + "</a>";
					on(dom.byId(nameId), "click", function(event) {
						showDetails(rowId);
					});

					var ownerCell = row.insertCell(-1);
					ownerCell.innerHTML = cluster.ownerName + " (" + cluster.owner + ")";
					
					var launchTimeCell = row.insertCell(-1);
					launchTimeCell.innerHTML = formatDateTime(cluster.launchTime);
					
					var expirationCell = row.insertCell(-1);
					if (cluster.terminateTime <= 0) {
						expirationCell.innerHTML = "No expiry";
					} else {
						expirationCell.innerHTML = formatDateTime(cluster.terminateTime);
					}
					
					var actionsCell = row.insertCell(-1);
				});
				updateDone();
				console.debug("cluster grid updated.");
			});
		},

		/* ------------------------------------------------------------------------------------ 
		 * Details section related data structures and functions
		 * ------------------------------------------------------------------------------------
		 */
		showDetails: function(index) {
			var clusterList = this.clusterList;
			var tBody = clusterList.tBodies.item(0);
			if (index >= tBody.rows.length) {
				console.log("row index out of range: " + index);
				return;
			}
			
			console.debug("Retrieving cluster details for row " + index + " ...");

			var row = tBody.rows.item(index);
			var offeringId = row.getAttribute("offeringId");
			var accountId = row.getAttribute("accountId");
			var subscriberId = row.getAttribute("owner");
			var clusterId = row.getAttribute("id");
			console.debug("--> offering id: " + offeringId);
			console.debug("--> account id: " + accountId);
			console.debug("--> subscriber id: " + subscriberId);
			console.debug("--> cluster id: " + clusterId);

			var waitDialog = this.waitDialog;
			var detailsDialog = this.detailsDialog;
			var clusterDetails = this.clusterDetails;
			
			waitDialog.show();
			
			var url = urls.subscriber + "/" + offeringId + "/" + accountId + "/" + subscriberId;
			request(url, { 
				handleAs: "json",
				headers: {
//					"api-key": apiKey
				}
			}).then(function(subscriber){
				console.debug(JSON.stringify(subscriber, null, 2));
				
				var url = urls.cluster + "/" + clusterId;
				request(url, { 
					handleAs: "json",
					headers: {
						"subscriber-id": subscriberId,
						"api-key": subscriber.apiKey
					}
				}).then(function(cluster){
					console.debug(JSON.stringify(cluster, null, 2));


					var html = "<p><b>Name:</b> " + cluster.name + "</p>"
						+ "<p><b>Description:</b> " + cluster.description + "</p>" 
						+ "<p><b>Cluster Size:</b> " + cluster.size + " node(s)</p>" 
						+ "<p><b>Cluster ID:</b> " + cluster.clusterId + "</p>";

					var detailsHtml = "";
					var details = {};
					if (cluster.details != null) {
						detailsHtml += "<hr>";
						for (var i = 0, len = cluster.details.length; i < len; i ++) {
							var attr = cluster.details[i];
							var name = attr.name;
							var value = attr.value;
							details[name] = value;
							detailsHtml += "<p><b>" + name + ":</b> " + value + "</p>";
						}
					}

					var statusHtml = "";
					if (cluster.clusterId == null) {
						statusHtml = "<span class=\"ibm-error-link\" title=\"Cluster is missing. Please contact support.\">Error</span>";
					} else if (details.State == null) {
						statusHtml = "<span class=\"ibm-error-link\" title=\"Failed to retrieve cluster status. Please contact support.\">Error</span>";
					} else {
						if (details.State == "ACTIVE") {
							if (details.ApplicationAction == "Provision") {
								statusHtml = "<span class=\"ibm-check-link\">Provisioning</span>";
							} else if (details.ApplicationAction == "Cancel") {
								statusHtml = "<span class=\"ibm-check-link\">Canceling</span>";
							} else if (details.ApplicationAction == "Flex Up") {
								statusHtml = "<span class=\"ibm-check-link\">Flexing up</span>";
							} else if (details.ApplicationAction == "") {
								statusHtml = "<span class=\"ibm-check-link\">Ready</span>";
							} else {
								statusHtml = "<span class=\"ibm-error-link\" title=\"Please contact support. (applicationAction: " + details.ApplicationAction + ")\">Failed</span>";
							}
						} else if (details.State == "CANCELED") {
							statusHtml = "<span class=\"ibm-check-link\">Canceled</span>";
						} else if (details.State == "EXPIRED") {
							statusHtml = "<span class=\"ibm-error-link\" title=\"Your trial cluster has expired\">Expired</span>";
						} else {
							statusHtml = "<span class=\"ibm-error-link\" title=\"Please contact support. (state: " + details.State + ")\">Failed</span>";
						}
					}
					html += "<p><b>Status:</b> " + statusHtml + "</p>";
					html += detailsHtml;

					console.log(html);
					
					clusterDetails.innerHTML = html;
					detailsDialog.show();
					waitDialog.hide();
				});
			});
		},
	});
});
