define([], 
function(){
	return {
		transfer: icasConfig.contextPath + "/rest/transfers",
		cluster: icasConfig.contextPath + "/rest/clusters", 
		wizard: icasConfig.contextPath + "/rest/wizard",
		tunnel: icasConfig.contextPath + "/rest/tunnels",
		gateway: icasConfig.contextPath + "/rest/gateway",
	};
});
