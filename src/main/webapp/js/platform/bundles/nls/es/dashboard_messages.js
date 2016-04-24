define({
    //button labels
    submitBtnText: "Enviar",
	closeBtnText: "Cerrar",
	deleteBtnText: "Suprimir",
	startBtnText: "Iniciar",
	stopBtnText: "Detener",
	okBtnText: "OK",
	editBtnText: "Editar",
	cancelBtnLabel: "Cancelar",
	saveBtnLabel: "Chatee y ahorre",
	
	pleaseWaitTitle: "Espere",
	pleaseWaitClusterCreationMsg: "Espere mientras se está enviando la solicitud de creación de clúster.",
	unknownErrorMsg: "Error desconocido",
	creatingClusterTitle: "Intentando crear un nuevo clúster",
	flexUpClusterTitle: "Intentando flexionar el clúster",
	clusterDetailsTitle: "Detalles del clúster",
	deleteClusterTitle: "Suprimir clúster",
	deleteClusterConfirmationMsg: "AVISO: si suprime un clúster, perderá todos los datos <br> que no se guarden o exporten desde el clúster. <br> ¿Está seguro de que desea suprimir este clúster?",
	notValidNumberMsg: "{NUBMER} no es un número válido",
	positiveNumberMsg: "Especifique un entero positivo",
	editClusterTitle: "Editar clúster",
	transferDataTitle: "Transferir datos",
	
	clickDetailsText: "Pulsar para obtener detalles",
	ownerNameSelfText: " (yo)",
	nodeText: " nodo",
	nodesText: " nodos",
	updateStatusText: "Actualizando...",
	clusterMissingText: "Falta el clúster. Póngase en contacto con el departamento de soporte.",
	clusterStatusFailedText: "No se ha podido recuperar el estado del clúster. Póngase en contacto con el departamento de soporte.",
	errorText: "Error",
	provisioningText: "Suministro",
	cancellingText: "Cancelación",
	flexupText: "Flexionando",
	readyText: "Preparado",
	failedText: "Error",
	failedAppActionTitleText: "Póngase en contacto con el departamento de soporte. (applicationAction: {APPACTION} )",
	failedStateTitleText: "Póngase en contacto con el departamento de soporte. (state: {STATE} )",
	approvingText: "Aprobando",
	cancelledText: "Cancelado",
	expiredText: "Caducado",
	expiredTitleText: "El clúster de prueba ha caducado",
	
	//cluster edit, delete and transfer links title
	editBtnTitle: "Editar",
	deleteBtnTitle: "Suprimir",
	transferDataBtnTitle: "Transferir datos",
	
	//show details popup lables
	nameText: "Nombre",
	descriptionText: "Descripción",
	launchTimeText: "Hora de inicio",
	expirationTimeText: "Hora de caducidad",
	clusterIdText: "ID de clúster",
	noExpirationText: "Sin caducidad",
	
	//edit cluster popup labels
	currentClusterSizeText: "Tamaño de clúster actual",
	additionalNotesText: "Nodos adicionales que se van a añadir",
	additionalNotesTitle: "Especifique nodos adicionales",
    
	//transfer data popup
	statusDoneText: "Hecho",
	statusErrorText: "La operación ha fallado. ERROR {ERRORCODE}: {ERRORDESC}",
	statusInProgressText: "La transferencia de datos está en curso...",
	statusStartedStatus: "Se ha iniciado. Puede cerrar ahora el diálogo.",
	statusFailedText: "No se ha podido iniciar",
	statusStoppedStatus: "Se ha detenido",
	
	//dashboard.html
	clusterNameText: "Nombre de clúster",
	ownerText: "Creado por",
	sizeText: "Tamaño",
	statusText: "Estado",
	vpnConfigurationText: "Configuración de VPN",
	actionText: "Acciones",
	addNewClusterLinkText: "Añadir nuevo clúster",
	refreshLinkText: "Renovar",
	quotaNodeText: "Ha suministrado {QUOTACURRENTUSAGE} de {QUOTALIMIT} nodos para su cuenta.",
	unlimitedText: "sin límite",
	locationText: "Ubicación",
	clusterFilterLabelText: "Filtrar por nombre",
	clusterFilterBtnText: " Búsqueda ",
	
	
	RESERVE_NETWORK : "Reservando red",
	CONFIGURE_MGMT_GATEWAY : "Configurando pasarela",
	TRUNK_CUST_GATEWAY : "Adjuntar pasarela",
	CONFIGURE_CUST_GATEWAY : "Configurando pasarela",
	TRUNK_HYPERVISORS : "Adjuntar hipervisores",
	PROVISION_NODES : "Suministrando clúster",
	
	// unprovision steps
	CANCEL_CLUSTER : "Cancelando clúster",
	UNTRUNK_HYPERVISORS : "Desadjuntar hipervisores",
	UNCONFIGURE_CUST_GATEWAY : "Desasignar pasarela",
	UNTRUNK_CUST_GATEWAY : "Desadjuntar pasarela",
	UNCONFIGURE_MGMT_GATEWAY : "Desasignar pasarela",
	UNRESERVE_NETWORK : "Anular la reserva de IP",
	REMOVE_RECORDS : "Eliminando clúster",
	
	//show VPN Configuration
	vpnDetailsTitle: "Detalles de parámetros de VPN",
	editVPNDetailsTitle: "Editar detalles de parámetros de VPN",
	viewVPNLinkText: "Ver",
	viewVPNLinkTitle: "Ver detalles de parámetros de VPN",
	editVPNLinkText: "Editar",
	editVPNLinkTitle: "Editar detalles de parámetros de VPN",
	cloudIpAddrText: "Dirección IP de nube",
	custIpText: "Dirección IP pública de la pasarela local",
	keyExchangeText: "Algoritmo de cifrado de intercambio de claves",
	dataIntegrityText: "Algoritmo hash de integridad de datos",
	diffeHellmanGrpIKESAText: "Grupo Diffe-Hellman para IKE SA",
	authMethodText: "Método de autenticación",
	lifeTimeIKESAText: "Tiempo de vida de IKE SA",
	encryptAlgoESPText: "Algoritmo de cifrado para ESP",
	espPFSText: "Confidencialidad directa total de ESP",
	hashAlgoESPText: "Algoritmo hash para ESP",
	keyLifeTimeESPText: "Tiempo de vida clave de ESP (en segundos)",
	preSharedSecretText: "Secreto precompartido",
	clusterSubnetText: "Subred de clúster",
	customerSubnetText: "Subred de cliente",
	connectionStatusText: "Estado conectado",
	connectedStatusText: "Conectada",
	retrievingConnectionStatusText: "Recuperando estado de conexión...",
	failedConnectionStatusText: "No se ha podido recuperar el estado de la conexión",
	disconnectedStatusText: "Desconectada",
	ikeEncryptAlgoKeyText: "IKE_ENCRYPTION_ALG",
	ikeHashAlgoKeyText: "IKE_HASH_ALG",
	ikeDHGrpKeyText: "IKE_DH_GROUP",
	ikeKeyLifeTimeKeyText: "IKE_KEY_LIFETIME",
	espEncryptAlgoKeyText: "ESP_ENCRYPTION_ALG",
	espPFSKeyText: "ESP_PFS",
	espHashAlgoKeyText: "ESP_HASH_ALG",
	espKeyLifeTimeKeyText: "ESP_KEY_LIFETIME",
	authModeKeyText: "AUTHENTICATION_MODE",
	preSharedSecretKeyText: "PRE_SHARED_SECRET",
	custSubnetKeyText: "CUST_SUBNETS",
	pleaseWaitVPNParamsSaveMsg: "Espere hasta que se guarden los parámetros de VPN.",
	saveVPNPramsTitle: "Error de guardado de los parámetros de VPN",
});