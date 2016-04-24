# IBM_PROLOG_BEGIN_TAG 
# This is an automatically generated prolog. 
#  
#  
#  
# Licensed Materials - Property of IBM 
#  
# Restricted Materials of IBM 
#  
# (C) COPYRIGHT International Business Machines Corp. 2013,2014 
# All Rights Reserved 
#  
# US Government Users Restricted Rights - Use, duplication or 
# disclosure restricted by GSA ADP Schedule Contract with IBM Corp. 
#  
# IBM_PROLOG_END_TAG 
#
#
Summary: Cluster Provisioning Engine
Name: cpe
Version: @cpe.version@
Release: 20160423
License: IBM Corporation
Group: application
BuildArch: noarch 
Prefix: /opt/ibm

%define _unpackaged_files_terminate_build 0

%description
This package contains files needed for the CPE web application 

Builder: __REPLACE_BUILDER_NAME__ Build Date: __REPLACE_BUILD_DATE__

%define myprefix /opt/ibm
%define solution cpe

# directory holding the war file
%define cpedir %{myprefix}/%{solution}

# deployment target
%define tomcatdir /opt/platform/gui/3.0/tomcat/webappspub
%define appdir cpe

%pre 

%post 
if [[ ! -d %{tomcatdir} ]]; then
	mkdir -p %{tomcatdir}
	chown pcmaeadmin:pcmaeadmin %{tomcatdir}
fi

echo "Deploying CPE application to Tomcat ..."
cd %{tomcatdir}
mkdir -p %{appdir}
chown pcmaeadmin:pcmaeadmin %{appdir}
cd %{appdir}
rm -rf *

unzip %{cpedir}/cpe.war >& /dev/null
chown -R pcmaeadmin:pcmaeadmin *
rm -rf admin

echo "Done."
echo
echo "Please restart WEBGUI for the change to take effect"

%preun 

# Check if we are doing RPM Upgrade (rpm -U) or RPM Uninstall (rpm -e) 
if [ "$1" = "0" ]; then
	#
	# IMPORTANT !!! 
	#
	# The following is ONLY executed when we are REMOVING the product
	# completely. It is not run when we are doing an RPM UPGRADE
	# 
	echo "Removing CPE application from Tomcat ..."
	cd %{tomcatdir}
	rm -rf %{appdir}
fi

%files
# Files and directories 
%attr( 755, root, root ) %dir %{myprefix}
%attr( 755, root, root ) %dir %{myprefix}/%{solution}

# war file
%attr (600, pcmaeadmin, pcmaeadmin ) %{cpedir}/cpe.war

# sql scripts
%attr (644, pcmaeadmin, pcmaeadmin ) %{cpedir}/sql/create_db_mysql.sql
%attr (644, pcmaeadmin, pcmaeadmin ) %{cpedir}/sql/r2_db_migration.sql
%attr (644, pcmaeadmin, pcmaeadmin ) %{cpedir}/sql/cpe_ui_dump.sql
%attr (644, pcmaeadmin, pcmaeadmin ) %{cpedir}/sql/dgw_db_changes.sql

# Let's not clean up the BUILD_ROOT so we can see the files in RTC
%Clean
%if "%{noclean}" == ""
   rm -rf $RPM_BUILD_ROOT
%endif
