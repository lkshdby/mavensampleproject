define({
	//populate-smartwizard-plugin.js messages
	stepDataErrorMsg: "Impossible d'extraire les données d'étape",
	stepDetailsErrorMsg:"Impossible d'extraire les informations d'étape",
	authFailedTitle: "Echec de l'authentification",
	noDataFoundMsg: "Données introuvables",
	usernameApiKeyMissingMsg: "Veuillez entrer le nom d'utilisateur et la clé API.",
	validClusterNameMsg: "Le nom de cluster est disponible et peut être utilisé",
	invalidClusterNameMsg: "Nom de cluster non valide. Il ne doit contenir que des caractères alphanumériques, des espaces, \".\", \"(\", \")\", des traits de soulignements, des point-virgules, des tirets et des caractères chinois et ne doit pas dépasser 64 caractères.",
	clusterNameExistMsg: "Un nom de cluster identique existe déjà. Veuillez essayer un autre nom",
	noSpinnerDataMsg: "Impossible de remplir le bouton fléché",
	paramMissingMsg: "Paramètre manquant",
	noTransferDataFoundMsg: "Impossible d'extraire les données de transfert",
	stepLabel: "Etape",
	yesBtnText: "Oui",
	noBtnText: "Non",
	nextBtnLabel: "Suivant",
	prevBtnLabel: "Précédent",
	finishBtnLabel: "Terminer",
	cancelBtnLabel: "Annuler",
	saveBtnLabel: "Sauvegarder",
	addBtnText: "Ajouter",
	nodeConfigurationTitle: "Configuration du noeud",
	clusterSizeCalculationText: "Formule pour calculer la taille du cluster Hadoop",
	smallDataNodeText: "Nbre de noeuds ayant des données de petite envergure = (taille des données brutes en To * 4)/ 7",
	largeDataNodeText: "Nbre de noeuds ayant des données de grande envergure = (taille des données brutes en To * 4)/ 35",
	transferDataText: "Centre de données de transfert",
	transferDataDesc: "Transfert de données",
	protectedNetworkTitle: "Réseaux protégés",

	//for subnet list popup
	customerSubnetHeaderText: "Sous-réseaux client",
	softLayerSubnetHeaderText:"Sous-réseaux Softlayer protégés",
	subnetHeaderText:"Sous-réseau",
	actionHeaderText:"Action",
	removeSubnetText:"Supprimer du réseau privé virtuel IPSec",
	ipAddrFieldsText:"Adresse IP",
	cidrFieldsText:"CIDR",

	subnetReqFieldsMsg:"Veuillez renseigner les zones obligatoires pour ajouter un sous-réseau",
	subnetIpAddrInvalidMsg:"Veuillez entrer une adresse IP valide. Le format valide est le suivant : xx.xx.xx.x",
	subnetCIDRInvalidMsg:"Veuillez entrer une valeur numérique pour CIDR",
	emptySubnetListMsg: "La liste des sous-réseaux ne peut pas être vide. Veuillez ajouter au moins un sous-réseau <br> OU annuler l'opération en cliquant sur le bouton \"Annuler\".",
	subnetValidationMsg: "La liste des sous-réseaux ne peut pas être vide. Veuillez cliquer sur le lien \"Sous-réseau client\" pour ajouter une adresse IP de sous-réseau et un routage CIDR.  ",

	//wizard.js messages
	requiredFieldsMsg: "Certaines zones obligatoires sont manquantes ou non valides.<br> Veuillez fournir l'entrée obligatoire.",
	accountQuotaTitle: "Aucun quota de compte",
	accountQuotaMsg: "Vous avez atteint le quota de votre compte.<br> Impossible de créer un cluster.",
	addNewClusterFailedTitle: "Echec de l'ajout d'un nouveau cluster",
	addNewClusterFailedMsg: "Impossible d'obtenir les informations d'étape pour créer un cluster.<br> Veuillez contacter le Support ou l'Administrateur.",
	wizardHeading:  "Ajouter un nouveau cluster",

	//common messages for both wizard.js and populate-smartwizard-plugin.js
	validationFailedTitle: "Echec de la validation",
	infoTitle: "Informations",
	okBtnText: "OK",

	//edit vpn params popup
	clusterSubnetText: "Sous-réseau de cluster ",
	customerSubnetText: "Sous-réseau de client",
});